import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth';
import { Router } from '@angular/router';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Check if user is authenticated
  if (!authService.isAuthenticated()) {
    router.navigate(['/auth'], { 
      queryParams: { returnUrl: state.url } 
    });
    return false;
  }

  // Get required roles from route data
  const requiredRoles = route.data?.['roles'] as string[] | undefined;
  
  if (!requiredRoles || requiredRoles.length === 0) {
    return true; // No specific role required
  }

  // Check if user has any of the required roles
  const hasRequiredRole = requiredRoles.some(role => 
    authService.hasRole(role as 'client' | 'seller')
  );

  if (hasRequiredRole) {
    return true;
  }

  // User doesn't have required role, redirect to home or unauthorized page
  router.navigate(['/']); // Could redirect to unauthorized page instead
  return false;
};
