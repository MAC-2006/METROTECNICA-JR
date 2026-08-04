import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { PadroesService } from '../../core/services/padroes.service';

@Component({
  selector: 'app-padroes',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './padroes.component.html',
  styleUrl: './padroes.component.scss'
})
export class PadroesComponent {
  protected readonly auth = inject(AuthService);
  private readonly padroesService = inject(PadroesService);

  protected readonly arquivos = signal<string[]>([]);
  protected readonly busca = signal('');
  protected readonly isLoading = signal(true);
  protected readonly isAdmin = this.auth.isSuperAdmin();

  // upload (admin)
  protected readonly progresso = signal(0);
  protected readonly enviando = signal(false);
  protected readonly mensagemUpload = signal<string | null>(null);
  protected readonly erroUpload = signal<string | null>(null);

  constructor() {
    this.carregar();
  }

  private carregar(): void {
    this.isLoading.set(true);
    this.padroesService.listar().subscribe({
      next: (res) => { this.arquivos.set(res.arquivos); this.isLoading.set(false); },
      error: () => this.isLoading.set(false)
    });
  }

  protected pesquisar(termo: string): void {
    this.busca.set(termo);
    if (!termo.trim()) { this.carregar(); return; }
    this.padroesService.buscar(termo).subscribe((res) => this.arquivos.set(res.arquivos));
  }

  protected abrir(arquivo: string): void {
    window.open(this.padroesService.urlDownload(arquivo), '_blank');
  }

  protected onArquivoSelecionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const zip = input.files[0];
    this.enviando.set(true);
    this.progresso.set(0);
    this.mensagemUpload.set(null);
    this.erroUpload.set(null);

    this.padroesService.uploadZip(zip).subscribe({
      next: (evento) => {
        if (evento.tipo === 'progresso') this.progresso.set(evento.percentual);
        if (evento.tipo === 'processando') this.progresso.set(100);
        if (evento.tipo === 'concluido') {
          this.mensagemUpload.set(`${evento.resultado.count} PDF(s) importado(s) com sucesso.`);
          this.enviando.set(false);
          this.carregar();
        }
      },
      error: (err) => {
        this.enviando.set(false);
        this.erroUpload.set(err?.error?.error ?? 'Não foi possível importar o ZIP de padrões.');
      }
    });

    input.value = '';
  }

  protected sair(): void {
    this.auth.logout();
  }
}