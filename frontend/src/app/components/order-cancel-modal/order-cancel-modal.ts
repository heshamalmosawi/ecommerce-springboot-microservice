import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-order-cancel-modal',
  imports: [CommonModule, FormsModule],
  templateUrl: './order-cancel-modal.html',
  styleUrl: './order-cancel-modal.scss'
})
export class OrderCancelModal {
  @Output() confirm = new EventEmitter<string>();
  @Output() cancelled = new EventEmitter<void>();

  visible = false;
  loading = false;
  reason = '';

  show(): void {
    this.visible = true;
    this.reason = '';
    this.loading = false;
  }

  hide(): void {
    this.visible = false;
    this.reason = '';
    this.loading = false;
  }

  onConfirm(): void {
    if (this.loading) return;
    this.loading = true;
    this.confirm.emit(this.reason);
  }

  onCancel(): void {
    this.hide();
    this.cancelled.emit();
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
