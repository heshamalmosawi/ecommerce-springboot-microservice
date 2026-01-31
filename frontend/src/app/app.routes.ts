import { Routes } from '@angular/router';
import { LayoutComponent } from './components/layout/layout';
import { Home } from './components/home/home';
import { AuthComponent } from './components/auth/auth';
import { Profile } from './components/profile/profile';
import { authGuard } from './guards/auth-guard';
import { roleGuard } from './guards/role-guard';
import { CartComponent } from './components/cart/cart';
import { AddProductComponent } from './components/add-product/add-product';
import { ProductDetail } from './components/product-detail/product-detail';
import { EditProductComponent } from './components/edit-product/edit-product';
import { Checkout } from './components/checkout/checkout';
import { OrderConfirmation } from './components/order-confirmation/order-confirmation';
import { OrderHistory } from './components/order-history/order-history';
import { PurchaseAnalytics } from './components/purchase-analytics/purchase-analytics';
import { SellerAnalytics } from './components/seller-analytics/seller-analytics';
import { SellerOrders } from './components/seller-orders/seller-orders';

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
        path: 'cart',
        component: CartComponent
      },
      {
        path: 'checkout',
        component: Checkout,
        canActivate: [authGuard]
      },
      {
        path: 'order-confirmation',
        component: OrderConfirmation
      },
      {
        path: 'profile',
        component: Profile,
        canActivate: [authGuard]
      },
      {
        path: 'profile/order-history',
        component: OrderHistory,
        canActivate: [authGuard]
      },
      {
        path: 'profile/analytics',
        component: PurchaseAnalytics,
        canActivate: [authGuard]
      },
      {
        path: 'seller/analytics',
        component: SellerAnalytics,
        canActivate: [authGuard, roleGuard],
        data: { roles: ['seller'] }
      },
      {
        path: 'seller/orders',
        component: SellerOrders,
        canActivate: [authGuard, roleGuard],
        data: { roles: ['seller'] }
      },
      {
        path: 'products/add',
        component: AddProductComponent,
        canActivate: [authGuard, roleGuard],
        data: { roles: ['seller'] }
      },
      {
        path: 'products/:id',
        component: ProductDetail,
      },
      {
        path: 'products/:id/edit',
        component: EditProductComponent,
        canActivate: [authGuard, roleGuard],
        data: { roles: ['seller'] }
      }
    ]
  }
];
