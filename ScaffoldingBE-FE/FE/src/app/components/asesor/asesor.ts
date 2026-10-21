import { Component, ElementRef, OnDestroy, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { Router } from '@angular/router';
import { FaceLandmarkerService } from '../../shared/services/face-landmarker.service';
import { TratamientoService } from '../../shared/services/tratamiento.service';
import { Tratamiento } from '../../shared/models/tratamiento.model';
import { EstiloCorte, PuntoFacial, ResultadoAnalisis } from '../../shared/models/rostro.model';
import { DESCRIPCION_FORMA, LM, RECOMENDACIONES, analizarRostro } from '../../shared/utils/analisis-rostro';

type Paso = 'inicio' | 'camara' | 'analizando' | 'resultado';

const MAX_LADO = 1024;

@Component({
  selector: 'app-asesor',
  imports: [],
  templateUrl: './asesor.html'
})
export class Asesor implements OnInit, OnDestroy {
  private faceService = inject(FaceLandmarkerService);
  private tratamientoService = inject(TratamientoService);
  private router = inject(Router);

  private video = viewChild<ElementRef<HTMLVideoElement>>('video');
  private canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');

  paso = signal<Paso>('inicio');
  estilo = signal<EstiloCorte>('masculino');
  error = signal<string | null>(null);
  resultado = signal<ResultadoAnalisis | null>(null);
  tratamientos = signal<Tratamiento[]>([]);

  private stream: MediaStream | null = null;

  descripcion = computed(() => {
    const r = this.resultado();
    return r ? DESCRIPCION_FORMA[r.forma] : null;
  });

  recomendaciones = computed(() => {
    const r = this.resultado();
    return r ? RECOMENDACIONES[r.forma][this.estilo()] : [];
  });

  /** Tratamiento "Corte..." del catálogo para ofrecer reservar directo. */
  tratamientoCorte = computed(() =>
    this.tratamientos().find((t) => t.nombre.toLowerCase().includes('corte')) ?? null
  );

  ngOnInit(): void {
    this.faceService.precargar();
    this.tratamientoService.list().subscribe({
      next: (t) => this.tratamientos.set(t),
      error: () => void 0
    });
  }

  ngOnDestroy(): void {
    this.detenerCamara();
  }

  // ── Cámara ─────────────────────────────────────────────
  async abrirCamara(): Promise<void> {
    this.error.set(null);
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'user', width: { ideal: 1280 }, height: { ideal: 720 } },
        audio: false
      });
      this.paso.set('camara');
      // el <video> siempre está en el DOM, solo se oculta
      const v = this.video()!.nativeElement;
      v.srcObject = this.stream;
      await v.play();
    } catch {
      this.error.set('No pudimos acceder a la cámara. Revisá los permisos o subí una foto.');
      this.paso.set('inicio');
    }
  }

  detenerCamara(): void {
    this.stream?.getTracks().forEach((t) => t.stop());
    this.stream = null;
  }

  capturar(): void {
    const v = this.video()!.nativeElement;
    const c = this.canvas().nativeElement;
    const escala = Math.min(1, MAX_LADO / Math.max(v.videoWidth, v.videoHeight));
    c.width = Math.round(v.videoWidth * escala);
    c.height = Math.round(v.videoHeight * escala);
    const ctx = c.getContext('2d')!;
    // espejo, igual que lo que el usuario ve en la vista previa
    ctx.save();
    ctx.translate(c.width, 0);
    ctx.scale(-1, 1);
    ctx.drawImage(v, 0, 0, c.width, c.height);
    ctx.restore();
    this.detenerCamara();
    this.analizar();
  }

  // ── Subir archivo ──────────────────────────────────────
  subirFoto(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) return;
    this.error.set(null);

    const url = URL.createObjectURL(file);
    const img = new Image();
    img.onload = () => {
      const c = this.canvas().nativeElement;
      const escala = Math.min(1, MAX_LADO / Math.max(img.width, img.height));
      c.width = Math.round(img.width * escala);
      c.height = Math.round(img.height * escala);
      c.getContext('2d')!.drawImage(img, 0, 0, c.width, c.height);
      URL.revokeObjectURL(url);
      this.analizar();
    };
    img.onerror = () => {
      URL.revokeObjectURL(url);
      this.error.set('No se pudo leer la imagen.');
    };
    img.src = url;
  }

  // ── Análisis ───────────────────────────────────────────
  private async analizar(): Promise<void> {
    this.paso.set('analizando');
    const c = this.canvas().nativeElement;
    try {
      const caras = await this.faceService.detectar(c);
      if (caras.length === 0) {
        return this.fallar('No detectamos ninguna cara. Probá con más luz y de frente.');
      }
      if (caras.length > 1) {
        return this.fallar('Hay más de una persona en la foto. Sacala solo/a.');
      }
      const r = analizarRostro(caras[0], c.width, c.height);
      if (!r.postura.ok) {
        return this.fallar(r.postura.motivo!);
      }
      this.dibujarGuias(caras[0]);
      this.resultado.set(r);
      this.paso.set('resultado');
    } catch {
      this.fallar('No se pudo cargar el analizador. Verificá tu conexión e intentá de nuevo.');
    }
  }

  private fallar(msg: string): void {
    this.error.set(msg);
    this.resultado.set(null);
    this.paso.set('inicio');
  }

  /** Dibuja sobre la foto las líneas que se midieron. */
  private dibujarGuias(p: PuntoFacial[]): void {
    const c = this.canvas().nativeElement;
    const ctx = c.getContext('2d')!;
    const W = c.width, H = c.height;
    ctx.lineWidth = Math.max(2, W / 300);
    ctx.strokeStyle = '#C9A96E';
    ctx.fillStyle = '#C9A96E';

    const linea = (a: number, b: number) => {
      ctx.beginPath();
      ctx.moveTo(p[a].x * W, p[a].y * H);
      ctx.lineTo(p[b].x * W, p[b].y * H);
      ctx.stroke();
      for (const i of [a, b]) {
        ctx.beginPath();
        ctx.arc(p[i].x * W, p[i].y * H, ctx.lineWidth * 1.8, 0, Math.PI * 2);
        ctx.fill();
      }
    };
    linea(LM.frenteSuperior, LM.menton);
    linea(LM.frenteIzq, LM.frenteDer);
    linea(LM.pomuloIzq, LM.pomuloDer);
    linea(LM.mandibulaIzq, LM.mandibulaDer);
  }

  porcentaje(v: number): string {
    return Math.round(v * 100) + '%';
  }

  reiniciar(): void {
    this.detenerCamara();
    this.resultado.set(null);
    this.error.set(null);
    this.paso.set('inicio');
  }

  reservar(): void {
    const t = this.tratamientoCorte();
    this.router.navigate(['/reservar'], t ? { queryParams: { tratamiento: t.id } } : {});
  }
}
