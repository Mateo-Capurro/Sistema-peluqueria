import { Component, inject, signal, OnInit } from '@angular/core';
import { TurnoService } from '../../shared/services/turno.service';
import { Turno } from '../../shared/models/turno.model';

@Component({
  selector: 'app-agenda',
  imports: [],
  templateUrl: './agenda.html'
})
export class Agenda implements OnInit {
  private turnoService = inject(TurnoService);

  turnos = signal<Turno[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  accionando = signal<number | null>(null); // id del turno en curso

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.turnoService.agenda().subscribe({
      next: (t) => { this.turnos.set(t); this.loading.set(false); },
      error: () => { this.error.set('No se pudo cargar la agenda.'); this.loading.set(false); }
    });
  }

  completar(t: Turno): void {
    this.ejecutar(t.id, this.turnoService.completar(t.id));
  }

  cancelar(t: Turno): void {
    this.ejecutar(t.id, this.turnoService.cancelar(t.id));
  }

  private ejecutar(id: number, obs: ReturnType<TurnoService['completar']>): void {
    this.accionando.set(id);
    this.error.set(null);
    obs.subscribe({
      next: (actualizado) => {
        this.turnos.update(list => list.map(x => x.id === id ? actualizado : x));
        this.accionando.set(null);
      },
      error: () => {
        this.error.set('No se pudo actualizar el turno.');
        this.accionando.set(null);
      }
    });
  }

  fecha(iso: string): string {
    return iso.slice(0, 10).split('-').reverse().join('/');
  }

  hora(iso: string): string {
    return iso.slice(11, 16);
  }

  badge(estado: string): string {
    switch (estado) {
      case 'PENDIENTE':  return 'bg-amber-100 text-amber-800';
      case 'CONFIRMADO': return 'bg-blue-100 text-blue-800';
      case 'COMPLETADO': return 'bg-green-100 text-green-800';
      case 'CANCELADO':  return 'bg-gray-100 text-gray-600';
      default:           return 'bg-gray-100 text-gray-600';
    }
  }
}
