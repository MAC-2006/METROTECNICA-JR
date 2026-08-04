import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ImpersonateResponse, Tenant, TenantRequest } from '../models/tenant.model';

@Injectable({ providedIn: 'root' })
export class TenantService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  listar(): Observable<Tenant[]> {
    return this.http.get<Tenant[]>(`${this.apiUrl}/tenants`);
  }

  obterPorId(id: number): Observable<Tenant> {
    return this.http.get<Tenant>(`${this.apiUrl}/tenants/${id}`);
  }

  criar(dto: TenantRequest): Observable<{ message: string; id: number }> {
    return this.http.post<{ message: string; id: number }>(`${this.apiUrl}/tenants`, dto);
  }

  atualizar(id: number, dto: TenantRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/tenants/${id}`, dto);
  }

  impersonar(tenantId: number): Observable<ImpersonateResponse> {
    return this.http.post<ImpersonateResponse>(`${this.apiUrl}/tenants/${tenantId}/impersonate`, {});
  }
}