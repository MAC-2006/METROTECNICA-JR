import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { InstrumentoService } from '../../core/services/instrumento.service';
import { InstrumentoListItem } from '../../core/models/instrumento.model';

@Component({
  selector: 'app-instrumentos-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './instrumentos-list.component.html',
  styleUrl: './instrumentos-list.component.scss'
})
export class InstrumentosListComponent {
  protected readonly auth = inject(AuthService);
  private readonly instrumentoService = inject(InstrumentoService);

  protected readonly instrumentos = signal<InstrumentoListItem[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly paginaAtual = signal(1);
  protected readonly totalPaginas = signal(1);

  protected readonly search = signal('');
  protected readonly year = signal<number | null>(null);
  protected readonly month = signal<number | null>(null);

  protected readonly meses = [
    { valor: 1, nome: 'Janeiro' }, { valor: 2, nome: 'Fevereiro' }, { valor: 3, nome: 'Março' },
    { valor: 4, nome: 'Abril' }, { valor: 5, nome: 'Maio' }, { valor: 6, nome: 'Junho' },
    { valor: 7, nome: 'Julho' }, { valor: 8, nome: 'Agosto' }, { valor: 9, nome: 'Setembro' },
    { valor: 10, nome: 'Outubro' }, { valor: 11, nome: 'Novembro' }, { valor: 12, nome: 'Dezembro' }
  ];

  protected readonly anos: number[] = Array.from({ length: 21 }, (_, i) => 2015 + i);

  private searchTimeout?: ReturnType<typeof setTimeout>;

  constructor() {
    this.carregar(1);
  }

  protected onSearchInput(valor: string): void {
    this.search.set(valor);
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => this.carregar(1), 400);
  }

  protected onFiltroChange(): void {
    this.carregar(1);
  }

  protected limparFiltros(): void {
    this.search.set('');
    this.year.set(null);
    this.month.set(null);
    this.carregar(1);
  }

  protected irParaPagina(pagina: number): void {
    if (pagina < 1 || pagina > this.totalPaginas()) return;
    this.carregar(pagina);
  }

  private carregar(pagina: number): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.instrumentoService.listar({
      page: pagina,
      search: this.search() || undefined,
      year: this.year() ?? undefined,
      month: this.month() ?? undefined
    }).subscribe({
      next: (res) => {
        this.instrumentos.set(res.instrumentos);
        this.paginaAtual.set(res.paginaAtual);
        this.totalPaginas.set(res.totalPaginas);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Não foi possível carregar os instrumentos.');
        this.isLoading.set(false);
      }
    });
  }

  protected sair(): void {
    this.auth.logout();
  }
}