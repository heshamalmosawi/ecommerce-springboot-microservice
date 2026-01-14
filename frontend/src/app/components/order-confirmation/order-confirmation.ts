import { Component, effect, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Order } from '../../services/order';
import { Toast } from '../toast/toast';

@Component({
  selector: 'app-order-confirmation',
  imports: [CommonModule, Toast],
  templateUrl: './order-confirmation.html',
  styleUrl: './order-confirmation.scss'
})
export class OrderConfirmation {
  private router = inject(Router);
  order: Order | null = null;

  constructor() {
    effect(() => {
      const navigation = this.router.currentNavigation();
      if (navigation?.extras?.state?.['order']) {
        this.order = navigation.extras.state['order'] as Order;
      }
    }, { allowSignalWrites: true });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'PENDING':
        return '#f59e0b';
      case 'PROCESSING':
        return '#3b82f6';
      case 'FAILED':
        return '#ef4444';
      case 'SHIPPED':
        return '#8b5cf6';
      case 'DELIVERED':
        return '#10b981';
      default:
        return '#6b7280';
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  continueShopping() {
    this.router.navigate(['/']);
  }
}
