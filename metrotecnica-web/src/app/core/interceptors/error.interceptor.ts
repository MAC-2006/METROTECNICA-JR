import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // 401 numa rota autenticada normalmente significa token expirado ou
      // inválido — desloga e manda pro login em vez de deixar a tela travada.
      if (error.status === 401 && authService.isAuthenticated()) {
        authService.logout();
      }
      return throwError(() => error);
    })
  );
};