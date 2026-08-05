import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth.service';

/**
 * Protege rotas de criação/edição (ex.: /instrumentos/novo, /instrumentos/:id/editar).
 * Usuários com papel "user" têm acesso somente leitura — a API já rejeita essas
 * ações com 403 (@PreAuthorize hasRole ADMIN), mas este guard evita que eles
 * cheguem a ver o formulário e melhora a experiência mandando de volta pro
 * painel com uma explicação.
 */
export const adminWriteGuard: CanActivateFn = () => {
  const isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  if (!isBrowser) return true; // deixa o SSR passar; o cliente decide de verdade

  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated() && authService.isAdmin()) {
    return true;
  }

  router.navigateByUrl(authService.isAuthenticated() ? '/dashboard' : '/login');
  return false;
};
