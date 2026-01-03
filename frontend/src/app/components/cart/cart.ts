import { Component, OnInit, OnDestroy } from '@angular/core';
import { Product } from '../../services/product';
import { CartService } from '../../services/cart';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-cart',
  imports: [],
  templateUrl: './cart.html',
  styleUrl: './cart.scss'
})
export class CartComponent implements OnInit, OnDestroy {
  items: Product[] = [];
  private cartSubscription!: Subscription;

  constructor(private cartService: CartService) { }

  ngOnInit() {
    this.cartSubscription = this.cartService.cart$.subscribe(items => {
      this.items = items;
      console.log(this.items);
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
}
