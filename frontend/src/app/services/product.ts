import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  quantity: number;
  userId: string;
  imageMediaIds: string[];
  imageUrl?: string;
}

export interface ProductDTO {
  name: string;
  description: string;
  price: number;
  quantity: number;
}

export interface ProductWithImagesDTO extends ProductDTO {
  images: string[]; // Array of base64 encoded images
}

export interface CreateProductResponse {
  id: string;
  name: string;
  description: string;
  price: number;
  quantity: number;
  userId: string;
  imageMediaIds: string[];
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly API_URL = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.API_URL}/products`).pipe(
      catchError(error => {
        console.error('Error fetching products:', error);
        return throwError(() => error);
      })
    );
  }

  getProductById(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.API_URL}/products/${id}`).pipe(
      catchError(error => {
        console.error('Error fetching product:', error);
        return throwError(() => error);
      })
    );
  }

  createProduct(productData: ProductDTO): Observable<CreateProductResponse> {
    return this.http.post<CreateProductResponse>(`${this.API_URL}/products`, productData).pipe(
      map(response => {
        console.log('Product created successfully:', response);
        return response;
      }),
      catchError(error => {
        console.error('Error creating product:', error);
        let errorMessage = 'Failed to create product';
        
        if (error.error && typeof error.error === 'string') {
          errorMessage = error.error.replace('Error: ', '');
        } else if (error.message) {
          errorMessage = error.message;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  createProductWithImages(productData: ProductWithImagesDTO): Observable<CreateProductResponse> {
    return this.http.post<CreateProductResponse>(`${this.API_URL}/products`, productData).pipe(
      map(response => {
        console.log('Product created successfully with images:', response);
        return response;
      }),
      catchError(error => {
        console.error('Error creating product with images:', error);
        let errorMessage = 'Failed to create product';
        
        if (error.error && typeof error.error === 'string') {
          errorMessage = error.error.replace('Error: ', '');
        } else if (error.message) {
          errorMessage = error.message;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  updateProduct(id: string, productData: ProductDTO): Observable<Product> {
    return this.http.put<Product>(`${this.API_URL}/products/${id}`, productData).pipe(
      catchError(error => {
        console.error('Error updating product:', error);
        let errorMessage = 'Failed to update product';
        
        if (error.error && typeof error.error === 'string') {
          errorMessage = error.error.replace('Error: ', '');
        } else if (error.message) {
          errorMessage = error.message;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  patchProduct(id: string, productData: Partial<ProductDTO>): Observable<Product> {
    return this.http.patch<Product>(`${this.API_URL}/products/${id}`, productData).pipe(
      catchError(error => {
        console.error('Error patching product:', error);
        let errorMessage = 'Failed to update product';
        
        if (error.error && typeof error.error === 'string') {
          errorMessage = error.error.replace('Error: ', '');
        } else if (error.message) {
          errorMessage = error.message;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  deleteProduct(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/products/${id}`).pipe(
      catchError(error => {
        console.error('Error deleting product:', error);
        let errorMessage = 'Failed to delete product';
        
        if (error.error && typeof error.error === 'string') {
          errorMessage = error.error.replace('Error: ', '');
        } else if (error.message) {
          errorMessage = error.message;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }
}