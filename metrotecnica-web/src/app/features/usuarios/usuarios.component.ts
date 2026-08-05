import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TenantService } from '../../core/services/tenant.service';
import { UserService } from '../../core/services/user.service';
import { Usuario, UsuarioRequest, UsuarioUpdateRequest } from '../../core/models/user.model';

interface UsuarioForm {
  email: string;
  password: string;
  nomeCompleto: string | null;
  role: string;
  canSign: boolean;
}

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.scss'
})
export class UsuariosComponent {
  protected readonly auth = inject(AuthService);
  private readonly tenantService = inject(TenantService);
  private readonly userService = inject(UserService);
  private readonly route = inject(ActivatedRoute);

  protected readonly tenantId = signal<number>(Number(this.route.snapshot.paramMap.get('tenantId')));
  protected readonly tenantNome = signal<string>('');

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly isSalvando = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  // null => formulário em modo "novo usuário"; caso contrário, id do usuário em edição.
  protected readonly editandoId = signal<number | null>(null);
  protected readonly form = signal<UsuarioForm>(this.formVazio());
  protected readonly excluindoId = signal<number | null>(null);

  constructor() {
    this.tenantService.obterPorId(this.tenantId()).subscribe({
      next: (tenant) => this.tenantNome.set(tenant.name),
      error: () => this.tenantNome.set('')
    });
    this.carregarUsuarios();
  }

  private formVazio(): UsuarioForm {
    return {
      email: '',
      password: '',
      nomeCompleto: null,
      role: 'user',
      canSign: false
    };
  }

  private carregarUsuarios(): void {
    this.isLoading.set(true);
    this.userService.listar(this.tenantId()).subscribe({
      next: (dados) => {
        this.usuarios.set(dados);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Não foi possível carregar os usuários desta empresa.');
        this.isLoading.set(false);
      }
    });
  }

  protected atualizarCampo<K extends keyof UsuarioForm>(campo: K, valor: UsuarioForm[K]): void {
    this.form.update((f) => ({ ...f, [campo]: valor }));
  }

  protected editar(usuario: Usuario): void {
    this.editandoId.set(usuario.id);
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.form.set({
      email: usuario.email,
      password: '',
      nomeCompleto: usuario.nomeCompleto,
      role: usuario.role,
      canSign: usuario.canSign
    });
  }

  protected cancelarEdicao(): void {
    this.editandoId.set(null);
    this.form.set(this.formVazio());
    this.errorMessage.set(null);
  }

  protected salvar(): void {
    const dados = this.form();

    if (!dados.email.trim() && this.editandoId() === null) {
      this.errorMessage.set('Informe o e-mail do usuário.');
      return;
    }
    if (this.editandoId() === null && dados.password.trim().length < 6) {
      this.errorMessage.set('A senha deve ter pelo menos 6 caracteres.');
      return;
    }

    this.isSalvando.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const idEmEdicao = this.editandoId();

    if (idEmEdicao === null) {
      const dto: UsuarioRequest = {
        email: dados.email.trim(),
        password: dados.password,
        nomeCompleto: dados.nomeCompleto?.trim() || null,
        role: dados.role,
        canSign: dados.canSign
      };

      this.userService.criar(this.tenantId(), dto).subscribe({
        next: () => {
          this.isSalvando.set(false);
          this.successMessage.set('Usuário criado com sucesso.');
          this.form.set(this.formVazio());
          this.carregarUsuarios();
        },
        error: (err) => {
          this.isSalvando.set(false);
          this.errorMessage.set(err?.error?.error ?? 'Não foi possível criar o usuário.');
        }
      });
    } else {
      const dto: UsuarioUpdateRequest = {
        nomeCompleto: dados.nomeCompleto?.trim() || null,
        role: dados.role,
        canSign: dados.canSign,
        password: dados.password.trim() ? dados.password : null
      };

      this.userService.atualizar(this.tenantId(), idEmEdicao, dto).subscribe({
        next: () => {
          this.isSalvando.set(false);
          this.successMessage.set('Usuário atualizado com sucesso.');
          this.cancelarEdicao();
          this.carregarUsuarios();
        },
        error: (err) => {
          this.isSalvando.set(false);
          this.errorMessage.set(err?.error?.error ?? 'Não foi possível atualizar o usuário.');
        }
      });
    }
  }

  protected excluir(usuario: Usuario): void {
    if (!confirm(`Remover o usuário ${usuario.email}? Esta ação não pode ser desfeita.`)) {
      return;
    }

    this.excluindoId.set(usuario.id);
    this.errorMessage.set(null);

    this.userService.excluir(this.tenantId(), usuario.id).subscribe({
      next: () => {
        this.excluindoId.set(null);
        this.successMessage.set('Usuário removido.');
        if (this.editandoId() === usuario.id) this.cancelarEdicao();
        this.carregarUsuarios();
      },
      error: () => {
        this.excluindoId.set(null);
        this.errorMessage.set('Não foi possível remover este usuário.');
      }
    });
  }

  protected sair(): void {
    this.auth.logout();
  }
}
