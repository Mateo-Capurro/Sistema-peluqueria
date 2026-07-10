export interface Tratamiento {
  id: number;
  nombre: string;
  duracionMinutos: number;
  precio: number;
}

export interface TratamientoRequest {
  nombre: string;
  duracionMinutos: number;
  precio: number;
}
