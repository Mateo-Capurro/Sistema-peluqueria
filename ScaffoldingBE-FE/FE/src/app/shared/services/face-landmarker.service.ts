import { Injectable } from '@angular/core';
import type { FaceLandmarker } from '@mediapipe/tasks-vision';
import { PuntoFacial } from '../models/rostro.model';

// Los .wasm se copian desde node_modules en el build (ver angular.json → assets)
const WASM_URL = '/mediapipe/wasm';
const MODEL_URL =
  'https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task';

/**
 * Envuelve MediaPipe Face Landmarker. Se carga de forma diferida (la primera vez
 * que se usa) para no sumar peso al bundle inicial. Todo corre en el navegador:
 * la foto del cliente nunca se envía al backend.
 */
@Injectable({ providedIn: 'root' })
export class FaceLandmarkerService {
  private landmarker: Promise<FaceLandmarker> | null = null;

  private cargar(): Promise<FaceLandmarker> {
    if (!this.landmarker) {
      this.landmarker = (async () => {
        const { FaceLandmarker, FilesetResolver } = await import('@mediapipe/tasks-vision');
        const vision = await FilesetResolver.forVisionTasks(WASM_URL);
        return FaceLandmarker.createFromOptions(vision, {
          baseOptions: { modelAssetPath: MODEL_URL, delegate: 'GPU' },
          runningMode: 'IMAGE',
          numFaces: 2 // pedimos 2 para poder avisar si hay más de una cara
        });
      })().catch((e) => {
        this.landmarker = null; // permitir reintento
        throw e;
      });
    }
    return this.landmarker;
  }

  /** Precarga el modelo (opcional, para que el análisis sea instantáneo). */
  precargar(): void {
    this.cargar().catch(() => void 0);
  }

  /** Devuelve los landmarks de cada cara detectada en la imagen. */
  async detectar(img: HTMLCanvasElement | HTMLImageElement): Promise<PuntoFacial[][]> {
    const lm = await this.cargar();
    return lm.detect(img).faceLandmarks;
  }
}
