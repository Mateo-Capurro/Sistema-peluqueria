import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TratamientoService } from '../../shared/services/tratamiento.service';
import { Tratamiento } from '../../shared/models/tratamiento.model';

@Component({
  selector: 'app-admin-tratamientos',
  imports: [FormsModule],
  templateUrl: './admin-tratamientos.html'
})
export class AdminTratamientos implements OnInit {
  private service = inject(TratamientoService);

  tratamientos = signal<Tratamiento[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  saving = signal(false);

  // formulario
  editingId = signal<number | null>(null);
  nombre = '';
  duracionMinutos: number | null = null;
  precio: number | null = null;

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.service.list().subscribe({
      next: (t) => { this.tratamientos.set(t); this.loading.set(false); },
      error: () => { this.error.set('No se pudieron cargar los tratamientos.'); this.loading.set(false); }
    });
  }

  nuevo(): void {
    this.editingId.set(null);
    this.nombre = '';
    this.duracionMinutos = null;
    this.precio = null;
  }

  editar(t: Tratamiento): void {
    this.editingId.set(t.id);
    this.nombre = t.nombre;
    this.duracionMinutos = t.duracionMinutos;
    this.precio = t.precio;
  }

  get formValido(): boolean {
    return this.nombre.trim().length > 0
      && this.duracionMinutos != null && this.duracionMinutos >= 1
      && this.precio != null && this.precio >= 0;
  }

  guardar(): void {
    if (!this.formValido) return;
    this.saving.set(true);
    this.error.set(null);
    const req = {
      nombre: this.nombre.trim(),
      duracionMinutos: this.duracionMinutos!,
      precio: this.precio!
    };
    const id = this.editingId();
    const obs = id == null ? this.service.create(req) : this.service.update(id, req);
    obs.subscribe({
      next: () => { this.saving.set(false); this.nuevo(); this.cargar(); },
      error: () => { this.error.set('No se pudo guardar el tratamiento.'); this.saving.set(false); }
    });
  }

  eliminar(t: Tratamiento): void {
    this.error.set(null);
    this.service.delete(t.id).subscribe({
      next: () => { if (this.editingId() === t.id) this.nuevo(); this.cargar(); },
      error: () => this.error.set('No se pudo eliminar el tratamiento.')
    });
  }
}
