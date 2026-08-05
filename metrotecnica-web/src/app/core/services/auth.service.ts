import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoginResponse {
  accessToken: string;
  role: string;
}

interface JwtPayload {
  sub: string;
  role: string;
  tenant_id: number | null;
  exp: number;
}

const TOKEN_KEY = 'metrotecnica_token';
const ADMIN_TOKEN_KEY = 'metrotecnica_admin_token';
/**
 * Guarda a sessão do usuário (token JWT) e expõe o estado de autenticação
 * como signals, prontos pra usar direto no template.
 *
 * tenantId === null identifica o super-admin global (sem empresa vinculada) —
 * é essa distinção que decide se o login cai no /admin-dashboard (gestão de
 * empresas) ou direto no /dashboard (painel de uma empresa específica).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly apiUrl = environment.apiUrl;

  private readonly initialPayload = this.readPayload();

  readonly isAuthenticated = signal<boolean>(this.hasValidToken());
  readonly role = signal<string | null>(this.initialPayload?.role ?? null);
  readonly email = signal<string | null>(this.initialPayload?.sub ?? null);
  readonly tenantId = signal<number | null>(this.initialPayload?.tenant_id ?? null);

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap((res) => this.aplicarToken(res.accessToken, res.role, email))
    );
  }

  /** Aplica um novo token sem passar pelo endpoint de login — usado no impersonate. */
  readonly temSessaoAdminSalva = signal<boolean>(!!this.storage()?.getItem(ADMIN_TOKEN_KEY));

  // Troque o método aplicarToken por esta versão com o parâmetro novo:
  aplicarToken(token: string, roleFallback: string, emailFallback: string, salvarComoAdmin = false): void {
    if (salvarComoAdmin) {
      // Guarda o token atual (do superadmin) ANTES de sobrescrever
      const tokenAtual = this.storage()?.getItem(TOKEN_KEY);
      if (tokenAtual) {
        this.storage()?.setItem(ADMIN_TOKEN_KEY, tokenAtual);
        this.temSessaoAdminSalva.set(true);
      }
    }

    this.storage()?.setItem(TOKEN_KEY, token);
    const payload = this.readPayload();
    this.isAuthenticated.set(true);
    this.role.set(payload?.role ?? roleFallback);
    this.email.set(payload?.sub ?? emailFallback);
    this.tenantId.set(payload?.tenant_id ?? null);
  }

  // NOVO método — restaura a sessão do superadmin
  voltarParaAdmin(): void {
    const adminToken = this.storage()?.getItem(ADMIN_TOKEN_KEY);
    if (!adminToken) return;

    this.storage()?.setItem(TOKEN_KEY, adminToken);
    this.storage()?.removeItem(ADMIN_TOKEN_KEY);
    this.temSessaoAdminSalva.set(false);

    const payload = this.readPayload();
    this.isAuthenticated.set(true);
    this.role.set(payload?.role ?? 'admin');
    this.email.set(payload?.sub ?? null);
    this.tenantId.set(payload?.tenant_id ?? null);

    this.router.navigateByUrl('/admin-dashboard');
  }

  // No logout(), adicione a limpeza da chave nova:
  logout(): void {
    this.storage()?.removeItem(TOKEN_KEY);
    this.storage()?.removeItem(ADMIN_TOKEN_KEY); // NOVO
    this.temSessaoAdminSalva.set(false); // NOVO
    this.isAuthenticated.set(false);
    this.role.set(null);
    this.email.set(null);
    this.tenantId.set(null);
    this.router.navigateByUrl('/login');
  }
  getToken(): string | null {
    return this.storage()?.getItem(TOKEN_KEY) ?? null;
  }

  /** true quando o usuário logado é admin global (sem empresa vinculada). */
  isSuperAdmin(): boolean {
    return this.role() === 'admin' && this.tenantId() === null;
  }

  isAdmin(): boolean {
    return this.role() === 'admin';
  }

  private hasValidToken(): boolean {
    const payload = this.initialPayload;
    if (!payload) return false;
    return payload.exp * 1000 > Date.now();
  }

  private readPayload(): JwtPayload | null {
    const token = this.storage()?.getItem(TOKEN_KEY);
    if (!token) return null;

    try {
      const [, payloadB64] = token.split('.');
      const normalized = payloadB64.replace(/-/g, '+').replace(/_/g, '/');
      const json = atob(normalized);
      return JSON.parse(json) as JwtPayload;
    } catch {
      return null;
    }
  }

  private storage(): Storage | null {
    return this.isBrowser ? localStorage : null;
  }
}
