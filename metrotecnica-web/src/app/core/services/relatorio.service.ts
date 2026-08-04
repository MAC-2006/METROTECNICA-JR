import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

export interface RelatorioParams {
  tipo: string;
  start?: string | null;
  end?: string | null;
  valor?: string | null;
}

@Injectable({ providedIn: 'root' })
export class RelatorioService {
  private readonly auth = inject(AuthService);
  private readonly apiUrl = environment.apiUrl;

  construirUrlPdf(params: RelatorioParams, download = false): string {
    return this.construirUrl('/relatorio/pdf', params, download);
  }

  construirUrlExcel(params: RelatorioParams): string {
    return this.construirUrl('/relatorio/excel', params, false);
  }

  private construirUrl(path: string, params: RelatorioParams, download: boolean): string {
    const token = this.auth.getToken();
    const query = new URLSearchParams();
    query.set('tipo', params.tipo);
    if (params.start) query.set('start', params.start);
    if (params.end) query.set('end', params.end);
    if (params.valor) query.set('valor', params.valor);
    if (download) query.set('download', 'true');
    if (token) query.set('jwt', token);
    return `${this.apiUrl}${path}?${query.toString()}`;
  }
}