import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CnpjConsultado {
  razaoSocial: string;
  nomeFantasia: string | null;
  logradouro: string | null;
  numero: string | null;
  bairro: string | null;
  cidade: string | null;
  estado: string | null;
  cep: string | null;
  telefone: string | null;
}

export interface CepConsultado {
  logradouro: string | null;
  bairro: string | null;
  cidade: string | null;
  estado: string | null;
}

@Injectable({ providedIn: 'root' })
export class ConsultaExternaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  // Agora passa pelo SEU backend (que chama a BrasilAPI por trás) — sem CORS, com log real.
  buscarCnpj(cnpjDigits: string): Observable<CnpjConsultado> {
    return this.http.get<any>(`${this.apiUrl}/consulta/cnpj/${cnpjDigits}`).pipe(
      map((d) => ({
        razaoSocial: d.razao_social ?? '',
        nomeFantasia: d.nome_fantasia || null,
        logradouro: d.logradouro || null,
        numero: d.numero || null,
        bairro: d.bairro || null,
        cidade: d.municipio || null,
        estado: d.uf || null,
        cep: d.cep ? this.formatarCep(d.cep) : null,
        telefone: this.montarTelefone(d.ddd_telefone_1)
      }))
    );
  }

  buscarCep(cepDigits: string): Observable<CepConsultado> {
    return this.http.get<any>(`${this.apiUrl}/consulta/cep/${cepDigits}`).pipe(
      map((d) => {
        if (d.erro) throw new Error('CEP não encontrado');
        return {
          logradouro: d.logradouro || null,
          bairro: d.bairro || null,
          cidade: d.localidade || null,
          estado: d.uf || null
        };
      })
    );
  }

  private formatarCep(cep: string): string {
    const digitos = cep.replace(/\D/g, '');
    return digitos.length === 8 ? `${digitos.slice(0, 5)}-${digitos.slice(5)}` : cep;
  }

  private montarTelefone(dddTelefone: string | undefined): string | null {
    if (!dddTelefone) return null;
    const digitos = dddTelefone.replace(/\D/g, '');
    if (digitos.length < 10) return dddTelefone;
    const ddd = digitos.slice(0, 2);
    const resto = digitos.slice(2);
    return resto.length === 9
      ? `(${ddd}) ${resto.slice(0, 5)}-${resto.slice(5)}`
      : `(${ddd}) ${resto.slice(0, 4)}-${resto.slice(4)}`;
  }
}