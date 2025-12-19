import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { ProductService, Product, ProductPage, PaginationParams, SearchParams } from '../../services/product';
import { environment } from '../../../environments/environment';
import { PaginationComponent } from '../pagination/pagination';
import { ProductSearchComponent } from '../product-search/product-search';

@Component({
  selector: 'app-home',
  imports: [CommonModule, PaginationComponent, ProductSearchComponent],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home implements OnInit {
  products: Product[] = [];
  loading: boolean = false;
  error: string | null = null;
  
  // Pagination properties
  currentPage: number = 0;
  pageSize: number = 12;
  totalPages: number = 0;
  totalElements: number = 0;
  sortBy: string = 'name';
  sortDir: 'asc' | 'desc' = 'asc';
  
  // Search properties
  currentSearchFilters: SearchParams = {};
  hasActiveFilters: boolean = false;

  constructor(private productService: ProductService, private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.loadProducts();
  }

  loadProducts() {
    this.loading = true;
    this.error = null;
    
    // Use search if there are active filters, otherwise use basic pagination
    if (this.hasActiveFilters) {
      const searchParams: SearchParams = {
        ...this.currentSearchFilters,
        page: this.currentPage,
        size: this.pageSize
      };
      
      this.productService.searchProducts(searchParams).subscribe({
        next: (productPage: ProductPage) => {
          this.products = productPage.content;
          this.totalPages = productPage.totalPages;
          this.totalElements = productPage.totalElements;
          this.loading = false;
          this.loadImages();
        },
        error: (err) => {
          this.error = err.error?.Error || err.message || 'Failed to load products';
          this.loading = false;
        }
      });
    } else {
      const params: PaginationParams = {
        page: this.currentPage,
        size: this.pageSize,
        sortBy: this.sortBy,
        sortDir: this.sortDir
      };
      
      this.productService.getProductsPaginated(params).subscribe({
        next: (productPage: ProductPage) => {
          this.products = productPage.content;
          this.totalPages = productPage.totalPages;
          this.totalElements = productPage.totalElements;
          this.loading = false;
          this.loadImages();
        },
        error: (err) => {
          this.error = err.error?.Error || err.message || 'Failed to load products';
          this.loading = false;
        }
      });
    }
  }

  onSearchChanged(filters: SearchParams) {
    this.currentSearchFilters = filters ;
    this.currentPage = 0; // Reset to first page when search changes
    
    // Check if there are active filters
    this.hasActiveFilters = !!(filters.name || filters.minPrice !== undefined || 
                              filters.maxPrice !== undefined || filters.sellerName);
    
    // Update sort properties if provided
    if (filters.sortBy) this.sortBy = filters.sortBy;
    if (filters.sortDir) this.sortDir = filters.sortDir;
    
    this.loadProducts();
  }

  loadImages() {
    for (let product of this.products) {
      product.imageUrl = 'https://placehold.co/300x200?text=No+Image';
      if (product.imageMediaIds && product.imageMediaIds.length > 0) {
        this.http.get(`${environment.apiUrl}/media/${product.imageMediaIds[0]}`).subscribe({
          next: (response: any) => {
            product.imageUrl = response.base64Data.startsWith('data:') 
              ? response.base64Data 
              : 'data:' + response.contentType + ';base64,' + response.base64Data;
          },
          error: () => {
            // Keep the placeholder
          }
        });
      }
    }
  }

  navigateToProduct(productId: string) {
    this.router.navigate(['/products', productId]);
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadProducts();
  }

  onPageSizeChange(newSize: number): void {
    this.pageSize = newSize;
    this.currentPage = 0; // Reset to first page
    this.loadProducts();
  }
}
