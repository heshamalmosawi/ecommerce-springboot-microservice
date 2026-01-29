import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService, ReorderResponse, ReorderItem } from '../../services/order';
import { CartService } from '../../services/cart';
import { Product } from '../../services/product';

@Component({
  selector: 'app-reorder-modal',
  imports: [CommonModule],
  templateUrl: './reorder-modal.html',
  styleUrl: './reorder-modal.scss'
})
export class ReorderModal implements OnInit {
  private orderService = inject(OrderService);
  private cartService = inject(CartService);

  reorderData: ReorderResponse | null = null;
  loading = false;
  error: string | null = null;
  orderId = '';
  addingToCart = false;

  visible = false;
  private onCloseCallback: ((result: string) => void) | null = null;

  ngOnInit(): void {}

  show(orderId: string): void {
    this.orderId = orderId;
    this.visible = true;
    this.reorderData = null;
    this.error = null;
    this.addingToCart = false;
    this.loadReorderData();
  }

  hide(): void {
    this.visible = false;
    this.onCloseCallback?.('');
    this.onCloseCallback = null;
  }

  onClose(callback: (result: string) => void): void {
    this.onCloseCallback = callback;
  }

  loadReorderData(): void {
    this.loading = true;
    this.error = null;

    this.orderService.getItemsForReorder(this.orderId).subscribe({
      next: (response) => {
        this.reorderData = response;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Failed to load reorder data';
        this.loading = false;
      }
    });
  }

  hasPriceChanged(item: ReorderItem): boolean {
    return item.originalPrice !== item.currentPrice;
  }

  hasLimitedStock(item: ReorderItem): boolean {
    return item.availableQuantity < item.requestedQuantity;
  }

  addToCart(): void {
    if (!this.reorderData || this.reorderData.availableItems.length === 0) return;

    this.addingToCart = true;

    this.reorderData.availableItems.forEach(item => {
      const product: Product = {
        id: item.productId,
        name: item.productName,
        description: '',
        price: item.currentPrice,
        quantity: item.availableQuantity,
        sellerName: '',
        imageMediaIds: [],
        imageUrl: item.imageUrl
      };
      this.cartService.addOrUpdateItem(product);
    });

    setTimeout(() => {
      this.addingToCart = false;
      this.hide();
    }, 500);
  }

  retry(): void {
    this.loadReorderData();
  }

  close(): void {
    this.hide();
  }
}
