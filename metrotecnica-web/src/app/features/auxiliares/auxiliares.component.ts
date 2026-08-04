import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AuxiliarService, TipoAuxiliar } from '../../core/services/auxiliar.service';
import { Auxiliar } from '../../core/models/auxiliar.model';

@Component({
  selector: 'app-auxiliares',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './auxiliares.component.html',
  styleUrl: './auxiliares.component.scss'
})
export class AuxiliaresComponent {
  protected readonly auth = inject(AuthService);
  private readonly auxiliarService = inject(AuxiliarService);

  protected readonly setores = signal<Auxiliar[]>([]);
  protected readonly locais = signal<Auxiliar[]>([]);
  protected readonly novoSetor = signal('');
  protected readonly novoLocal = signal('');
  protected readonly errorMessage = signal<string | null>(null);

  constructor() {
    this.carregar('setores');
    this.carregar('locais-uso');
  }

  private carregar(tipo: TipoAuxiliar): void {
    this.auxiliarService.listar(tipo).subscribe({
      next: (dados) => tipo === 'setores' ? this.setores.set(dados) : this.locais.set(dados),
      error: () => this.errorMessage.set('Não foi possível carregar os dados.')
    });
  }

  protected adicionar(tipo: TipoAuxiliar): void {
    const nome = tipo === 'setores' ? this.novoSetor().trim() : this.novoLocal().trim();
    if (!nome) return;

    this.auxiliarService.criar(tipo, nome).subscribe({
      next: () => {
        tipo === 'setores' ? this.novoSetor.set('') : this.novoLocal.set('');
        this.carregar(tipo);
      },
      error: (err) => this.errorMessage.set(err?.error?.error ?? 'Erro ao adicionar item.')
    });
  }

  protected remover(tipo: TipoAuxiliar, item: Auxiliar): void {
    if (!confirm(`Remover "${item.nome}"?`)) return;

    this.auxiliarService.deletar(tipo, item.id).subscribe({
      next: () => this.carregar(tipo),
      error: () => this.errorMessage.set('Não foi possível remover — pode estar em uso por algum instrumento.')
    });
  }

  protected sair(): void {
    this.auth.logout();
  }
}