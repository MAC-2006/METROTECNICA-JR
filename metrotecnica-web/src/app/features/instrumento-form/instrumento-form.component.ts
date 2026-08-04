import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { InstrumentoService } from '../../core/services/instrumento.service';
import { AuxiliarService } from '../../core/services/auxiliar.service';
import { Auxiliar } from '../../core/models/auxiliar.model';
import {
  HistoricoRetroativo,
  InstrumentoRequest,
  PontoCalibracao
} from '../../core/models/instrumento.model';

@Component({
  selector: 'app-instrumento-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './instrumento-form.component.html',
  styleUrl: './instrumento-form.component.scss'
})
export class InstrumentoFormComponent {
  protected readonly auth = inject(AuthService);
  private readonly instrumentoService = inject(InstrumentoService);
  private readonly auxiliarService = inject(AuxiliarService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly instrumentoId = signal<number | null>(null);
  protected readonly isEdicao = signal(false);
  protected readonly isSalvando = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly setores = signal<Auxiliar[]>([]);
  protected readonly locais = signal<Auxiliar[]>([]);

  protected readonly form = signal<InstrumentoRequest>(this.formVazio());

  protected readonly pontos = signal<PontoCalibracao[]>([]);
  protected readonly historicos = signal<HistoricoRetroativo[]>([]);

  protected readonly statusOptions = [
    'APROVADO', 'REPROVADO', 'EXTRAVIADO', 'FORA DE USO', 'MANUTENÇÃO', 'NÃO REQUER CALIBR.', 'APROVADO RESTRIÇÃO'
  ];

  constructor() {
    this.auxiliarService.listar('setores').subscribe((s) => this.setores.set(s));
    this.auxiliarService.listar('locais-uso').subscribe((l) => this.locais.set(l));

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.instrumentoId.set(id);
      this.isEdicao.set(true);
      this.carregarInstrumento(id);
    }
  }

  private formVazio(): InstrumentoRequest {
    return {
      numeroSequencial: null,
      identificacao: '',
      descricao: '',
      marca: null,
      modelo: null,
      capacidade: null,
      precisao: null,
      setorId: null,
      localId: null,
      frequenciaMeses: 12,
      dataCertificacao: null,
      certificado: null,
      statusGeral: null,
      observacoes: null,
      lpMin: null,
      lpMax: null,
      aferidoLp: null,
      lnpMin: null,
      lnpMax: null,
      aferidoLnp: null,
      erro: null,
      incertezaMedicao: null,
      criterioAceitacao: null,
      pontos: [],
      historicosRetroativos: []
    };
  }
  private paraIso(dataBr: string | null): string | null {
    if (!dataBr || dataBr === '---') return null;
    const [dia, mes, ano] = dataBr.split('/');
    if (!dia || !mes || !ano) return null;
    return `${ano}-${mes}-${dia}`;
  }
  private carregarInstrumento(id: number): void {
    this.instrumentoService.obterPorId(id).subscribe({
      next: (inst) => {
        this.form.set({
          numeroSequencial: inst.numeroSequencial,
          identificacao: inst.identificacao,
          descricao: inst.descricao,
          marca: inst.marca,
          modelo: inst.modelo,
          capacidade: inst.capacidade,
          precisao: inst.precisao,
          setorId: inst.setorId,
          localId: inst.localId,
          frequenciaMeses: inst.frequenciaMeses,
          dataCertificacao: inst.dataCertificacao,
          certificado: inst.certificado,
          statusGeral: inst.statusGeral,
          observacoes: inst.observacoes,
          lpMin: inst.lpMin,
          lpMax: inst.lpMax,
          aferidoLp: inst.aferidoLp,
          lnpMin: inst.lnpMin,
          lnpMax: inst.lnpMax,
          aferidoLnp: inst.aferidoLnp,
          erro: inst.erro,
          incertezaMedicao: inst.incertezaMedicao,
          criterioAceitacao: inst.criterioAceitacao,
          pontos: [],
          historicosRetroativos: []
        });
        this.pontos.set(inst.pontos ?? []);
        this.historicos.set(
          (inst.historicos ?? []).map((h) => ({
            id: h.id,
            certificado: h.certificado,
            dataCertificacao: this.paraIso(h.data)
          }))
        );
        this.assinaturaInfo.set({
          documentHash: inst.documentHash,
          metrologistaNome: inst.metrologistaNome
        });
      },
      error: () => this.errorMessage.set('Não foi possível carregar o instrumento.')
    });
  }
  protected assinar(): void {
    const id = this.instrumentoId();
    if (!id) return;

    this.isAssinando.set(true);
    this.errorMessage.set(null);

    this.instrumentoService.assinar(id).subscribe({
      next: () => {
        this.carregarInstrumento(id);
        this.isAssinando.set(false);
      },
      error: (err) => {
        this.isAssinando.set(false);
        this.errorMessage.set(err?.error?.error ?? 'Não foi possível assinar o certificado.');
      }
    });
  }
  protected atualizarCampo<K extends keyof InstrumentoRequest>(campo: K, valor: InstrumentoRequest[K]): void {
    this.form.update((f) => ({ ...f, [campo]: valor }));
  }

  protected adicionarPonto(): void {
    this.pontos.update((p) => [...p, { pontoNominal: '', vIndicado1: 0, vIndicado2: 0 }]);
  }

  protected removerPonto(index: number): void {
    this.pontos.update((p) => p.filter((_, i) => i !== index));
  }

  protected atualizarPonto(index: number, campo: keyof PontoCalibracao, valor: string | number): void {
    this.pontos.update((p) =>
      p.map((ponto, i) => (i === index ? { ...ponto, [campo]: valor } : ponto))
    );
  }

  protected adicionarHistorico(): void {
    this.historicos.update((h) => [...h, { certificado: '', dataCertificacao: null }]);
  }

  protected removerHistorico(index: number): void {
    this.historicos.update((h) => h.filter((_, i) => i !== index));
  }

  protected atualizarHistorico(index: number, campo: keyof HistoricoRetroativo, valor: string | null): void {
    this.historicos.update((h) =>
      h.map((item, i) => (i === index ? { ...item, [campo]: valor } : item))
    );
  }

  protected salvar(): void {
    const dados = this.form();

    if (!dados.identificacao.trim() || !dados.descricao.trim()) {
      this.errorMessage.set('Identificação (TAG) e descrição são obrigatórias.');
      return;
    }

    this.isSalvando.set(true);
    this.errorMessage.set(null);

    const payload: InstrumentoRequest = {
      ...dados,
      pontos: this.pontos(),
      historicosRetroativos: this.historicos()
    };

    const request$ = this.isEdicao()
      ? this.instrumentoService.atualizar(this.instrumentoId()!, payload)
      : this.instrumentoService.criar(payload);

    request$.subscribe({
      next: () => this.router.navigateByUrl('/instrumentos'),
      error: (err) => {
        this.isSalvando.set(false);
        this.errorMessage.set(err?.error?.error ?? 'Não foi possível salvar o instrumento.');
      }
    });
  }
  // junto dos outros signals
  protected readonly isAssinando = signal(false);
  protected readonly assinaturaInfo = signal<{ documentHash: string | null; metrologistaNome: string | null } | null>(null);
  protected sair(): void {
    this.auth.logout();
  }
}