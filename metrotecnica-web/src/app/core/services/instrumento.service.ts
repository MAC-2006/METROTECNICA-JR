import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { InstrumentoDetalhe, InstrumentoRequest } from '../models/instrumento.model';
import {
  InstrumentoPageResponse,
  StatsResponse
} from '../models/instrumento.model';

@Injectable({ providedIn: 'root' })
export class InstrumentoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  obterEstatisticas(): Observable<StatsResponse> {
    return this.http.get<StatsResponse>(`${this.apiUrl}/stats`);
  }

  listar(params: {
    year?: number;
    month?: number;
    search?: string;
    page?: number;
  } = {}): Observable<InstrumentoPageResponse> {
    let httpParams = new HttpParams();

    if (params.year) httpParams = httpParams.set('year', params.year);
    if (params.month) httpParams = httpParams.set('month', params.month);
    if (params.search) httpParams = httpParams.set('search', params.search);
    httpParams = httpParams.set('page', params.page ?? 1);

    return this.http.get<InstrumentoPageResponse>(`${this.apiUrl}/instrumentos`, {
      params: httpParams
    });
  }
  obterPorId(id: number): Observable<InstrumentoDetalhe> {
    return this.http.get<InstrumentoDetalhe>(`${this.apiUrl}/instrumentos/${id}`);
  }

  criar(dto: InstrumentoRequest): Observable<{ message: string; id: number }> {
    return this.http.post<{ message: string; id: number }>(`${this.apiUrl}/instrumentos`, dto);
  }

  atualizar(id: number, dto: InstrumentoRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/instrumentos/${id}`, dto);
  }

}