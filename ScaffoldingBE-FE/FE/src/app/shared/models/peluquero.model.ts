export interface Peluquero {
  id: number;
  nombre: string;
  horaInicio: string; // "HH:mm:ss"
  horaFin: string;
  activo: boolean;
}

export interface PeluqueroRequest {
  username: string;
  password: string;
  name: string;
  email: string;
  telefono: string;
  dni: string;
  horaInicio: string;
  horaFin: string;
}

export interface PeluqueroUpdateRequest {
  horaInicio: string;
  horaFin: string;
  activo: boolean;
}
