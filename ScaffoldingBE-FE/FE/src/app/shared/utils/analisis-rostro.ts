import {
  EstiloCorte,
  FormaRostro,
  MedidasRostro,
  PuntoFacial,
  Postura,
  Recomendacion,
  ResultadoAnalisis
} from '../models/rostro.model';

/**
 * Índices de la malla de 478 puntos de MediaPipe Face Landmarker.
 * Referencia: https://ai.google.dev/edge/mediapipe/solutions/vision/face_landmarker
 */
export const LM = {
  frenteSuperior: 10,   // parte alta de la frente (NO es la línea del pelo)
  menton: 152,
  pomuloIzq: 234,
  pomuloDer: 454,
  frenteIzq: 54,
  frenteDer: 284,
  mandibulaIzq: 172,
  mandibulaDer: 397,
  ojoExtIzq: 33,
  ojoExtDer: 263,
  punta_nariz: 1
} as const;

/**
 * Umbrales de clasificación. Son un punto de partida: conviene calibrarlos
 * con fotos reales de clientes (ver README del módulo).
 */
export const UMBRALES = {
  alargada: 1.42,        // largo/ancho por encima → cara alargada
  corta: 1.22,           // largo/ancho por debajo → redonda o cuadrada
  mandibulaCuadrada: 0.82, // mandíbula casi tan ancha como pómulos
  frenteCorazon: 0.12,   // frente más ancha que mandíbula por este margen (relativo a pómulos)
  diamante: 0.80,        // frente y mandíbula angostas respecto a pómulos
  rollMax: 10,           // grados
  giroMax: 0.18
} as const;

const dist = (a: PuntoFacial, b: PuntoFacial, w: number, h: number) =>
  Math.hypot((a.x - b.x) * w, (a.y - b.y) * h);

/** Calcula medidas en píxeles (corrige la deformación de imágenes no cuadradas). */
export function calcularMedidas(p: PuntoFacial[], ancho: number, alto: number): MedidasRostro {
  const largo = dist(p[LM.frenteSuperior], p[LM.menton], ancho, alto);
  const anchoPomulos = dist(p[LM.pomuloIzq], p[LM.pomuloDer], ancho, alto);
  const anchoFrente = dist(p[LM.frenteIzq], p[LM.frenteDer], ancho, alto);
  const anchoMandibula = dist(p[LM.mandibulaIzq], p[LM.mandibulaDer], ancho, alto);
  return {
    largo,
    anchoFrente,
    anchoPomulos,
    anchoMandibula,
    ratioLargoAncho: largo / anchoPomulos,
    ratioFrente: anchoFrente / anchoPomulos,
    ratioMandibula: anchoMandibula / anchoPomulos
  };
}

/** Verifica que la cara esté de frente y derecha; si no, las medidas no sirven. */
export function evaluarPostura(p: PuntoFacial[], ancho: number, alto: number): Postura {
  const oi = p[LM.ojoExtIzq];
  const od = p[LM.ojoExtDer];
  const rollGrados = Math.abs(
    (Math.atan2((od.y - oi.y) * alto, (od.x - oi.x) * ancho) * 180) / Math.PI
  );

  const nariz = p[LM.punta_nariz];
  const izq = Math.abs(nariz.x - p[LM.pomuloIzq].x);
  const der = Math.abs(p[LM.pomuloDer].x - nariz.x);
  const giro = Math.abs(izq - der) / (izq + der);

  if (rollGrados > UMBRALES.rollMax) {
    return { rollGrados, giro, ok: false, motivo: 'Tenés la cabeza inclinada. Mantenela derecha.' };
  }
  if (giro > UMBRALES.giroMax) {
    return { rollGrados, giro, ok: false, motivo: 'Estás girando la cara. Mirá directo a la cámara.' };
  }
  return { rollGrados, giro, ok: true };
}

/** Reglas de clasificación de forma de rostro a partir de las proporciones. */
export function clasificarForma(m: MedidasRostro): FormaRostro {
  const { ratioLargoAncho: r, ratioFrente: f, ratioMandibula: j } = m;

  // 1) La proporción largo/ancho es la señal más fuerte → se evalúa primero.
  if (r >= UMBRALES.alargada) return 'alargada';
  if (r < UMBRALES.corta) {
    return j >= UMBRALES.mandibulaCuadrada ? 'cuadrada' : 'redonda';
  }
  // 2) Largo intermedio: decide la relación frente / pómulos / mandíbula.
  if (f < UMBRALES.diamante && j < UMBRALES.diamante - 0.05) return 'diamante';
  if (f - j > UMBRALES.frenteCorazon && j < UMBRALES.mandibulaCuadrada) return 'corazon';
  return 'ovalada';
}

export function analizarRostro(p: PuntoFacial[], ancho: number, alto: number): ResultadoAnalisis {
  const medidas = calcularMedidas(p, ancho, alto);
  return {
    medidas,
    postura: evaluarPostura(p, ancho, alto),
    forma: clasificarForma(medidas)
  };
}

