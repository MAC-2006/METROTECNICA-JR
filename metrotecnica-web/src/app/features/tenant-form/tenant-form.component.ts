import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TenantService } from '../../core/services/tenant.service';
import { ConsultaExternaService } from '../../core/services/consulta-externa.service';
import { TenantRequest } from '../../core/models/tenant.model';

@Component({
  selector: 'app-tenant-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './tenant-form.component.html',
  styleUrl: './tenant-form.component.scss'
})
export class TenantFormComponent {
  protected readonly auth = inject(AuthService);
  private readonly tenantService = inject(TenantService);
  private readonly consultaExterna = inject(ConsultaExternaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly tenantId = signal<number | null>(null);
  protected readonly isEdicao = signal(false);
  protected readonly isSalvando = signal(false);
  protected readonly isCarregando = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = signal<TenantRequest>(this.formVazio());

  // Estado das buscas automáticas de CNPJ e CEP.
  protected readonly buscandoCnpj = signal(false);
  protected readonly buscandoCep = signal(false);
  protected readonly cnpjNaoEncontrado = signal(false);
  protected readonly cepNaoEncontrado = signal(false);

  // Evita repetir a mesma busca várias vezes enquanto o usuário digita.
  private ultimoCnpjBuscado: string | null = null;
  private ultimoCepBuscado: string | null = null;

  constructor() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.tenantId.set(id);
      this.isEdicao.set(true);
      this.carregarTenant(id);
    }
  }

  private formVazio(): TenantRequest {
    return {
      name: '',
      razaoSocial: null,
      cnpj: null,
      logradouro: null,
      numero: null,
      bairro: null,
      cidade: null,
      estado: null,
      cep: null,
      telefone: null,
      slug: '',
      url: null
    };
  }

  private carregarTenant(id: number): void {
    this.isCarregando.set(true);
    this.tenantService.obterPorId(id).subscribe({
      next: (t) => {
        this.form.set({
          name: t.name,
          razaoSocial: t.razaoSocial,
          cnpj: t.cnpj,
          logradouro: t.logradouro,
          numero: t.numero,
          bairro: t.bairro,
          cidade: t.cidade,
          estado: t.estado,
          cep: t.cep,
          telefone: t.telefone,
          slug: t.slug,
          url: t.url
        });
        this.isCarregando.set(false);
      },
      error: () => {
        this.errorMessage.set('Não foi possível carregar a empresa.');
        this.isCarregando.set(false);
      }
    });
  }

  protected atualizarCampo<K extends keyof TenantRequest>(campo: K, valor: TenantRequest[K]): void {
    this.form.update((f) => ({ ...f, [campo]: valor }));

    if (campo === 'cnpj') this.aoDigitarCnpj(valor as string | null);
    if (campo === 'cep') this.aoDigitarCep(valor as string | null);
  }

  /** Dispara a busca na BrasilAPI assim que o CNPJ tiver 14 dígitos. */
  private aoDigitarCnpj(valor: string | null): void {
    const digitos = (valor ?? '').replace(/\D/g, '');
    this.cnpjNaoEncontrado.set(false);

    if (digitos.length !== 14 || digitos === this.ultimoCnpjBuscado) return;
    this.ultimoCnpjBuscado = digitos;
    this.buscandoCnpj.set(true);

    this.consultaExterna.buscarCnpj(digitos).subscribe({
      next: (dados) => {
        this.form.update((f) => ({
          ...f,
          razaoSocial: dados.razaoSocial || f.razaoSocial,
          logradouro: dados.logradouro ?? f.logradouro,
          numero: dados.numero ?? f.numero,
          bairro: dados.bairro ?? f.bairro,
          cidade: dados.cidade ?? f.cidade,
          estado: dados.estado ?? f.estado,
          cep: dados.cep ?? f.cep,
          telefone: dados.telefone ?? f.telefone
        }));

        // Preenche nome/slug só se ainda estiverem vazios, reaproveitando a
        // mesma lógica de geração de slug do campo de nome.
        if (!this.form().name.trim()) {
          this.onNomeChange(dados.nomeFantasia || dados.razaoSocial);
        }

        this.buscandoCnpj.set(false);
      },
      error: () => {
        this.buscandoCnpj.set(false);
        this.cnpjNaoEncontrado.set(true);
      }
    });
  }

  /** Dispara a busca no ViaCEP assim que o CEP tiver 8 dígitos. */
  private aoDigitarCep(valor: string | null): void {
    const digitos = (valor ?? '').replace(/\D/g, '');
    this.cepNaoEncontrado.set(false);

    if (digitos.length !== 8 || digitos === this.ultimoCepBuscado) return;
    this.ultimoCepBuscado = digitos;
    this.buscandoCep.set(true);

    this.consultaExterna.buscarCep(digitos).subscribe({
      next: (dados) => {
        this.form.update((f) => ({
          ...f,
          logradouro: dados.logradouro ?? f.logradouro,
          bairro: dados.bairro ?? f.bairro,
          cidade: dados.cidade ?? f.cidade,
          estado: dados.estado ?? f.estado
        }));
        this.buscandoCep.set(false);
      },
      error: () => {
        this.buscandoCep.set(false);
        this.cepNaoEncontrado.set(true);
      }
    });
  }

  /** Gera o slug automaticamente a partir do nome, só enquanto o usuário não editou o slug manualmente. */
  protected onNomeChange(valor: string): void {
    const slugAtual = this.form().slug;
    const slugAutoAnterior = this.gerarSlug(this.form().name);
    const usuarioEditouSlugManualmente = slugAtual !== '' && slugAtual !== slugAutoAnterior;

    this.form.update((f) => ({
      ...f,
      name: valor,
      slug: usuarioEditouSlugManualmente ? f.slug : this.gerarSlug(valor)
    }));
  }

  private gerarSlug(nome: string): string {
    return nome
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/(^-|-$)/g, '');
  }

  protected salvar(): void {
    const dados = this.form();

    if (!dados.name.trim() || !dados.slug.trim()) {
      this.errorMessage.set('Nome e slug são obrigatórios.');
      return;
    }

    this.isSalvando.set(true);
    this.errorMessage.set(null);

    const request$ = this.isEdicao()
      ? this.tenantService.atualizar(this.tenantId()!, dados)
      : this.tenantService.criar(dados);

    request$.subscribe({
      next: () => this.router.navigateByUrl('/admin-dashboard'),
      error: (err) => {
        this.isSalvando.set(false);
        this.errorMessage.set(err?.error?.error ?? 'Não foi possível salvar a empresa.');
      }
    });
  }

  protected sair(): void {
    this.auth.logout();
  }
}