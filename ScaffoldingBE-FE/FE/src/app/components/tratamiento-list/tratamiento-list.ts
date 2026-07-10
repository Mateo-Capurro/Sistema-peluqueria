import { Component, inject, signal, OnInit } from '@angular/core';
import { TratamientoService } from '../../shared/services/tratamiento.service';
import { Tratamiento } from '../../shared/models/tratamiento.model';

@Component({
  selector: 'app-tratamiento-list',
  imports: [],
  templateUrl: './tratamiento-list.html'
})
export class TratamientoList implements OnInit {
  private tratamientoService = inject(TratamientoService);

  tratamientos = signal<Tratamiento[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.tratamientoService.list().subscribe({
      next: (data) => {
        this.tratamientos.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los tratamientos.');
        this.loading.set(false);
      }
    });
  }
}
