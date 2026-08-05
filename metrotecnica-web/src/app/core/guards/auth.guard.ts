import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  if (!isBrowser) return true; // deixa o SSR passar; o cliente decide de verdade

  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  router.navigateByUrl('/login');
  return false;
};