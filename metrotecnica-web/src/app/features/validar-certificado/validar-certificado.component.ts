import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ValidacaoService } from '../../core/services/validacao.service';
import { ValidacaoCertificado } from '../../core/models/validacao.model';

@Component({
  selector: 'app-validar-certificado',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './validar-certificado.component.html',
  styleUrl: './validar-certificado.component.scss'
})
export class ValidarCertificadoComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly validacaoService = inject(ValidacaoService);

  protected readonly isLoading = signal(true);
  protected readonly resultado = signal<ValidacaoCertificado | null>(null);

  constructor() {
    const hash = this.route.snapshot.paramMap.get('hash');

    if (!hash) {
      this.resultado.set(this.respostaInvalida());
      this.isLoading.set(false);
      return;
    }

    this.validacaoService.validar(hash).subscribe({
      next: (res) => {
        this.resultado.set(res);
        this.isLoading.set(false);
      },
      error: () => {
        this.resultado.set(this.respostaInvalida());
        this.isLoading.set(false);
      }
    });
  }

  private respostaInvalida(): ValidacaoCertificado {
    return {
      valido: false,
      identificacao: null,
      descricao: null,
      marca: null,
      modelo: null,
      tenantNome: null,
      dataCertificacao: null,
      proxCalibFormatada: null,
      statusGeral: null,
      metrologistaNome: null,
      responsavelNome: null,
      documentHashParcial: null
    };
  }
}