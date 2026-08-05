import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Usuario, UsuarioRequest, UsuarioUpdateRequest } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  listar(tenantId: number): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.apiUrl}/tenants/${tenantId}/users`);
  }

  criar(tenantId: number, dto: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/tenants/${tenantId}/users`, dto);
  }

  atualizar(tenantId: number, id: number, dto: UsuarioUpdateRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.apiUrl}/tenants/${tenantId}/users/${id}`, dto);
  }

  excluir(tenantId: number, id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/tenants/${tenantId}/users/${id}`);
  }
}
