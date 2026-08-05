import { Component, afterNextRender, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { RelatorioParams, RelatorioService } from '../../core/services/relatorio.service';

@Component({
  selector: 'app-relatorio-viewer',
  standalone: true,
  templateUrl: './relatorio-viewer.component.html',
  styleUrl: './relatorio-viewer.component.scss'
})
export class RelatorioViewerComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly relatorioService = inject(RelatorioService);
  private readonly sanitizer = inject(DomSanitizer);

  // Nascem vazios de propósito: no SSR não existe token (fica em localStorage,
  // inacessível no servidor), então não faz sentido montar essas URLs por lá.
  // Elas só são preenchidas de verdade em afterNextRender, que roda exclusivamente
  // no navegador, já com o token real do usuário logado.
  protected readonly pdfUrlSeguro = signal<SafeResourceUrl | null>(null);
  protected readonly pdfDownloadUrl = signal<string | null>(null);
  protected readonly excelDownloadUrl = signal<string | null>(null);

  constructor() {
    const p = this.route.snapshot.queryParams;
    const params: RelatorioParams = {
      tipo: p['tipo'] ?? 'geral',
      start: p['start'] ?? null,
      end: p['end'] ?? null,
      valor: p['valor'] ?? null
    };

    afterNextRender(() => {
      const pdfUrl = this.relatorioService.construirUrlPdf(params, false);
      this.pdfUrlSeguro.set(this.sanitizer.bypassSecurityTrustResourceUrl(pdfUrl));
      this.pdfDownloadUrl.set(this.relatorioService.construirUrlPdf(params, true));
      this.excelDownloadUrl.set(this.relatorioService.construirUrlExcel(params));
    });
  }
}