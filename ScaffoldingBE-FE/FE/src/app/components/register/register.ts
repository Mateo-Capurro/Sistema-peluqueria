import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private auth = inject(AuthService);

  error = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    name: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    telefono: ['', [Validators.required]],
    dni: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  onSubmit(): void {
    if (this.form.invalid) return;
    this.error.set(null);
    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => {
        this.error.set(this.extractError(err));
      }
    });
  }

  private extractError(err: any): string {
    const body = err?.error;
    // errores de validación por campo -> mostrarlos concatenados
    if (body?.details && typeof body.details === 'object') {
      const msgs = Object.values(body.details) as string[];
      if (msgs.length) return msgs.join('. ');
    }
    return body?.message || 'Error al registrarse';
  }
}
