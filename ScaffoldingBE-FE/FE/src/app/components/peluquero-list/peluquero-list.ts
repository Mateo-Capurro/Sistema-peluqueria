import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PeluqueroService } from '../../shared/services/peluquero.service';
import { AuthService } from '../../shared/services/auth.service';
import { Peluquero } from '../../shared/models/peluquero.model';

@Component({
  selector: 'app-peluquero-list',
  imports: [],
  templateUrl: './peluquero-list.html'
})
export class PeluqueroList implements OnInit {
  private peluqueroService = inject(PeluqueroService);
  private auth = inject(AuthService);
  private router = inject(Router);

  peluqueros = signal<Peluquero[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  esCliente = computed(() => this.auth.role() === 'CLIENTE');

  reservarCon(id: number): void {
    this.router.navigate(['/reservar'], { queryParams: { peluquero: id } });
  }

  ngOnInit(): void {
    this.peluqueroService.list().subscribe({
      next: (data) => {
        this.peluqueros.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los peluqueros.');
        this.loading.set(false);
      }
    });
  }
}
