import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TurnoService } from '../../shared/services/turno.service';
import { Turno } from '../../shared/models/turno.model';

type Estado = 'cargando' | 'ok' | 'error';

@Component({
  selector: 'app-confirmar',
  imports: [RouterLink],
  templateUrl: './confirmar.html'
})
export class Confirmar implements OnInit {
  private route = inject(ActivatedRoute);
  private turnoService = inject(TurnoService);

  estado = signal<Estado>('cargando');
  turno = signal<Turno | null>(null);
  mensaje = signal<string>('');

  ngOnInit(): void {
    const token = this.route.snapshot.paramMap.get('token');
    if (!token) {
      this.estado.set('error');
      this.mensaje.set('Link inválido.');
      return;
    }
    this.turnoService.confirmarPorToken(token).subscribe({
      next: (t) => { this.turno.set(t); this.estado.set('ok'); },
      error: (err) => {
        this.estado.set('error');
        this.mensaje.set(err?.error?.message || 'No se pudo confirmar el turno.');
      }
    });
  }

  fechaHora(iso: string): string {
    return iso.slice(8, 10) + '/' + iso.slice(5, 7) + '/' + iso.slice(0, 4) + ' ' + iso.slice(11, 16);
  }
}
