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

// Purchase Analytics Types
export enum OrderStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  FAILED = 'FAILED'
}

export interface DateRange {
  start: string | null;  // YYYY-MM-DD format
  end: string | null;    // YYYY-MM-DD format
}

export interface ProductAnalytics {
  productId: string;
  productName: string;
  orderCount: number;
  totalQuantity: number;
  totalSpent: number;
}

export interface PurchaseSummary {
  totalSpent: number;
  orderCount: number;
  productCount: number;
  dateRange: DateRange | null;
  mostPurchasedProducts: ProductAnalytics[];
  topSpendingProducts: ProductAnalytics[];
}

export interface AnalyticsFilters {
  startDate?: string;  // YYYY-MM-DD format
  endDate?: string;    // YYYY-MM-DD format
  status?: OrderStatus;
}

// Seller Analytics Types
export interface SellerProductAnalytics {
  productId: string;
  productName: string;
  unitsSold: number;
  orderCount: number;
  totalRevenue: number;
}

export interface SellerAnalyticsSummary {
  totalRevenue: number;
  totalOrders: number;
  totalUnitsSold: number;
  productCount: number;
  dateRange: DateRange | null;
  bestSellingProducts: SellerProductAnalytics[];
  topRevenueProducts: SellerProductAnalytics[];
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly API_URL = environment.apiUrl;

  constructor(private http: HttpClient) { }

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

  getUserOrders(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'createdAt',
    sortDir: string = 'desc',
    status?: string,
    startDate?: string,
    endDate?: string
  ): Observable<OrdersPage> {
    const params: any = {
      page: page.toString(),
      size: size.toString(),
      sortBy: sortBy,
      sortDir: sortDir
    };

    // Add optional filter parameters if provided
    if (status) {
      params.status = status;
    }
    if (startDate) {
      params.startDate = startDate;
    }
    if (endDate) {
      params.endDate = endDate;
    }

    return this.http.get<OrdersPage>(`${this.API_URL}/orders`, { params }).pipe(
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

  getPurchaseAnalytics(filters?: AnalyticsFilters): Observable<PurchaseSummary> {
    const params: any = {};

    if (filters?.startDate) {
      params.startDate = filters.startDate;
    }
    if (filters?.endDate) {
      params.endDate = filters.endDate;
    }
    if (filters?.status) {
      params.status = filters.status;
    }

    return this.http.get<PurchaseSummary>(`${this.API_URL}/orders/analytics/purchase-summary`, { params }).pipe(
      map(response => {
        console.log('Purchase analytics fetched successfully:', response);
        return response;
      }),
      catchError(error => {
        console.error('Error fetching purchase analytics:', error);
        let errorMessage = 'Failed to fetch purchase analytics';

        // API returns plain text for errors
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

  getSellerAnalytics(filters?: AnalyticsFilters): Observable<SellerAnalyticsSummary> {
    const params: any = {};

    if (filters?.startDate) {
      params.startDate = filters.startDate;
    }
    if (filters?.endDate) {
      params.endDate = filters.endDate;
    }
    if (filters?.status) {
      params.status = filters.status;
    }

    return this.http.get<SellerAnalyticsSummary>(`${this.API_URL}/orders/analytics/seller-summary`, { params }).pipe(
      map(response => {
        console.log('Seller analytics fetched successfully:', response);
        return response;
      }),
      catchError(error => {
        console.error('Error fetching seller analytics:', error);
        let errorMessage = 'Failed to fetch seller analytics';

        // API returns plain text for errors
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
