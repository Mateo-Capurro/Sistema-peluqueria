import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Tratamiento, TratamientoRequest } from '../models/tratamiento.model';

@Injectable({ providedIn: 'root' })
export class TratamientoService {
  private http = inject(HttpClient);
  private readonly base = '/api/tratamientos';

  list(): Observable<Tratamiento[]> {
    return this.http.get<Tratamiento[]>(this.base);
  }

  get(id: number): Observable<Tratamiento> {
    return this.http.get<Tratamiento>(`${this.base}/${id}`);
  }

  create(req: TratamientoRequest): Observable<Tratamiento> {
    return this.http.post<Tratamiento>(this.base, req);
  }

  update(id: number, req: TratamientoRequest): Observable<Tratamiento> {
    return this.http.put<Tratamiento>(`${this.base}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
