import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpEventType } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { MigracaoResultado, MigracaoService } from '../../core/services/migracao.service';

@Component({
  selector: 'app-migracao',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './migracao.component.html',
  styleUrl: './migracao.component.scss'
})
export class MigracaoComponent {
  protected readonly auth = inject(AuthService);
  private readonly migracaoService = inject(MigracaoService);

  protected readonly nomeEmpresa = signal('');
  protected readonly progresso = signal(0);
  protected readonly enviando = signal(false);
  protected readonly resultado = signal<MigracaoResultado | null>(null);
  protected readonly errorMessage = signal<string | null>(null);
  protected arquivoSelecionado: File | null = null;

  protected onArquivoSelecionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    if (!this.nomeEmpresa().trim()) {
      this.errorMessage.set('Digite o nome da empresa antes de selecionar o arquivo.');
      input.value = '';
      return;
    }

    this.arquivoSelecionado = input.files[0];
    this.enviar();
  }

    private enviar(): void {
        if (!this.arquivoSelecionado) return;

        this.enviando.set(true);
        this.errorMessage.set(null);
        this.resultado.set(null);
        this.progresso.set(0);

        this.migracaoService.upload(this.arquivoSelecionado, this.nomeEmpresa()).subscribe({
        next: (evento) => {
            switch (evento.tipo) {
            case 'progresso':
                this.progresso.set(evento.percentual);
                break;
            case 'processando':
                this.progresso.set(100); // Ex: Arquivo subiu, agora o servidor está processando
                break;
            case 'concluido':
                this.resultado.set(evento.resultado);
                this.enviando.set(false);
                break;
            }
        },
        error: (err: any) => {
            this.enviando.set(false);
            this.errorMessage.set(
            err?.error?.error ?? 'Não foi possível migrar os dados. Verifique se o ZIP contém instrumentos.dbf.'
            );
        }
        });
    }

  protected reiniciar(): void {
    this.resultado.set(null);
    this.errorMessage.set(null);
    this.nomeEmpresa.set('');
    this.progresso.set(0);
    this.arquivoSelecionado = null;
  }

  protected sair(): void {
    this.auth.logout();
  }
}