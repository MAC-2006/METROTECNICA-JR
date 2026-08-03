import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';

interface GaugeTick {
  x1: number;
  y1: number;
  x2: number;
  y2: number;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly showPassword = signal(false);

  protected readonly gaugeTicks: GaugeTick[] = Array.from({ length: 9 }, (_, i) => {
    const angleDeg = -120 + i * 30;
    const rad = (angleDeg * Math.PI) / 180;
    return {
      x1: 100 + 68 * Math.cos(rad),
      y1: 100 + 68 * Math.sin(rad),
      x2: 100 + 82 * Math.cos(rad),
      y2: 100 + 82 * Math.sin(rad)
    };
  });

  protected togglePassword(): void {
    this.showPassword.update((visible) => !visible);
  }

  protected submit(): void {
    if (this.form.invalid || this.isLoading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { email, password } = this.form.getRawValue();

    this.authService.login(email, password).subscribe({
      next: () => {
        // Super-admin (sem tenant vinculado) cai na gestão de empresas;
        // qualquer outro usuário vai direto para o painel de instrumentos.
        const destino = this.authService.isSuperAdmin() ? '/admin-dashboard' : '/dashboard';
        this.router.navigateByUrl(destino);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.errorMessage.set(
          err.status === 401
            ? 'E-mail ou senha inválidos.'
            : 'Não foi possível conectar ao servidor. Tente novamente.'
        );
      }
    });
  }
}