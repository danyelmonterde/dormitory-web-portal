import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { dormerGuard } from './core/guards/dormer.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login').then(m => m.LoginComponent)
  },
  {
    path: 'admin-dashboard',
    loadComponent: () => import('./features/admin/admin-dashboard').then(m => m.AdminDashboardComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'dormer-dashboard',
    loadComponent: () => import('./features/dormer/dormer-dashboard').then(m => m.DormerDashboardComponent),
    canActivate: [authGuard, dormerGuard]
  },
  { path: '**', redirectTo: 'login' }
];
