import { Component, inject, signal, OnInit } from '@angular/core';
import { PeluqueroService } from '../../shared/services/peluquero.service';
import { Peluquero } from '../../shared/models/peluquero.model';

@Component({
  selector: 'app-peluquero-list',
  imports: [],
  templateUrl: './peluquero-list.html'
})
export class PeluqueroList implements OnInit {
  private peluqueroService = inject(PeluqueroService);

  peluqueros = signal<Peluquero[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

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
