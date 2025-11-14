import { Routes } from '@angular/router';
import { Layout } from './components/layout/layout';
import { Home } from './components/home/home';

export const routes: Routes = [
  {
    path: '',
    component: Layout,
    children: [
      {
        path: '',
        component: Home
      }
    ]
  }
];
