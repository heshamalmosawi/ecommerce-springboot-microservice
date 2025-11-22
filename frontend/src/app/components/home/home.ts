import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ProductService, Product } from '../../services/product';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-home',
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home implements OnInit {
  products: Product[] = [];
  loading: boolean = false;
  error: string | null = null;

  constructor(private productService: ProductService, private http: HttpClient) {}

  ngOnInit() {
    this.loadProducts();
  }

  loadProducts() {
    this.loading = true;
    this.error = null;
    
    this.productService.getProducts().subscribe({
      next: (products) => {
        this.products = products;
        this.loading = false;
        this.loadImages();
      },
      error: (err) => {
        this.error = err.message || 'Failed to load products';
        this.loading = false;
      }
    });
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
}
