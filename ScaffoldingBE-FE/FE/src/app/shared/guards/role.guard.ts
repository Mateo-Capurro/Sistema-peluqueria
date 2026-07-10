import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/auth.model';

/**
 * Guard factory: allows the route only if the authenticated user has one of the
 * given roles. Redirects to login when unauthenticated, or to home otherwise.
 */
export const roleGuard = (roles: Role[]): CanActivateFn => () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.parseUrl('/auth/login');
  }

  const role = auth.role();
  if (role && roles.includes(role)) {
    return true;
  }

  return router.parseUrl('/');
};
