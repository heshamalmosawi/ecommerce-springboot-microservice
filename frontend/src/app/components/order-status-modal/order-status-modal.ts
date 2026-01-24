import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-order-status-modal',
  imports: [CommonModule, FormsModule],
  templateUrl: './order-status-modal.html',
  styleUrl: './order-status-modal.scss'
})
export class OrderStatusModal {
  @Input() currentStatus = '';
  @Output() confirm = new EventEmitter<{status: string, reason: string}>();
  @Output() cancel = new EventEmitter<void>();

  visible = false;
  loading = false;
  selectedStatus = '';
  reason = '';

  private readonly statusTransitions: { [key: string]: string[] } = {
    'PENDING': ['PROCESSING', 'CANCELLED'],
    'PROCESSING': ['SHIPPED', 'CANCELLED'],
    'SHIPPED': ['DELIVERED']
  };

  get validStatuses(): string[] {
    return this.statusTransitions[this.currentStatus] || [];
  }

  get canConfirm(): boolean {
    return !!this.selectedStatus && !this.loading;
  }

  show(): void {
    this.visible = true;
    this.selectedStatus = '';
    this.reason = '';
    this.loading = false;
  }

  hide(): void {
    this.visible = false;
    this.selectedStatus = '';
    this.reason = '';
    this.loading = false;
  }

  onConfirm(): void {
    if (!this.canConfirm) return;
    this.loading = true;
    this.confirm.emit({
      status: this.selectedStatus,
      reason: this.reason
    });
  }

  onCancel(): void {
    this.hide();
    this.cancel.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.onCancel();
    }
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.onCancel();
    }
  }
}
