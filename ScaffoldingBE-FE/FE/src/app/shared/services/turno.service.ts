import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Disponibilidad, ReservaTurnoRequest, Turno } from '../models/turno.model';

@Injectable({ providedIn: 'root' })
export class TurnoService {
  private http = inject(HttpClient);
  private readonly base = '/api/turnos';

  disponibilidad(peluqueroId: number, fecha: string, tratamientoId: number): Observable<Disponibilidad> {
    const params = new HttpParams()
      .set('peluqueroId', peluqueroId)
      .set('fecha', fecha)
      .set('tratamientoId', tratamientoId);
    return this.http.get<Disponibilidad>(`${this.base}/disponibilidad`, { params });
  }

  reservar(req: ReservaTurnoRequest): Observable<Turno> {
    return this.http.post<Turno>(this.base, req);
  }

  misTurnos(): Observable<Turno[]> {
    return this.http.get<Turno[]>(`${this.base}/mios`);
  }

  agenda(): Observable<Turno[]> {
    return this.http.get<Turno[]>(`${this.base}/agenda`);
  }

  confirmar(id: number): Observable<Turno> {
    return this.http.patch<Turno>(`${this.base}/${id}/confirmar`, {});
  }

  confirmarPorToken(token: string): Observable<Turno> {
    return this.http.post<Turno>(`${this.base}/confirmar/token/${token}`, {});
  }

  cancelar(id: number): Observable<Turno> {
    return this.http.patch<Turno>(`${this.base}/${id}/cancelar`, {});
  }

  completar(id: number): Observable<Turno> {
    return this.http.patch<Turno>(`${this.base}/${id}/completar`, {});
  }
}
