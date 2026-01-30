# Reorder API Specification - Frontend Integration

## Overview

This document provides complete specifications for integrating the reorder functionality into the frontend application. The reorder API allows users to quickly add items from their past orders to their shopping cart for repurchase.

**Base URL**: `/orders`
**Endpoint**: `GET /orders/{orderId}/reorder`
**Authentication**: Required (JWT Bearer token)

---

## Feature Description

The reorder feature enables users to:
1. View items from a previously completed order
2. Check current availability and pricing of each item
3. Receive warnings for limited stock or price changes
4. Add available items to their cart with a single action

**Target Users**: Buyers/Customers with order history
**Use Cases**: Quick repurchase of frequently bought items

---

## API Specification

### Request

#### Endpoint
```
GET /orders/{orderId}/reorder
```

#### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| orderId | string | Yes | The ID of the original order to reorder from |

#### Headers

| Header | Type | Required | Description |
|--------|------|----------|-------------|
| Authorization | string | Yes | JWT Bearer token (e.g., `Bearer <token>`) |

#### Example Request
```typescript
// Using Angular HttpClient
this.http.get(`/orders/${orderId}/reorder`, {
  headers: {
    'Authorization': `Bearer ${authService.getToken()}`
  }
});
```

---

### Response

#### Success Response (200 OK)

```typescript
interface ReorderResponseDTO {
  availableItems: ReorderItem[];
  unavailableItems: UnavailableItem[];
  warnings: string[];
  originalOrderId: string;
  fetchedAt: string;
}

interface ReorderItem {
  productId: string;
  productName: string;
  requestedQuantity: number;
  availableQuantity: number;
  originalPrice: number;
  currentPrice: number;
  imageUrl?: string;
}

interface UnavailableItem {
  productId: string;
  productName: string;
  requestedQuantity: number;
  reason: string;
}
```

#### Example Response
```json
{
  "availableItems": [
    {
      "productId": "prod123",
      "productName": "Wireless Headphones",
      "requestedQuantity": 2,
      "availableQuantity": 2,
      "originalPrice": 99.99,
      "currentPrice": 99.99,
      "imageUrl": "https://media.example.com/image.jpg"
    },
    {
      "productId": "prod456",
      "productName": "USB Cable",
      "requestedQuantity": 5,
      "availableQuantity": 3,
      "originalPrice": 9.99,
      "currentPrice": 12.99,
      "imageUrl": null
    }
  ],
  "unavailableItems": [
    {
      "productId": "prod789",
      "productName": "Phone Case",
      "requestedQuantity": 1,
      "reason": "Out of stock"
    }
  ],
  "warnings": [
    "Only 3 of 5 'USB Cable' available (limited stock)",
    "Price changed for 'USB Cable': 9.99 → 12.99"
  ],
  "originalOrderId": "order-abc-123",
  "fetchedAt": "2026-01-28T10:30:00.123456"
}
```

---

### Error Responses

#### 400 Bad Request
```json
{
  "error": "Order not found: order-abc-123"
}
```
**Scenarios:**
- Order ID doesn't exist
- Order has no items

#### 401 Unauthorized
```json
{
  "error": "You are not authorized to reorder this order."
}
```
**Scenarios:**
- User doesn't own the order
- Invalid or expired JWT token

#### 500 Internal Server Error
```json
{
  "error": "Unable to verify product availability. Please try again later."
}
```
**Scenarios:**
- Product-service is down
- Network issues between services

---

## Frontend Implementation Guide

### Step 1: Create TypeScript Interfaces

Create these interfaces in `src/app/models/` or similar location:

```typescript
// src/app/models/reorder.interface.ts
export interface ReorderResponse {
  availableItems: ReorderItem[];
  unavailableItems: UnavailableItem[];
  warnings: string[];
  originalOrderId: string;
  fetchedAt: string;
}

export interface ReorderItem {
  productId: string;
  productName: string;
  requestedQuantity: number;
  availableQuantity: number;
  originalPrice: number;
  currentPrice: number;
  imageUrl?: string;
}

export interface UnavailableItem {
  productId: string;
  productName: string;
  requestedQuantity: number;
  reason: string;
}
```

---

### Step 2: Create Service Method

Add method to existing order service:

```typescript
// src/app/services/order.service.ts
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReorderResponse } from '../models/reorder.interface';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  constructor(private http: HttpClient) { }

  getItemsForReorder(orderId: string): Observable<ReorderResponse> {
    return this.http.get<ReorderResponse>(`/orders/${orderId}/reorder`);
  }
}
```

---

### Step 3: Create Reorder Component

