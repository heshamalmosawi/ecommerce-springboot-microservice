import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductService, CreateProductResponse } from '../../services/product';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-add-product',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './add-product.html',
  styleUrl: './add-product.scss'
})
export class AddProductComponent {
  productForm: FormGroup;
  isSubmitting = false;
  selectedFiles: File[] = [];
  imagePreviews: string[] = [];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private productService: ProductService,
    private authService: AuthService
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      price: ['', [Validators.required, Validators.min(0.01)]],
      quantity: ['', [Validators.required, Validators.min(0)]]
    });
  }

  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const files = Array.from(input.files);
      
      // Validate file types
      const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
      const validFiles = files.filter(file => validTypes.includes(file.type));
      
      if (validFiles.length !== files.length) {
        alert('Only JPEG, PNG, and WebP images are allowed');
        return;
      }
      
      // Validate file size (max 5MB per file)
      const maxSize = 5 * 1024 * 1024; // 5MB
      const oversizedFiles = validFiles.filter(file => file.size > maxSize);
      
      if (oversizedFiles.length > 0) {
        alert('Images must be smaller than 5MB each');
        return;
      }
      
      this.selectedFiles = [...this.selectedFiles, ...validFiles];
      
      // Generate previews
      validFiles.forEach(file => {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.imagePreviews.push(e.target?.result as string);
        };
        reader.readAsDataURL(file);
      });
    }
  }

  removeImage(index: number): void {
    this.selectedFiles.splice(index, 1);
    this.imagePreviews.splice(index, 1);
  }

  async onSubmit(): Promise<void> {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    if (this.selectedFiles.length === 0) {
      alert('Please add at least one product image');
      return;
    }

    // Check if user has seller role
    if (!this.authService.hasRole('seller')) {
      alert('Only sellers can add products');
      return;
    }

    this.isSubmitting = true;
    
    try {
      // Convert all images to base64
      const imageBase64Array: string[] = [];
      for (const file of this.selectedFiles) {
        const base64 = await this.convertFileToBase64(file);
        imageBase64Array.push(base64);
      }
      
      // Create product data with images
      const productData = {
        name: this.productForm.value.name,
        description: this.productForm.value.description,
        price: parseFloat(this.productForm.value.price),
        quantity: parseInt(this.productForm.value.quantity, 10),
        images: imageBase64Array
      };
      
      // Create product with images
      this.productService.createProductWithImages(productData).subscribe({
        next: (createdProduct: CreateProductResponse) => {
          console.log('Product created successfully:', createdProduct);
          this.isSubmitting = false;
          // Navigate back to dashboard or products list
          this.router.navigate(['/']);
        },
        error: (error: any) => {
          console.error('Error creating product:', error);
          alert(`Failed to create product: ${error.message}`);
          this.isSubmitting = false;
        }
      });
      
    } catch (error) {
      console.error('Error processing images:', error);
      alert('Failed to process images. Please try again.');
      this.isSubmitting = false;
    }
  }

  private convertFileToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        resolve(reader.result as string);
      };
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  cancel(): void {
    this.router.navigate(['/']);
  }
}