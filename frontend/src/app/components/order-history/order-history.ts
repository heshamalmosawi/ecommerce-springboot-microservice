import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService, Order, OrdersPage } from '../../services/order';
import { FormsModule } from '@angular/forms';
import { OrderCancelModal } from '../order-cancel-modal/order-cancel-modal';
import { OrderStatusModal } from '../order-status-modal/order-status-modal';
import { OrderActions } from '../order-actions/order-actions';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-order-history',
  imports: [CommonModule, FormsModule, OrderCancelModal, OrderStatusModal, OrderActions],
  templateUrl: './order-history.html',
  styleUrl: './order-history.scss'
})
export class OrderHistory implements OnInit {
  @ViewChild(OrderCancelModal) cancelModal!: OrderCancelModal;
  @ViewChild(OrderStatusModal) statusModal!: OrderStatusModal;

  ordersPage: OrdersPage | null = null;
  orders: Order[] = [];
  loading = false;
  error = '';
  searchMode = false;
  searchOrderId = '';
  searching = false;
  cancelling = false;
  updatingStatus = false;
  selectedOrderId = '';

  currentPage = 0;
  pageSize = 10;
  totalOrders = 0;
  totalPages = 0;

  sortBy = 'createdAt';
  sortDir = 'desc';

  filterStatus = '';
  filterStartDate = '';
  filterEndDate = '';
  showFilters = false;

  expandedOrders: Set<string> = new Set();
  userRole: 'client' | 'seller' = 'client';

  constructor(
    private orderService: OrderService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.getCurrentUserValue()?.role || 'client';
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.error = '';
    this.searchMode = false;
    
    this.orderService.getUserOrders(
      this.currentPage, 
      this.pageSize, 
      this.sortBy, 
      this.sortDir,
      this.filterStatus || undefined,
      this.filterStartDate || undefined,
      this.filterEndDate || undefined
    ).subscribe({
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

  searchOrder(): void {
    if (!this.searchOrderId.trim()) {
      this.loadOrders();
      return;
    }
    
    this.searching = true;
    this.error = '';
    this.searchMode = true;
    
    this.orderService.getOrderById(this.searchOrderId.trim()).subscribe({
      next: (order) => {
        this.orders = [order];
        this.ordersPage = null;
        this.searching = false;
        this.expandedOrders.add(order.id);
      },
      error: (err) => {
        console.error('Error searching order:', err);
        this.error = err.message || 'Order not found. Please check the order ID.';
        this.orders = [];
        this.searching = false;
      }
    });
  }

  clearSearch(): void {
    this.searchOrderId = '';
    this.searchMode = false;
    this.loadOrders();
  }

  onSearchEnter(event: Event): void {
    const keyboardEvent = event as KeyboardEvent;
    if (keyboardEvent.key === 'Enter') {
      this.searchOrder();
    }
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

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadOrders();
  }

  clearFilters(): void {
    this.filterStatus = '';
    this.filterStartDate = '';
    this.filterEndDate = '';
    this.currentPage = 0;
    this.loadOrders();
  }

  hasActiveFilters(): boolean {
    return !!(this.filterStatus || this.filterStartDate || this.filterEndDate);
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
      case 'cancelled':
        return 'status-cancelled';
      case 'failed':
        return 'status-failed';
      default:
        return 'status-unknown';
    }
  }

  onCancelOrderClick(orderId: string): void {
    this.cancelModal.show();
  }

  onCancelOrderConfirm(reason: string): void {
    const orderIdToCancel = this.expandedOrders.values().next().value;
    if (!orderIdToCancel) return;

    this.cancelling = true;
    this.orderService.cancelOrder(orderIdToCancel, reason).subscribe({
      next: (response) => {
        this.cancelModal.hide();
        this.cancelling = false;
        this.loadOrders();
      },
      error: (err) => {
        console.error('Error cancelling order:', err);
        this.cancelling = false;
        this.cancelModal.hide();
      }
    });
  }

  onUpdateStatusClick(orderId: string, currentStatus: string): void {
    this.selectedOrderId = orderId;
    this.statusModal.currentStatus = currentStatus;
    this.statusModal.show();
  }

  onUpdateStatusConfirm(data: {status: string, reason: string}): void {
    this.updatingStatus = true;
    this.orderService.updateOrderStatus(this.selectedOrderId, data.status, data.reason).subscribe({
      next: (response) => {
        this.statusModal.hide();
        this.updatingStatus = false;
        this.loadOrders();
      },
      error: (err) => {
        console.error('Error updating order status:', err);
        this.updatingStatus = false;
        this.statusModal.hide();
      }
    });
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