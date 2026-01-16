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

export interface Pageable {
  pageNumber: number;
  pageSize: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
}

export interface OrdersPage {
  content: Order[];
  pageable: Pageable;
  totalPages: number;
  totalElements: number;
  last: boolean;
  first: boolean;
  numberOfElements: number;
  size: number;
  number: number;
  empty: boolean;
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

  getUserOrders(page: number = 0, size: number = 10, sortBy: string = 'createdAt', sortDir: string = 'desc'): Observable<OrdersPage> {
    return this.http.get<OrdersPage>(`${this.API_URL}/orders`, {
      params: {
        page: page.toString(),
        size: size.toString(),
        sortBy: sortBy,
        sortDir: sortDir
      }
    }).pipe(
      map(response => {
        console.log('Orders fetched successfully:', response);
        return response;
      }),
      catchError(error => {
        console.error('Error fetching orders:', error);
        let errorMessage = 'Failed to fetch orders';
        
        if (error.error && error.error.message) {
          errorMessage = error.error.message;
        } else if (error.message) {
          errorMessage = error.message;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  getOrderById(orderId: string): Observable<Order> {
    return this.http.get<Order>(`${this.API_URL}/orders/${orderId}`).pipe(
      map(response => {
        console.log('Order fetched successfully:', response);
        return response;
      }),
      catchError(error => {
        console.error('Error fetching order:', error);
        let errorMessage = 'Failed to fetch order';
        
        if (error.error && typeof error.error === 'string') {
          errorMessage = error.error;
        } else if (error.error && error.error.message) {
          errorMessage = error.error.message;
        } else if (error.message) {
          errorMessage = error.message;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }
}