export const DESCRIPCION_FORMA: Record<FormaRostro, { titulo: string; texto: string; objetivo: string }> = {
  ovalada: {
    titulo: 'Ovalado',
    texto: 'Proporciones equilibradas, frente y mandíbula similares y mentón suave.',
    objetivo: 'Casi cualquier corte te queda bien. Podés elegir por estilo y no por corrección.'
  },
  redonda: {
    titulo: 'Redondo',
    texto: 'Largo y ancho parecidos, con contornos suaves en mandíbula y mejillas.',
    objetivo: 'Buscamos alargar visualmente: volumen arriba y costados más cortos.'
  },
  cuadrada: {
    titulo: 'Cuadrado',
    texto: 'Mandíbula marcada y casi tan ancha como los pómulos.',
    objetivo: 'Suavizar ángulos con textura, o aprovecharlos con cortes prolijos.'
  },
  alargada: {
    titulo: 'Alargado',
    texto: 'El largo del rostro supera claramente su ancho.',
    objetivo: 'Evitar volumen arriba y sumar ancho a los costados o con flequillo.'
  },
  corazon: {
    titulo: 'Corazón',
    texto: 'Frente amplia que se va angostando hacia un mentón fino.',
    objetivo: 'Equilibrar: menos volumen en la parte alta, más hacia la mandíbula.'
  },
  diamante: {
    titulo: 'Diamante',
    texto: 'Pómulos anchos con frente y mentón más angostos.',
    objetivo: 'Sumar ancho en frente y mentón, sin agregar volumen a los costados.'
  }
};

export const RECOMENDACIONES: Record<FormaRostro, Record<EstiloCorte, Recomendacion[]>> = {
  ovalada: {
    masculino: [
      { nombre: 'Slick back', descripcion: 'Peinado hacia atrás con costados degradados.' },
      { nombre: 'Crop texturizado', descripcion: 'Corto, con flequillo corto y textura arriba.' },
      { nombre: 'Medio largo', descripcion: 'Largo hasta la oreja con capas suaves.' }
    ],
    femenino: [
      { nombre: 'Bob clásico', descripcion: 'Recto a la altura de la mandíbula.' },
      { nombre: 'Largo en capas', descripcion: 'Capas largas que dan movimiento.' },
      { nombre: 'Pixie', descripcion: 'Corto y texturizado; resalta las facciones.' }
    ]
  },
  redonda: {
    masculino: [
      { nombre: 'Fade alto con quiff', descripcion: 'Costados muy cortos y volumen hacia arriba.' },
      { nombre: 'Pompadour', descripcion: 'Volumen en la parte superior que estiliza.' },
      { nombre: 'Undercut', descripcion: 'Contraste marcado entre costados y parte superior.' }
    ],
    femenino: [
      { nombre: 'Long bob asimétrico', descripcion: 'Más largo adelante, pasa la línea del mentón.' },
      { nombre: 'Capas largas', descripcion: 'Comienzan debajo del mentón para alargar.' },
      { nombre: 'Flequillo lateral', descripcion: 'Rompe la simetría redonda del rostro.' }
    ]
  },
  cuadrada: {
    masculino: [
      { nombre: 'Crew cut', descripcion: 'Corto y prolijo; aprovecha la mandíbula marcada.' },
      { nombre: 'Texturizado desordenado', descripcion: 'Suaviza los ángulos con movimiento.' },
      { nombre: 'Side part', descripcion: 'Raya al costado con degradado suave.' }
    ],
    femenino: [
      { nombre: 'Ondas suaves', descripcion: 'Las ondas contrarrestan los ángulos.' },
      { nombre: 'Capas desfiladas', descripcion: 'Enmarcan el rostro y suavizan la mandíbula.' },
      { nombre: 'Flequillo cortina', descripcion: 'Abierto al medio, suaviza la frente.' }
    ]
  },
  alargada: {
    masculino: [
      { nombre: 'Flequillo francés', descripcion: 'Flequillo recto que acorta visualmente la cara.' },
      { nombre: 'Costados con largo', descripcion: 'Evitar fades muy altos; dejar algo de largo al costado.' },
      { nombre: 'Buzz cut parejo', descripcion: 'Misma medida en toda la cabeza, sin volumen arriba.' }
    ],
    femenino: [
      { nombre: 'Bob a la mandíbula', descripcion: 'Suma ancho a la altura correcta.' },
      { nombre: 'Flequillo recto', descripcion: 'Reduce el largo visible del rostro.' },
      { nombre: 'Ondas a los costados', descripcion: 'Volumen lateral en lugar de arriba.' }
    ]
  },
  corazon: {
    masculino: [
      { nombre: 'Medio largo con textura', descripcion: 'Cubre algo de frente y suma peso abajo.' },
      { nombre: 'Flequillo lateral', descripcion: 'Disimula una frente amplia.' },
      { nombre: 'Barba completa', descripcion: 'Da ancho a la zona del mentón.' }
    ],
    femenino: [
      { nombre: 'Long bob', descripcion: 'Volumen a la altura del mentón.' },
      { nombre: 'Flequillo largo de lado', descripcion: 'Reduce visualmente la frente.' },
      { nombre: 'Ondas en las puntas', descripcion: 'Agrega volumen en la parte baja.' }
    ]
  },
  diamante: {
    masculino: [
      { nombre: 'Fringe texturizado', descripcion: 'Flequillo que ensancha la frente.' },
      { nombre: 'Side swept', descripcion: 'Peinado de costado con algo de largo.' },
      { nombre: 'Barba corta', descripcion: 'Da presencia a un mentón angosto.' }
    ],
    femenino: [
      { nombre: 'Bob con flequillo', descripcion: 'Agrega ancho en frente y mentón.' },
      { nombre: 'Capas detrás de la oreja', descripcion: 'Resalta pómulos sin ensanchar.' },
      { nombre: 'Largo con volumen en puntas', descripcion: 'Equilibra la parte baja del rostro.' }
    ]
  }
};
