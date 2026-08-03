import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { InstrumentoService } from '../../core/services/instrumento.service';
import { StatsResponse } from '../../core/models/instrumento.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  protected readonly auth = inject(AuthService);
  private readonly instrumentoService = inject(InstrumentoService);

  protected readonly stats = signal<StatsResponse | null>(null);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  constructor() {
    this.carregarStats();
  }

  private carregarStats(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.instrumentoService.obterEstatisticas().subscribe({
      next: (dados) => {
        this.stats.set(dados);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Não foi possível carregar as estatísticas.');
        this.isLoading.set(false);
      }
    });
  }

  protected sair(): void {
    this.auth.logout();
  }
}