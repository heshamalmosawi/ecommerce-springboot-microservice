import { Routes } from '@angular/router';
import { LayoutComponent } from './components/layout/layout';
import { Home } from './components/home/home';
import { AuthComponent } from './components/auth/auth';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      {
        path: '',
        component: Home
      },
      {
        path: 'auth',
        component: AuthComponent
      }
    ]
  }
];
