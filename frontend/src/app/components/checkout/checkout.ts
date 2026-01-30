import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CartService } from '../../services/cart';
import { Product } from '../../services/product';
import { Subscription } from 'rxjs';
import { OrderService, CreateOrderRequest, Order } from '../../services/order';
import { AuthService } from '../../services/auth';
import { Router } from '@angular/router';
import { Toast } from '../toast/toast';

@Component({
  selector: 'app-checkout',
  imports: [CommonModule, ReactiveFormsModule, Toast],
  templateUrl: './checkout.html',
  styleUrl: './checkout.scss'
})
export class Checkout implements OnInit, OnDestroy {
  items: Product[] = [];
  private cartSubscription!: Subscription;
  checkoutForm: FormGroup;
  isSubmitting = false;
  @ViewChild(Toast) toast!: Toast;

  constructor(
    private cartService: CartService,
    private fb: FormBuilder,
    private orderService: OrderService,
    private authService: AuthService,
    private router: Router
  ) {
    this.checkoutForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      internationalPhone: ['', [Validators.required, Validators.pattern('^\\+[1-9]\\d{1,14}$')]],
      address: ['', [Validators.required, Validators.minLength(5)]],
      city: ['', [Validators.required]],
      postalCode: ['', [Validators.required, Validators.pattern('^\\d{3,5}$')]]
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
      return;
    }

    this.isSubmitting = true;

    const token = this.authService.getToken();
    if (!token) {
      this.toast.show('You must be logged in to place an order', 'error');
      this.isSubmitting = false;
      return;
    }

    const formValue = this.checkoutForm.value;
    const orderItems = this.items.map(item => ({
      productId: item.id,
      quantity: item.quantity
    }));

    const orderRequest: CreateOrderRequest = {
      email: formValue.email,
      internationalPhone: formValue.internationalPhone,
      fullName: formValue.fullName,
      address: formValue.address,
      city: formValue.city,
      postalCode: formValue.postalCode,
      orderItems: orderItems
    };

     this.orderService.createOrder(orderRequest, token).subscribe({
      next: (order: Order) => {
        this.cartService.clear();
        this.router.navigate(['/order-confirmation'], { 
          queryParams: { orderId: order.id },
          state: { order }
        });
      },
      error: (error) => {
        this.toast.show(error.message || 'Failed to create order. Please try again.', 'error');
        this.isSubmitting = false;
      }
    });
  }
}
