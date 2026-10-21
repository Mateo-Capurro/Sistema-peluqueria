export type FormaRostro = 'ovalada' | 'redonda' | 'cuadrada' | 'alargada' | 'corazon' | 'diamante';

export type EstiloCorte = 'masculino' | 'femenino';

/** Punto de landmark normalizado (0..1) como lo devuelve MediaPipe. */
export interface PuntoFacial {
  x: number;
  y: number;
  z?: number;
}

/** Medidas en píxeles reales de la imagen. */
export interface MedidasRostro {
  largo: number;
  anchoFrente: number;
  anchoPomulos: number;
  anchoMandibula: number;
  /** largo / anchoPomulos */
  ratioLargoAncho: number;
  /** anchoFrente / anchoPomulos */
  ratioFrente: number;
  /** anchoMandibula / anchoPomulos */
  ratioMandibula: number;
}

export interface Postura {
  /** Inclinación lateral de la cabeza en grados. */
  rollGrados: number;
  /** Asimetría izquierda/derecha (0 = de frente). */
  giro: number;
  ok: boolean;
  motivo?: string;
}

export interface Recomendacion {
  nombre: string;
  descripcion: string;
}

export interface ResultadoAnalisis {
  forma: FormaRostro;
  medidas: MedidasRostro;
  postura: Postura;
}
