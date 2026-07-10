import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PeluqueroService } from '../../shared/services/peluquero.service';
import { TratamientoService } from '../../shared/services/tratamiento.service';
import { TurnoService } from '../../shared/services/turno.service';
import { Peluquero } from '../../shared/models/peluquero.model';
import { Tratamiento } from '../../shared/models/tratamiento.model';
import { Slot } from '../../shared/models/turno.model';

@Component({
  selector: 'app-reserva',
  imports: [FormsModule],
  templateUrl: './reserva.html'
})
export class Reserva implements OnInit {
  private peluqueroService = inject(PeluqueroService);
  private tratamientoService = inject(TratamientoService);
  private turnoService = inject(TurnoService);
  private router = inject(Router);

  peluqueros = signal<Peluquero[]>([]);
  tratamientos = signal<Tratamiento[]>([]);

  peluqueroId = signal<number | null>(null);
  tratamientoId = signal<number | null>(null);
  fecha = signal<string>(''); // yyyy-MM-dd

  slots = signal<Slot[] | null>(null);
  loadingCatalog = signal(true);
  loadingSlots = signal(false);
  reservando = signal<string | null>(null); // inicio del slot en curso
  error = signal<string | null>(null);
  success = signal<string | null>(null);

  // martes(2)..domingo(7 -> 0). Lunes cerrado.
  esLunes = computed(() => {
    const f = this.fecha();
    if (!f) return false;
    return new Date(f + 'T00:00:00').getDay() === 1;
  });

  puedeBuscar = computed(() =>
    this.peluqueroId() != null &&
    this.tratamientoId() != null &&
    !!this.fecha() &&
    !this.esLunes()
  );

  hoy = new Date().toISOString().slice(0, 10);

  ngOnInit(): void {
    this.peluqueroService.list().subscribe({
      next: (p) => { this.peluqueros.set(p); this.tryFinishCatalog(); },
      error: () => { this.error.set('No se pudo cargar el catálogo.'); this.loadingCatalog.set(false); }
    });
    this.tratamientoService.list().subscribe({
      next: (t) => { this.tratamientos.set(t); this.tryFinishCatalog(); },
      error: () => { this.error.set('No se pudo cargar el catálogo.'); this.loadingCatalog.set(false); }
    });
  }

  private catalogPending = 2;
  private tryFinishCatalog(): void {
    if (--this.catalogPending <= 0) this.loadingCatalog.set(false);
  }

  buscar(): void {
    if (!this.puedeBuscar()) return;
    this.slots.set(null);
    this.success.set(null);
    this.error.set(null);
    this.loadingSlots.set(true);
    this.turnoService.disponibilidad(this.peluqueroId()!, this.fecha(), this.tratamientoId()!).subscribe({
      next: (d) => { this.slots.set(d.slots); this.loadingSlots.set(false); },
      error: () => { this.error.set('No se pudo obtener la disponibilidad.'); this.loadingSlots.set(false); }
    });
  }

  reservar(slot: Slot): void {
    this.reservando.set(slot.inicio);
    this.error.set(null);
    this.turnoService.reservar({
      peluqueroId: this.peluqueroId()!,
      tratamientoId: this.tratamientoId()!,
      inicio: slot.inicio
    }).subscribe({
      next: () => {
        this.reservando.set(null);
        this.success.set('¡Turno reservado! Te enviamos la confirmación por email y WhatsApp.');
        // refrescar slots para reflejar el ocupado
        this.buscar();
      },
      error: (e) => {
        this.reservando.set(null);
        this.error.set(e?.status === 409
          ? 'Ese horario ya no está disponible.'
          : 'No se pudo reservar el turno.');
      }
    });
  }

  hora(iso: string): string {
    return iso.slice(11, 16);
  }

  irAMisTurnos(): void {
    this.router.navigate(['/mis-turnos']);
  }
}
