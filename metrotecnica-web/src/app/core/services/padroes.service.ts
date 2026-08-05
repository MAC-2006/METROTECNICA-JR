import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

export type UploadPadroesEvento =
  | { tipo: 'progresso'; percentual: number }
  | { tipo: 'processando' }
  | { tipo: 'concluido'; resultado: { message: string; count: number } };

@Injectable({ providedIn: 'root' })
export class PadroesService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly apiUrl = environment.apiUrl;

  listar(): Observable<{ arquivos: string[] }> {
    return this.http.get<{ arquivos: string[] }>(`${this.apiUrl}/lista-padroes`);
  }

  buscar(termo: string): Observable<{ arquivos: string[] }> {
    return this.http.get<{ arquivos: string[] }>(`${this.apiUrl}/busca-padroes`, { params: { q: termo } });
  }

  urlDownload(arquivo: string): string {
    const token = this.auth.getToken();
    const params = new URLSearchParams({ arquivo });
    if (token) params.set('jwt', token);
    return `${this.apiUrl}/padroes/download?${params.toString()}`;
  }

  // Mesmo padrão de XHR do migracao.service.ts — Fetch API não expõe progresso de upload.
  uploadZip(file: File): Observable<UploadPadroesEvento> {
    return new Observable<UploadPadroesEvento>((subscriber) => {
      const formData = new FormData();
      formData.append('file', file);

      const xhr = new XMLHttpRequest();
      xhr.open('POST', `${this.apiUrl}/admin/upload-padroes`, true);

      const token = this.auth.getToken();
      if (token) xhr.setRequestHeader('Authorization', `Bearer ${token}`);

      xhr.upload.onprogress = (event) => {
        if (!event.lengthComputable) return;
        const percentual = Math.round((event.loaded / event.total) * 100);
        if (percentual >= 100) subscriber.next({ tipo: 'processando' });
        else subscriber.next({ tipo: 'progresso', percentual });
      };

      xhr.upload.onload = () => subscriber.next({ tipo: 'processando' });

      xhr.onload = () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          try {
            subscriber.next({ tipo: 'concluido', resultado: JSON.parse(xhr.responseText) });
            subscriber.complete();
          } catch {
            subscriber.error({ error: { error: 'Resposta inválida do servidor.' } });
          }
        } else {
          let corpo: unknown = null;
          try { corpo = JSON.parse(xhr.responseText); } catch { /* ignore */ }
          subscriber.error({ status: xhr.status, error: corpo });
        }
      };

      xhr.onerror = () => subscriber.error({ status: 0, error: { error: 'Falha de conexão com o servidor.' } });

      xhr.send(formData);
      return () => xhr.abort();
    });
  }
}