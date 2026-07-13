import { Routes } from '@angular/router';
import { authGuard } from './shared/guards/auth.guard';
import { roleGuard } from './shared/guards/role.guard';

export const routes: Routes = [
  {
    path: 'confirmar/:token',
    loadComponent: () => import('./components/confirmar/confirmar').then(m => m.Confirmar)
  },
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
    canActivate: [authGuard],
    children: [
      { path: '', loadComponent: () => import('./components/home/home').then(m => m.Home) },
      {
        path: 'tratamientos',
        canActivate: [roleGuard(['CLIENTE', 'PELUQUERO', 'ADMIN'])],
        loadComponent: () =>
          import('./components/tratamiento-list/tratamiento-list').then(m => m.TratamientoList)
      },
      {
        path: 'peluqueros',
        canActivate: [roleGuard(['CLIENTE', 'PELUQUERO', 'ADMIN'])],
        loadComponent: () =>
          import('./components/peluquero-list/peluquero-list').then(m => m.PeluqueroList)
      },
      {
        path: 'reservar',
        canActivate: [roleGuard(['CLIENTE'])],
        loadComponent: () =>
          import('./components/reserva/reserva').then(m => m.Reserva)
      },
      {
        path: 'mis-turnos',
        canActivate: [roleGuard(['CLIENTE'])],
        loadComponent: () =>
          import('./components/mis-turnos/mis-turnos').then(m => m.MisTurnos)
      },
      {
        path: 'agenda',
        canActivate: [roleGuard(['PELUQUERO'])],
        loadComponent: () =>
          import('./components/agenda/agenda').then(m => m.Agenda)
      },
      {
        path: 'admin/tratamientos',
        canActivate: [roleGuard(['ADMIN'])],
        loadComponent: () =>
          import('./components/admin-tratamientos/admin-tratamientos').then(m => m.AdminTratamientos)
      },
      {
        path: 'admin/peluqueros',
        canActivate: [roleGuard(['ADMIN'])],
        loadComponent: () =>
          import('./components/admin-peluqueros/admin-peluqueros').then(m => m.AdminPeluqueros)
      }
    ]
  },
  { path: '**', redirectTo: 'auth/login' }
];
