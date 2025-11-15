import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth';
import { Router } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // TODO: ?? Redirect to login page with return URL
  // router.navigate(['/auth/login'], { 
  //   queryParams: { returnUrl: state.url } 
  // });
  return false;
};
