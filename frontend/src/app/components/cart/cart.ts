import { Component, OnInit, OnDestroy } from '@angular/core';
import { Product } from '../../services/product';
import { CartService } from '../../services/cart';
import { AuthService } from '../../services/auth';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cart',
  imports: [CommonModule],
  templateUrl: './cart.html',
  styleUrl: './cart.scss'
})
export class CartComponent implements OnInit, OnDestroy {
  items: Product[] = [];
  private cartSubscription!: Subscription;

  constructor(private cartService: CartService, private authService: AuthService, private router: Router) { }

  ngOnInit() {
    this.cartSubscription = this.cartService.cart$.subscribe(items => {
      this.items = items;
      console.log('Cart items updated:', this.items);
    });
  }

  ngOnDestroy() {
    if (this.cartSubscription) {
      this.cartSubscription.unsubscribe();
    }
  }

  incrementQuantity(item: Product) {
    this.cartService.addOrUpdateItem(item, true);
  }

  decrementQuantity(item: Product) {
    if (item.quantity > 1) {
      console.log('Decrementing item:', item);
      this.cartService.addOrUpdateItem(item, false);
    } else {
      console.log('Removing item:', item);
      this.cartService.removeItem(item.id);
    }
  }

  getCartTotal(): number {
    return this.items.reduce((sum, item) => sum + item.price * item.quantity, 0);
  }

  proceedToCheckout() {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/auth'], { queryParams: { returnUrl: '/checkout' } });
    } else {
      this.router.navigate(['/checkout']);
    }
  }
}
