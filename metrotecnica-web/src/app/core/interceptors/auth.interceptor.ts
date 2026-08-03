import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  const isApiCall = req.url.startsWith(environment.apiUrl);

  if (token && isApiCall) {
    const authorizedReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(authorizedReq);
  }

  return next(req);
};