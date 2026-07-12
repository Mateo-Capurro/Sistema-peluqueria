import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PeluqueroService } from '../../shared/services/peluquero.service';
import { Peluquero } from '../../shared/models/peluquero.model';

@Component({
  selector: 'app-admin-peluqueros',
  imports: [FormsModule],
  templateUrl: './admin-peluqueros.html'
})
export class AdminPeluqueros implements OnInit {
  private service = inject(PeluqueroService);

  peluqueros = signal<Peluquero[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  saving = signal(false);

  editingId = signal<number | null>(null); // null = alta de cuenta nueva

  // alta de cuenta (create)
  username = '';
  password = '';
  name = '';
  email = '';
  telefono = '';
  dni = '';

  // jornada / estado (create + update)
  horaInicio = '';
  horaFin = '';
  activo = true;

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.service.list().subscribe({
      next: (p) => { this.peluqueros.set(p); this.loading.set(false); },
      error: () => { this.error.set('No se pudieron cargar los peluqueros.'); this.loading.set(false); }
    });
  }

  nuevo(): void {
    this.editingId.set(null);
    this.username = ''; this.password = ''; this.name = '';
    this.email = ''; this.telefono = ''; this.dni = '';
    this.horaInicio = ''; this.horaFin = ''; this.activo = true;
  }

  editar(p: Peluquero): void {
    this.editingId.set(p.id);
    this.horaInicio = p.horaInicio.slice(0, 5);
    this.horaFin = p.horaFin.slice(0, 5);
    this.activo = p.activo;
  }

  private get jornadaValida(): boolean {
    return !!this.horaInicio && !!this.horaFin && this.horaInicio < this.horaFin;
  }

  get formValido(): boolean {
    if (this.editingId() != null) return this.jornadaValida;
    return this.username.trim().length >= 3
      && this.password.length >= 6
      && this.name.trim().length > 0
      && this.email.trim().length > 0
      && this.telefono.trim().length > 0
      && this.dni.trim().length > 0
      && this.jornadaValida;
  }

  guardar(): void {
    if (!this.formValido) return;
    this.saving.set(true);
    this.error.set(null);
    const id = this.editingId();
    const obs = id == null
      ? this.service.create({
          username: this.username.trim(), password: this.password, name: this.name.trim(),
          email: this.email.trim(), telefono: this.telefono.trim(), dni: this.dni.trim(),
          horaInicio: this.horaInicio, horaFin: this.horaFin
        })
      : this.service.update(id, { horaInicio: this.horaInicio, horaFin: this.horaFin, activo: this.activo });
    obs.subscribe({
      next: () => { this.saving.set(false); this.nuevo(); this.cargar(); },
      error: (e) => {
        this.error.set(e?.status === 409
          ? 'Ya existe un usuario con ese username, email o dni.'
          : 'No se pudo guardar el peluquero.');
        this.saving.set(false);
      }
    });
  }
}
