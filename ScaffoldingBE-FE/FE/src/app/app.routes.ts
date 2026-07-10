import { Routes } from '@angular/router';
import { authGuard } from './shared/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadComponent: () => import('./components/auth-layout/auth-layout').then(m => m.AuthLayout),
    children: [
      { path: 'login',    loadComponent: () => import('./components/login/login').then(m => m.Login) },
      { path: 'register', loadComponent: () => import('./components/register/register').then(m => m.Register) },
      { path: '**',       redirectTo: 'login' }
    ]
  },
  {
    path: '',
    loadComponent: () => import('./components/main-layout/main-layout').then(m => m.MainLayout),
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: 'auth/login' }
];
