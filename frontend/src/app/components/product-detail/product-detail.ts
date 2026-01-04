import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService, Product } from '../../services/product';
import { AuthService } from '../../services/auth';
import { CartService } from '../../services/cart';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-product-detail',
  imports: [CommonModule],
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.scss'
})
export class ProductDetail implements OnInit {
  product: Product | null = null;
  loading: boolean = false;
  error: string | null = null;
  addingToCart: boolean = false;
  cartMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private authService: AuthService,
    private cartService: CartService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    const productId = this.route.snapshot.paramMap.get('id');
    if (productId) {
      this.loadProduct(productId);
    } else {
      this.error = 'Product ID not found';
    }
  }

  loadProduct(productId: string) {
    this.loading = true;
    this.error = null;
    
    this.productService.getProductById(productId).subscribe({
      next: (product) => {
        this.product = product;
        this.loadProductImages();
        this.loading = false;
      },
      error: (err) => {
        if (err.status === 404) {
          this.error = 'Product not found';
        } else {
          this.error = err.error?.Error || err.message || 'Failed to load product';
        }
        this.loading = false;
      }
    });
  }

  loadProductImages() {
    if (this.product && this.product.imageMediaIds && this.product.imageMediaIds.length > 0) {
      this.product.imageUrls = [];
      
      this.product.imageMediaIds.forEach((mediaId, index) => {
        this.http.get(`${environment.apiUrl}/media/${mediaId}`).subscribe({
          next: (response: any) => {
            if (this.product && this.product.imageUrls) {
              const imageUrl = response.base64Data.startsWith('data:') 
                ? response.base64Data 
                : 'data:' + response.contentType + ';base64,' + response.base64Data;
              this.product.imageUrls[index] = imageUrl;
              
              // Set the first image as the main image
              if (index === 0) {
                this.product.imageUrl = imageUrl;
              }
            }
          },
          error: () => {
            if (this.product && this.product.imageUrls) {
              this.product.imageUrls[index] = 'https://placehold.co/600x400?text=Image+Not+Found';
              
              // If this is the first image and it failed, set placeholder
              if (index === 0 && !this.product.imageUrl) {
                this.product.imageUrl = 'https://placehold.co/600x400?text=No+Image';
              }
            }
          }
        });
      });
    } else if (this.product) {
      this.product.imageUrl = 'https://placehold.co/600x400?text=No+Image';
      this.product.imageUrls = [];
    }
  }

  selectImage(imageUrl: string) {
    if (this.product) {
      this.product.imageUrl = imageUrl;
    }
  }

  isProductOwner(): boolean {
    if (!this.product) {
      return false;
    }
    
    const currentUser = this.authService.getCurrentUserValue();
    if (!currentUser) {
      return false;
    }
    
    return this.product.sellerName === currentUser.name;
  }

  editProduct(): void {
    if (this.product) {
      this.router.navigate(['/products', this.product.id, 'edit']);
    }
  }

  addToCart() {
    if (!this.product) return;
    
    this.addingToCart = true;
    this.cartMessage = null;

    const productToAdd = { ...this.product, quantity: 1 };
    this.cartService.addOrUpdateItem(productToAdd);
    this.cartMessage = 'Added to cart successfully!';

    setTimeout(() => {
      this.cartMessage = null;
    }, 3000);
  }

  goBack() {
    this.router.navigate(['/']);
  }
}