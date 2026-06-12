import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const dormerGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn() && authService.isDormer()) {
    return true;
  }

  if (authService.isLoggedIn()) {
    router.navigate(['/admin-dashboard']);
  } else {
    router.navigate(['/login']);
  }
  return false;
};