```typescript
// src/app/components/reorder-modal/reorder-modal.component.ts
import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../services/order.service';
import { ReorderResponse } from '../../models/reorder.interface';

@Component({
  selector: 'app-reorder-modal',
  templateUrl: './reorder-modal.component.html',
  styleUrls: ['./reorder-modal.component.scss']
})
export class ReorderModalComponent implements OnInit {
  reorderData: ReorderResponse | null = null;
  loading = false;
  error: string | null = null;

  constructor(
    private orderService: OrderService,
    @Inject(MAT_DIALOG_DATA) public data: { orderId: string },
    private dialogRef: MatDialogRef<ReorderModalComponent>
  ) {}

  ngOnInit(): void {
    this.loadReorderData();
  }

  loadReorderData(): void {
    this.loading = true;
    this.error = null;

    this.orderService.getItemsForReorder(this.data.orderId).subscribe({
      next: (response) => {
        this.reorderData = response;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.error || 'Failed to load reorder data';
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
    if (!this.reorderData) return;

    // Implement add to cart logic here
    // Add all availableItems to cart
    this.reorderData.availableItems.forEach(item => {
      // Call cart service to add item
      console.log('Adding to cart:', item);
    });

    this.dialogRef.close('added');
  }

  close(): void {
    this.dialogRef.close();
  }
}
```

---

### Step 4: Create Reorder Modal Template

```html
<!-- src/app/components/reorder-modal/reorder-modal.component.html -->
<h2 mat-dialog-title>Reorder Items</h2>

<div mat-dialog-content>
  <!-- Loading State -->
  <div *ngIf="loading" class="loading-container">
    <mat-spinner></mat-spinner>
    <p>Checking item availability...</p>
  </div>

  <!-- Error State -->
  <mat-error *ngIf="error">
    {{ error }}
  </mat-error>

  <!-- Reorder Data -->
  <div *ngIf="reorderData && !loading">
    <!-- Warnings -->
    <div *ngIf="reorderData.warnings.length > 0" class="warnings-section">
      <mat-card>
        <mat-card-header>
          <mat-card-title>⚠️ Please Note</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <ul>
            <li *ngFor="let warning of reorderData.warnings">{{ warning }}</li>
          </ul>
        </mat-card-content>
      </mat-card>
    </div>

    <!-- Available Items -->
    <div *ngIf="reorderData.availableItems.length > 0" class="available-section">
      <h3>Available for Reorder ({{ reorderData.availableItems.length }})</h3>
      
      <mat-list>
        <mat-list-item *ngFor="let item of reorderData.availableItems">
          <img *ngIf="item.imageUrl" [src]="item.imageUrl" alt="" class="item-image">
          <div class="item-info">
            <h4 mat-line>{{ item.productName }}</h4>
            <p mat-line>Quantity: {{ item.requestedQuantity }}</p>
            <p mat-line class="price-info">
              <span *ngIf="hasPriceChanged(item)" class="price-changed">
                Was: ${{ item.originalPrice.toFixed(2) }} → Now: ${{ item.currentPrice.toFixed(2) }}
              </span>
              <span *ngIf="!hasPriceChanged(item)">
                ${{ item.currentPrice.toFixed(2) }}
              </span>
            </p>
            <div *ngIf="hasLimitedStock(item)" class="limited-stock">
              ⚠️ Limited Stock: Only {{ item.availableQuantity }} available
            </div>
          </div>
        </mat-list-item>
      </mat-list>
    </div>

    <!-- Unavailable Items -->
    <div *ngIf="reorderData.unavailableItems.length > 0" class="unavailable-section">
      <h3>Unavailable ({{ reorderData.unavailableItems.length }})</h3>
      
      <mat-list>
        <mat-list-item *ngFor="let item of reorderData.unavailableItems">
          <div class="item-info">
            <h4 mat-line>{{ item.productName }}</h4>
            <p mat-line>Requested: {{ item.requestedQuantity }}</p>
            <p mat-line class="unavailable-reason">{{ item.reason }}</p>
          </div>
        </mat-list-item>
      </mat-list>
    </div>
  </div>
</div>

<div mat-dialog-actions>
  <button mat-button (click)="close()">Cancel</button>
  <button 
    mat-raised-button 
    color="primary"
    (click)="addToCart()"
    [disabled]="!reorderData || reorderData.availableItems.length === 0">
    Add Available Items to Cart
  </button>
</div>
```

---

### Step 5: Add Reorder Button to Order History

```html
<!-- In order history component -->
<tr *ngFor="let order of orders">
  <td>{{ order.id }}</td>
  <td>{{ order.createdAt | date:'medium' }}</td>
  <td>{{ order.status }}</td>
  <td>{{ order.totalPrice | currency }}</td>
  <td>
    <button 
      mat-button 
      color="accent"
      (click)="openReorderDialog(order.id)"
      [disabled]="order.status !== 'DELIVERED'">
      Reorder
    </button>
  </td>
</tr>
```

