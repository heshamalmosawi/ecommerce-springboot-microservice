import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface OrderItem {
  productId: string;
  quantity: number;
}

export interface CreateOrderRequest {
  email: string;
  internationalPhone: string;
  fullName: string;
  address: string;
  city: string;
  postalCode: string;
  orderItems: OrderItem[];
}

export interface OrderItemResponse {
  productId: string;
  productName: string;
  quantity: number;
  price: number;
}

export interface Order {
  id: string;
  buyerId: string;
  email: string;
  internationalPhone: string;
  fullName: string;
  address: string;
  city: string;
  postalCode: string;
  orderItems: OrderItemResponse[];
  totalPrice: number;
  status: string;
  createdAt: string;
  updatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly API_URL = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createOrder(orderData: CreateOrderRequest, token: string): Observable<Order> {
    return this.http.post<Order>(`${this.API_URL}/orders`, orderData, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    }).pipe(
      map(order => {
        console.log('Order created successfully:', order);
        return order;
      }),
      catchError(error => {
        console.error('Error creating order:', error);
        let errorMessage = 'Failed to create order';
        
        if (error.error && error.error.message) {
          errorMessage = error.error.message;
        } else if (error.message) {
          errorMessage = error.message;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }
}
