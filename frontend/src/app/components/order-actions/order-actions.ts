import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-order-actions',
  imports: [CommonModule],
  templateUrl: './order-actions.html',
  styleUrl: './order-actions.scss'
})
export class OrderActions {
  @Input() orderId = '';
  @Input() currentStatus = '';
  @Input() userRole: 'client' | 'seller' = 'client';
  @Output() updateStatusClick = new EventEmitter<void>();
  @Output() cancelClick = new EventEmitter<void>();
  @Output() reorderClick = new EventEmitter<void>();

  get canCancel(): boolean {
    return ['PENDING', 'PROCESSING'].includes(this.currentStatus);
  }

  get canUpdateStatus(): boolean {
    if (this.userRole !== 'seller') {
      return false;
    }
    return ['PENDING', 'PROCESSING', 'SHIPPED'].includes(this.currentStatus);
  }

  get canReorder(): boolean {
    return ['DELIVERED'].includes(this.currentStatus);
  }

  get showActions(): boolean {
    return this.canCancel || this.canUpdateStatus || this.canReorder;
  }

  onUpdateStatus(): void {
    this.updateStatusClick.emit();
  }

  onCancelOrder(): void {
    this.cancelClick.emit();
  }

  onReorder(): void {
    this.reorderClick.emit();
  }
}