```typescript
// In order history component
import { MatDialog } from '@angular/material/dialog';
import { ReorderModalComponent } from '../components/reorder-modal/reorder-modal.component';

constructor(private dialog: MatDialog) {}

openReorderDialog(orderId: string): void {
  const dialogRef = this.dialog.open(ReorderModalComponent, {
    width: '600px',
    data: { orderId }
  });

  dialogRef.afterClosed().subscribe(result => {
    if (result === 'added') {
      // Refresh cart, show success message, etc.
      this.loadCart();
    }
  });
}
```

---

## UI/UX Guidelines

### When to Show Reorder Button

✅ **Show reorder button when:**
- Order status is `DELIVERED`
- User is authenticated
- Order has items

❌ **Hide/disable reorder button when:**
- Order is `PENDING`, `PROCESSING`, `SHIPPED`, `CANCELLED`, or `FAILED`
- Order has no items
- User is not authenticated

### User Flow

1. User navigates to Order History
2. User clicks "Reorder" button on a delivered order
3. Reorder modal opens with loading state
4. API call fetches current product data
5. Display categorized items (available vs. unavailable)
6. Show warnings for price changes and limited stock
7. User confirms to add available items to cart
8. Modal closes, cart is updated
9. Show success notification

### Visual Design Recommendations

**Available Items:**
- Green checkmark or ✓ icon
- Full product details with image
- Price highlighted if changed
- Stock level indicator

**Unavailable Items:**
- Red X or ✗ icon
- Grayed out or dimmed
- Clear reason displayed

**Warnings:**
- Yellow warning icon (⚠️)
- Prominent but not alarming
- List format for multiple warnings

---

## Error Handling

### Display User-Friendly Messages

```typescript
// In reorder modal component
handleError(error: any): void {
  if (error.status === 401) {
    this.error = 'Please log in to reorder items';
    // Redirect to login
  } else if (error.status === 404) {
    this.error = 'Order not found';
  } else if (error.status === 500) {
    this.error = 'Service temporarily unavailable. Please try again later.';
  } else {
    this.error = 'An error occurred. Please try again.';
  }
}
```

### Retry Mechanism

Consider adding a "Retry" button for failed requests:

```html
<div *ngIf="error" class="error-section">
  <p>{{ error }}</p>
  <button mat-button color="primary" (click)="loadReorderData()">
    Retry
  </button>
</div>
```

---

## Testing Checklist

### Manual Testing

- [ ] Reorder button appears only for delivered orders
- [ ] Reorder modal opens with loading indicator
- [ ] Available items display correctly
- [ ] Unavailable items display with reasons
- [ ] Warnings show for price changes
- [ ] Warnings show for limited stock
- [ ] "Add to Cart" button adds all available items
- [ ] Modal closes successfully after adding to cart
- [ ] Error messages display correctly for 400, 401, 500 errors
- [ ] Unauthorized access shows proper error

### Integration Testing

- [ ] API call returns 200 for valid orders
- [ ] API call returns 401 for orders not owned by user
- [ ] API call returns 404 for non-existent orders
- [ ] Cart service receives correct items on "Add to Cart"
- [ ] Auth token is properly sent with request

---

## Performance Considerations

1. **Lazy Loading**: Only fetch reorder data when modal is opened
2. **Caching**: Consider caching product data for short duration
3. **Pagination**: If orders have many items, consider pagination in modal
4. **Image Loading**: Lazy load product images in modal

---

## Accessibility

- Ensure all buttons have proper aria-labels
- Use semantic HTML (h3, h4, ul, li)
- Provide keyboard navigation for modal
- Screen reader support for warnings and errors
- Color contrast ratios meet WCAG AA standards

---

## Browser Support

Test on:
- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)
- Mobile browsers (iOS Safari, Chrome Mobile)

---

## FAQ

**Q: Can users reorder items from cancelled orders?**
A: No, frontend should only show reorder button for delivered orders.

**Q: What happens if price changed between orders?**
A: API returns warning with old and new prices. Frontend should display warning prominently.

**Q: What if only some items are available?**
A: Frontend should allow adding only available items. Unavailable items are displayed separately.

**Q: How to handle long item names or many items?**
A: Use scrolling container in modal with max-height. Truncate long names with ellipsis and show tooltip on hover.

**Q: Should we allow modifying quantities before adding to cart?**
A: Current API doesn't support this. Available quantity is fixed. Future enhancement could allow quantity adjustment.

---

## API Contact

For questions about the backend API:
- Backend Implementation Guide: `backend/REORDER_API_IMPLEMENTATION.md`
- Contact: Backend development team
- Service: `order-service`
- Endpoint: `/orders/{orderId}/reorder`

---

## Changelog

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-01-28 | Initial specification for reorder API integration |
