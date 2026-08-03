import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TenantService } from '../../core/services/tenant.service';
import { Tenant } from '../../core/models/tenant.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent {
  protected readonly auth = inject(AuthService);
  private readonly tenantService = inject(TenantService);
  private readonly router = inject(Router);

  protected readonly tenants = signal<Tenant[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly impersonandoId = signal<number | null>(null);

  constructor() {
    this.carregarTenants();
  }

  private carregarTenants(): void {
    this.isLoading.set(true);
    this.tenantService.listar().subscribe({
      next: (dados) => {
        this.tenants.set(dados);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Não foi possível carregar as empresas.');
        this.isLoading.set(false);
      }
    });
  }

  protected acessarEmpresa(tenant: Tenant): void {
    this.impersonandoId.set(tenant.id);
    this.errorMessage.set(null);

    this.tenantService.impersonar(tenant.id).subscribe({
      next: (res) => {
        this.auth.aplicarToken(res.accessToken, res.role, this.auth.email() ?? '');
        this.router.navigateByUrl('/dashboard');
      },
      error: (err) => {
        this.impersonandoId.set(null);
        if (err?.error?.error === 'cadastro_incompleto') {
          this.errorMessage.set(`${tenant.name}: cadastro jurídico pendente (razão social / CNPJ).`);
        } else {
          this.errorMessage.set('Não foi possível acessar esta empresa.');
        }
      }
    });
  }

  protected sair(): void {
    this.auth.logout();
  }
}