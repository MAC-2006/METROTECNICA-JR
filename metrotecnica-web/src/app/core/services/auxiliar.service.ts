import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Auxiliar } from '../models/auxiliar.model';

export type TipoAuxiliar = 'setores' | 'locais-uso';

@Injectable({ providedIn: 'root' })
export class AuxiliarService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  listar(tipo: TipoAuxiliar): Observable<Auxiliar[]> {
    return this.http.get<Auxiliar[]>(`${this.apiUrl}/${tipo}`);
  }

  criar(tipo: TipoAuxiliar, nome: string): Observable<{ message: string; id: number }> {
    return this.http.post<{ message: string; id: number }>(`${this.apiUrl}/${tipo}`, { nome });
  }

  atualizar(tipo: TipoAuxiliar, id: number, nome: string): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${tipo}/${id}`, { nome });
  }

  deletar(tipo: TipoAuxiliar, id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${tipo}/${id}`);
  }
}