import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Peluquero, PeluqueroRequest, PeluqueroUpdateRequest } from '../models/peluquero.model';

@Injectable({ providedIn: 'root' })
export class PeluqueroService {
  private http = inject(HttpClient);
  private readonly base = '/api/peluqueros';

  list(): Observable<Peluquero[]> {
    return this.http.get<Peluquero[]>(this.base);
  }

  get(id: number): Observable<Peluquero> {
    return this.http.get<Peluquero>(`${this.base}/${id}`);
  }

  create(req: PeluqueroRequest): Observable<Peluquero> {
    return this.http.post<Peluquero>(this.base, req);
  }

  update(id: number, req: PeluqueroUpdateRequest): Observable<Peluquero> {
    return this.http.put<Peluquero>(`${this.base}/${id}`, req);
  }
}
