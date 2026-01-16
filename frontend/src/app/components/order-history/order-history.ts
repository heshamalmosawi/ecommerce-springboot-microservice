import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService, Order, OrdersPage } from '../../services/order';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-order-history',
  imports: [CommonModule, FormsModule],
  templateUrl: './order-history.html',
  styleUrl: './order-history.scss'
})
export class OrderHistory implements OnInit {
  ordersPage: OrdersPage | null = null;
  orders: Order[] = [];
  loading = false;
  error = '';
  
  currentPage = 0;
  pageSize = 10;
  totalOrders = 0;
  totalPages = 0;

  sortBy = 'createdAt';
  sortDir = 'desc';

  expandedOrders: Set<string> = new Set();

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.error = '';
    
    this.orderService.getUserOrders(this.currentPage, this.pageSize, this.sortBy, this.sortDir).subscribe({
      next: (page) => {
        this.ordersPage = page;
        this.orders = page.content;
        this.totalOrders = page.totalElements;
        this.totalPages = page.totalPages;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading orders:', err);
        this.error = 'Failed to load orders. Please try again.';
        this.loading = false;
      }
    });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadOrders();
  }

  onSortChange(field: string): void {
    if (this.sortBy === field) {
      this.sortDir = this.sortDir === 'desc' ? 'asc' : 'desc';
    } else {
      this.sortBy = field;
      this.sortDir = 'desc';
    }
    this.currentPage = 0;
    this.loadOrders();
  }

  toggleOrderExpand(orderId: string): void {
    if (this.expandedOrders.has(orderId)) {
      this.expandedOrders.delete(orderId);
    } else {
      this.expandedOrders.add(orderId);
    }
  }

  isOrderExpanded(orderId: string): boolean {
    return this.expandedOrders.has(orderId);
  }

  getStatusClass(status: string): string {
    const statusLower = status.toLowerCase();
    switch (statusLower) {
      case 'pending':
        return 'status-pending';
      case 'processing':
        return 'status-processing';
      case 'shipped':
        return 'status-shipped';
      case 'delivered':
        return 'status-delivered';
      case 'failed':
        return 'status-failed';
      default:
        return 'status-unknown';
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}