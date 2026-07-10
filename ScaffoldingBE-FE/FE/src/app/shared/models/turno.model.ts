export type EstadoTurno = 'PENDIENTE' | 'CONFIRMADO' | 'CANCELADO' | 'COMPLETADO';

export interface Turno {
  id: number;
  clienteNombre: string;
  peluqueroNombre: string;
  tratamientoNombre: string;
  inicio: string; // ISO LocalDateTime
  fin: string;
  estado: EstadoTurno;
}

export interface ReservaTurnoRequest {
  peluqueroId: number;
  tratamientoId: number;
  inicio: string; // ISO LocalDateTime, e.g. "2026-07-14T10:00:00"
}

export interface Slot {
  inicio: string;
  fin: string;
}

export interface Disponibilidad {
  peluqueroId: number;
  fecha: string; // "yyyy-MM-dd"
  slots: Slot[];
}
