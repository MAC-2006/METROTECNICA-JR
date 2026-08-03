import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent),
    canActivate: [guestGuard]
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'instrumentos',
    loadComponent: () => import('./features/instrumentos-list/instrumentos-list.component').then((m) => m.InstrumentosListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'admin-dashboard',
    loadComponent: () => import('./features/admin-dashboard/admin-dashboard.component').then((m) => m.AdminDashboardComponent),
    canActivate: [adminGuard]
  },
  { path: '**', redirectTo: '' }
];