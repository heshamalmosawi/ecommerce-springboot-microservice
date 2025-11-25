import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.html',
  styleUrls: ['./pagination.scss']
})
export class PaginationComponent {
  @Input() currentPage: number = 0;
  @Input() totalPages: number = 0;
  @Input() totalElements: number = 0;
  @Input() pageSize: number = 12;
  @Input() pageSizeOptions: number[] = [12, 24, 48];
  @Output() pageChange = new EventEmitter<number>();
  @Output() pageSizeChange = new EventEmitter<number>();

  get pages(): number[] {
    const pages: number[] = [];
    const startPage = Math.max(0, this.currentPage - 2);
    const endPage = Math.min(this.totalPages - 1, this.currentPage + 2);
    
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    
    return pages;
  }

  get startItem(): number {
    return this.currentPage * this.pageSize + 1;
  }

  get endItem(): number {
    const end = this.startItem + this.pageSize - 1;
    return Math.min(end, this.totalElements);
  }

  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.pageChange.emit(page);
    }
  }

  onPageSizeChange(newSize: number): void {
    if (newSize !== this.pageSize) {
      this.pageSizeChange.emit(newSize);
    }
  }

  goToFirst(): void {
    this.onPageChange(0);
  }

  goToPrevious(): void {
    this.onPageChange(this.currentPage - 1);
  }

  goToNext(): void {
    this.onPageChange(this.currentPage + 1);
  }

  goToLast(): void {
    this.onPageChange(this.totalPages - 1);
  }
}