import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AuxiliarService } from '../../core/services/auxiliar.service';
import { Auxiliar } from '../../core/models/auxiliar.model';

@Component({
  selector: 'app-relatorios',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './relatorios.component.html',
  styleUrl: './relatorios.component.scss'
})
export class RelatoriosComponent {
  protected readonly auth = inject(AuthService);
  private readonly auxiliarService = inject(AuxiliarService);
  private readonly router = inject(Router);

  protected readonly tipo = signal('geral');
  protected readonly start = signal('');
  protected readonly end = signal('');
  protected readonly valor = signal<string | null>(null);

  protected readonly setores = signal<Auxiliar[]>([]);
  protected readonly locais = signal<Auxiliar[]>([]);

  protected readonly tipos = [
    { valor: 'geral', nome: 'Relação Geral de Ativos' },
    { valor: 'vencidos', nome: 'Instrumentos Vencidos' },
    { valor: 'proximas', nome: 'Vencimentos por Período' },
    { valor: 'setor', nome: 'Por Setor Específico' },
    { valor: 'local', nome: 'Por Local de Uso' },
    { valor: 'situacao', nome: 'Por Situação (Status)' },
    { valor: 'cadastro', nome: 'Por Data de Cadastro' }
  ];

  protected readonly situacoes = ['APROVADO', 'REPROVADO', 'FORA DE USO', 'EXTRAVIADO'];

  protected readonly precisaDatas = computed(() => ['proximas', 'cadastro'].includes(this.tipo()));
  protected readonly precisaSetor = computed(() => this.tipo() === 'setor');
  protected readonly precisaLocal = computed(() => this.tipo() === 'local');
  protected readonly precisaSituacao = computed(() => this.tipo() === 'situacao');

  constructor() {
    this.auxiliarService.listar('setores').subscribe((s) => this.setores.set(s));
    this.auxiliarService.listar('locais-uso').subscribe((l) => this.locais.set(l));
  }

  protected onTipoChange(valor: string): void {
    this.tipo.set(valor);
    this.valor.set(null);
  }

  protected gerar(): void {
    const queryParams: Record<string, string> = { tipo: this.tipo() };

    if (this.precisaDatas()) {
      if (this.start()) queryParams['start'] = this.start();
      if (this.end()) queryParams['end'] = this.end();
    }
    if ((this.precisaSetor() || this.precisaLocal() || this.precisaSituacao()) && this.valor()) {
      queryParams['valor'] = this.valor()!;
    }

    const url = this.router.serializeUrl(
      this.router.createUrlTree(['/relatorios/visualizar'], { queryParams })
    );
    window.open(url, '_blank');
  }

  protected sair(): void {
    this.auth.logout();
  }
}