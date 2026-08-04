import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

export interface MigracaoResultado {
  message: string;
  count: number;
  tenantId: number;
  name: string;
  slug: string;
  warnings: string[];
}

export type MigracaoEvento =
  | { tipo: 'progresso'; percentual: number }
  | { tipo: 'processando' }
  | { tipo: 'concluido'; resultado: MigracaoResultado };

@Injectable({ providedIn: 'root' })
export class MigracaoService {
  private readonly auth = inject(AuthService);
  private readonly apiUrl = environment.apiUrl;

  // XMLHttpRequest direto: o HttpClient do app usa withFetch() (necessário
  // pro SSR), e a Fetch API não expõe progresso de UPLOAD — só de download.
  upload(file: File, nomeEmpresa: string): Observable<MigracaoEvento> {
    return new Observable<MigracaoEvento>((subscriber) => {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('nome_empresa', nomeEmpresa);

      const xhr = new XMLHttpRequest();
      xhr.open('POST', `${this.apiUrl}/admin/upload-migracao`, true);

      const token = this.auth.getToken();
      if (token) {
        xhr.setRequestHeader('Authorization', `Bearer ${token}`);
      }

      // Progresso real do ENVIO do arquivo. Deixa de emitir número quando
      // chega em 100% — a partir daí é o servidor extraindo o ZIP e
      // processando o DBF, o que pode levar bem mais tempo que o envio em
      // si (principalmente em bases grandes), então tratamos como uma fase
      // separada em vez de deixar a barra "presa" em 100%.
      xhr.upload.onprogress = (event) => {
        if (!event.lengthComputable) return;

        const percentual = Math.round((event.loaded / event.total) * 100);
        if (percentual >= 100) {
          subscriber.next({ tipo: 'processando' });
        } else {
          subscriber.next({ tipo: 'progresso', percentual });
        }
      };

      // Garante a transição pra "processando" mesmo se o navegador não
      // disparar um último onprogress exatamente em 100%.
      xhr.upload.onload = () => {
        subscriber.next({ tipo: 'processando' });
      };

      xhr.onload = () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          try {
            const resultado = JSON.parse(xhr.responseText) as MigracaoResultado;
            subscriber.next({ tipo: 'concluido', resultado });
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

      xhr.onerror = () => {
        subscriber.error({ status: 0, error: { error: 'Falha de conexão com o servidor.' } });
      };

      xhr.send(formData);
      return () => xhr.abort();
    });
  }
}