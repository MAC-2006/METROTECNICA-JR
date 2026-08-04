import { Component, inject } from '@angular/core';
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

  protected readonly pdfUrlSeguro: SafeResourceUrl;
  protected readonly pdfDownloadUrl: string;
  protected readonly excelDownloadUrl: string;

  constructor() {
    const p = this.route.snapshot.queryParams;
    const params: RelatorioParams = {
      tipo: p['tipo'] ?? 'geral',
      start: p['start'] ?? null,
      end: p['end'] ?? null,
      valor: p['valor'] ?? null
    };

    const pdfUrl = this.relatorioService.construirUrlPdf(params, false);
    this.pdfUrlSeguro = this.sanitizer.bypassSecurityTrustResourceUrl(pdfUrl);
    this.pdfDownloadUrl = this.relatorioService.construirUrlPdf(params, true);
    this.excelDownloadUrl = this.relatorioService.construirUrlExcel(params);
  }
}