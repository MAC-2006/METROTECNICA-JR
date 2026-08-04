import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ValidacaoCertificado } from '../models/validacao.model';

@Injectable({ providedIn: 'root' })
export class ValidacaoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  validar(hash: string): Observable<ValidacaoCertificado> {
    return this.http.get<ValidacaoCertificado>(`${this.apiUrl}/validar/${hash}`);
  }
}