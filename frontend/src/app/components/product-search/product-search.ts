import { Component, EventEmitter, Output, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

export interface SearchFilters {
  name?: string;
  minPrice?: number;
  maxPrice?: number;
  sellerName?: string;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  page?: number;
  size?: number;
}

@Component({
  selector: 'app-product-search',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './product-search.html',
  styleUrl: './product-search.scss'
})
export class ProductSearchComponent implements OnInit, OnDestroy {
  @Output() searchChanged = new EventEmitter<SearchFilters>();
  
  searchForm: FormGroup;
  isCollapsed = false;
  private destroy$ = new Subject<void>();

  sortOptions = [
    { value: 'name', label: 'Name' },
    { value: 'price', label: 'Price' },
    { value: 'quantity', label: 'Quantity' },
    { value: 'description', label: 'Description' }
  ];

  sortDirections = [
    { value: 'asc', label: 'Ascending (A-Z, 0-9)' },
    { value: 'desc', label: 'Descending (Z-A, 9-0)' }
  ];

  constructor(private fb: FormBuilder) {
    this.searchForm = this.fb.group({
      name: [''],
      minPrice: [null, [Validators.min(0)]],
      maxPrice: [null, [Validators.min(0)]],
      sellerName: [''],
      sortBy: ['name'],
      sortDir: ['asc']
    });
  }

  ngOnInit() {
    this.searchForm.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged((prev, curr) => JSON.stringify(prev) === JSON.stringify(curr)),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.onSearchChange();
    });
  }

  onSearchChange() {
    const formValues = this.searchForm.value;
    
    // Build search filters
    const filters: SearchFilters = {
      name: formValues.name || undefined,
      minPrice: formValues.minPrice ?? undefined,
      maxPrice: formValues.maxPrice ?? undefined,
      sellerName: formValues.sellerName || undefined,
      sortBy: formValues.sortBy,
      sortDir: formValues.sortDir,
      page: 0, // Reset to first page on search
      size: 12
    };

    this.searchChanged.emit(filters);
  }

  resetFilters() {
    this.searchForm.patchValue({
      name: '',
      minPrice: null,
      maxPrice: null,
      sellerName: '',
      sortBy: 'name',
      sortDir: 'asc'
    });
  }

  toggleSidebar() {
    this.isCollapsed = !this.isCollapsed;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}