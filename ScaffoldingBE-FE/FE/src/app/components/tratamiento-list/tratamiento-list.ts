import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TratamientoService } from '../../shared/services/tratamiento.service';
import { AuthService } from '../../shared/services/auth.service';
import { Tratamiento } from '../../shared/models/tratamiento.model';

@Component({
  selector: 'app-tratamiento-list',
  imports: [],
  templateUrl: './tratamiento-list.html'
})
export class TratamientoList implements OnInit {
  private tratamientoService = inject(TratamientoService);
  private auth = inject(AuthService);
  private router = inject(Router);

  tratamientos = signal<Tratamiento[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  esCliente = computed(() => this.auth.role() === 'CLIENTE');

  reservarCon(id: number): void {
    this.router.navigate(['/reservar'], { queryParams: { tratamiento: id } });
  }

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
