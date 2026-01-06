import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CartService } from '../../services/cart';
import { Product } from '../../services/product';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-checkout',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './checkout.html',
  styleUrl: './checkout.scss'
})
export class Checkout implements OnInit {
  items: Product[] = [];
  private cartSubscription!: Subscription;
  checkoutForm: FormGroup;
  isSubmitting = false;

  constructor(
    private cartService: CartService,
    private fb: FormBuilder
  ) {
    this.checkoutForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern('^[0-9+]{8,15}$')]],
      address: ['', [Validators.required, Validators.minLength(5)]],
      city: ['', [Validators.required]],
      postalCode: ['', [Validators.required]],
      paymentMethod: ['cod']
    });
  }

  ngOnInit() {
    this.cartSubscription = this.cartService.cart$.subscribe(items => {
      this.items = items;
      console.log('Checkout - Cart items updated:', this.items);
    });
  }

  ngOnDestroy() {
    if (this.cartSubscription) {
      this.cartSubscription.unsubscribe();
    }
  }

  getCartTotal(): number {
    return this.items.reduce((sum, item) => sum + item.price * item.quantity, 0);
  }

  onSubmit() {
    if (this.checkoutForm.invalid || this.items.length === 0) {
      console.log('Form validation failed or cart is empty');
      return;
    }

    console.log('Placing order...');
    console.log('Form data:', this.checkoutForm.value);
    console.log('Order items:', this.items);
    console.log('Total:', this.getCartTotal());
  }
}
