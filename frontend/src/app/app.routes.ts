import { Routes } from '@angular/router';
import { LayoutComponent } from './components/layout/layout';
import { Home } from './components/home/home';
import { AuthComponent } from './components/auth/auth';
import { Profile } from './components/profile/profile';
import { authGuard } from './guards/auth-guard';
import { roleGuard } from './guards/role-guard';

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
      },
      {
        path: 'products/add',
        loadComponent: () => import('./components/add-product/add-product').then(m => m.AddProductComponent),
        canActivate: [authGuard, roleGuard],
        data: { roles: ['seller'] }
      },
      {
        path: 'products/:id',
        loadComponent: () => import('./components/product-detail/product-detail').then(m => m.ProductDetail)
      },
      {
        path: 'profile',
        component: Profile,
        canActivate: [authGuard]
      }
    ]
  }
];
