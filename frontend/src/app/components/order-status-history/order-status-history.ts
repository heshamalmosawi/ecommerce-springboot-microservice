import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatusHistory, OrderStatus } from '../../services/order';

@Component({
  selector: 'app-order-status-history',
  imports: [CommonModule],
  templateUrl: './order-status-history.html',
  styleUrl: './order-status-history.scss'
})
export class OrderStatusHistory {
  @Input() statusHistory: StatusHistory[] | null = null;
  @Input() fallbackStatus?: string;
  @Input() fallbackDate?: string;

  get hasStatusHistory(): boolean {
    return this.statusHistory !== null && this.statusHistory.length > 0;
  }

  get sortedHistory(): StatusHistory[] {
    if (!this.statusHistory) return [];
    return [...this.statusHistory].sort((a, b) =>
      new Date(a.changedAt).getTime() - new Date(b.changedAt).getTime()
    );
  }

  getStatusClass(status: OrderStatus | string): string {
    const statusLower = String(status).toLowerCase();
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

  getStatusIcon(status: OrderStatus | string): string {
    const statusLower = String(status).toLowerCase();
    switch (statusLower) {
      case 'pending':
        return '⏳';
      case 'processing':
        return '⚙️';
      case 'shipped':
        return '🚚';
      case 'delivered':
        return '✅';
      case 'cancelled':
        return '❌';
      case 'failed':
        return '⚠️';
      default:
        return '📋';
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

  isLatest(index: number): boolean {
    return index === this.sortedHistory.length - 1;
  }
}