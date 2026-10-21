import { describe, expect, it } from 'vitest';
import { MedidasRostro, PuntoFacial } from '../models/rostro.model';
import { LM, RECOMENDACIONES, calcularMedidas, clasificarForma, evaluarPostura } from './analisis-rostro';

function medidas(r: number, f: number, j: number): MedidasRostro {
  return { largo: r, anchoFrente: f, anchoPomulos: 1, anchoMandibula: j,
           ratioLargoAncho: r, ratioFrente: f, ratioMandibula: j };
}

/** Malla falsa con los puntos relevantes ubicados a mano (coords normalizadas). */
function malla(cfg: { largo: number; frente: number; pomulos: number; mandibula: number; inclinacion?: number }) {
  const p: PuntoFacial[] = Array.from({ length: 478 }, () => ({ x: 0.5, y: 0.5 }));
  const cx = 0.5;
  p[LM.frenteSuperior] = { x: cx, y: 0.5 - cfg.largo / 2 };
  p[LM.menton] = { x: cx, y: 0.5 + cfg.largo / 2 };
  p[LM.frenteIzq] = { x: cx - cfg.frente / 2, y: 0.3 };
  p[LM.frenteDer] = { x: cx + cfg.frente / 2, y: 0.3 };
  p[LM.pomuloIzq] = { x: cx - cfg.pomulos / 2, y: 0.5 };
  p[LM.pomuloDer] = { x: cx + cfg.pomulos / 2, y: 0.5 };
  p[LM.mandibulaIzq] = { x: cx - cfg.mandibula / 2, y: 0.7 };
  p[LM.mandibulaDer] = { x: cx + cfg.mandibula / 2, y: 0.7 };
  p[LM.ojoExtIzq] = { x: 0.4, y: 0.42 };
  p[LM.ojoExtDer] = { x: 0.6, y: 0.42 + (cfg.inclinacion ?? 0) };
  p[LM.punta_nariz] = { x: cx, y: 0.55 };
  return p;
}

describe('clasificarForma', () => {
  it.each([
    [medidas(1.55, 0.85, 0.80), 'alargada'],
    [medidas(1.10, 0.95, 0.70), 'redonda'], // corta: no debe caer en corazón
    [medidas(1.15, 0.85, 0.75), 'redonda'],
    [medidas(1.15, 0.90, 0.90), 'cuadrada'],
    [medidas(1.30, 0.95, 0.70), 'corazon'],
    [medidas(1.30, 0.72, 0.68), 'diamante'],
    [medidas(1.30, 0.86, 0.80), 'ovalada']
  ])('caso %# → %s', (m, esperado) => {
    expect(clasificarForma(m)).toBe(esperado);
  });
});

describe('calcularMedidas', () => {
  it('corrige imágenes no cuadradas usando ancho/alto en píxeles', () => {
    const p = malla({ largo: 0.4, frente: 0.3, pomulos: 0.3, mandibula: 0.25 });
    // imagen 4:3 → el ancho normalizado vale más píxeles que el alto
    const m = calcularMedidas(p, 800, 600);
    expect(m.largo).toBeCloseTo(240);
    expect(m.anchoPomulos).toBeCloseTo(240);
    expect(m.ratioLargoAncho).toBeCloseTo(1);
  });
});

describe('evaluarPostura', () => {
  it('acepta una cara derecha y de frente', () => {
    const p = malla({ largo: 0.4, frente: 0.3, pomulos: 0.3, mandibula: 0.25 });
    expect(evaluarPostura(p, 1000, 1000).ok).toBe(true);
  });

  it('rechaza la cabeza inclinada', () => {
    const p = malla({ largo: 0.4, frente: 0.3, pomulos: 0.3, mandibula: 0.25, inclinacion: 0.08 });
    expect(evaluarPostura(p, 1000, 1000).ok).toBe(false);
  });

  it('rechaza la cara girada', () => {
    const p = malla({ largo: 0.4, frente: 0.3, pomulos: 0.3, mandibula: 0.25 });
    p[LM.punta_nariz] = { x: 0.6, y: 0.55 };
    expect(evaluarPostura(p, 1000, 1000).ok).toBe(false);
  });
});

describe('RECOMENDACIONES', () => {
  it('tiene sugerencias para cada forma y estilo', () => {
    for (const forma of Object.values(RECOMENDACIONES)) {
      expect(forma.masculino.length).toBeGreaterThan(0);
      expect(forma.femenino.length).toBeGreaterThan(0);
    }
  });
});
