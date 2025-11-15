import { Routes } from '@angular/router';
import { LayoutComponent } from './components/layout/layout';
import { Home } from './components/home/home';
import { AuthComponent } from './components/auth/auth';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      {
        path: '',
        component: Home,
        // canActivate: [authGuard]
      },
      {
        path: 'auth',
        component: AuthComponent
      }
    ]
  }
];
