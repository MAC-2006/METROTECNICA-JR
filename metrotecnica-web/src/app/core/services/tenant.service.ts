import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ImpersonateResponse, Tenant } from '../models/tenant.model';

@Injectable({ providedIn: 'root' })
export class TenantService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  listar(): Observable<Tenant[]> {
    return this.http.get<Tenant[]>(`${this.apiUrl}/tenants`);
  }

  impersonar(tenantId: number): Observable<ImpersonateResponse> {
    return this.http.post<ImpersonateResponse>(`${this.apiUrl}/tenants/${tenantId}/impersonate`, {});
  }
}