import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated() && authService.isSuperAdmin()) {
    return true;
  }

  // Usuário autenticado mas não é super-admin: manda para o dashboard normal.
  // Não autenticado: manda para o login.
  router.navigateByUrl(authService.isAuthenticated() ? '/dashboard' : '/login');
  return false;
};