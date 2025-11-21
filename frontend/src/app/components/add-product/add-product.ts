import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductService, CreateProductResponse, ProductWithImagesDTO } from '../../services/product';
import { AuthService } from '../../services/auth';
import { ImageUtilsService } from '../../services/image-utils';

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
    private authService: AuthService,
    private imageUtils: ImageUtilsService
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      price: ['', [Validators.required, Validators.min(0.01)]],
      quantity: ['', [Validators.required, Validators.min(1)]]
    });
  }

  async onFileSelect(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const files = Array.from(input.files);

      // Validate each file
      const validFiles: File[] = [];
      for (const file of files) {
        const validation = this.imageUtils.validateImageFile(file, 5);
        if (!validation.isValid) {
          alert(validation.error);
          return;
        }
        validFiles.push(file);
      }

      this.selectedFiles = [...this.selectedFiles, ...validFiles];

      // Generate previews
      for (const file of validFiles) {
        const { previewUrl } = await this.imageUtils.convertFileToBase64(file);
        this.imagePreviews.push(previewUrl);
      }
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



    // Check if user has seller role
    if (!this.authService.hasRole('seller')) {
      alert('Only sellers can add products');
      return;
    }

    this.isSubmitting = true;

    try {
      // Create product data
      const productData: ProductWithImagesDTO = {
        name: this.productForm.value.name,
        description: this.productForm.value.description,
        price: parseFloat(this.productForm.value.price),
        quantity: parseInt(this.productForm.value.quantity, 10),
        images: []
      };

      // Add images if any are selected
      if (this.selectedFiles.length > 0) {
        const imageBase64Array: string[] = [];
        for (const file of this.selectedFiles) {
          const { base64 } = await this.imageUtils.convertFileToBase64(file);
          imageBase64Array.push(base64);
        }
        productData.images = imageBase64Array;
      }

      // Create product
      this.productService.createProductWithImages(productData).subscribe({
        next: (createdProduct: CreateProductResponse) => {
          console.log('Product created successfully:', createdProduct);
          this.isSubmitting = false;
          // Navigate back to dashboard or products list
          this.router.navigate(['/']);
        },
        error: (error: any) => {
          if (error instanceof Error) {
            console.error('Error creating product:', error);
            const errorMessage = error.message || 'Failed to create product';
            alert(errorMessage);
          } else {
            console.error('Unexpected error:', error);
            alert('An unexpected error occurred. Please try again.');
          }
          this.isSubmitting = false;
        }
      });

    } catch (error) {
      console.error('Error processing images:', error);
      alert('Failed to process images. Please try again.');
      this.isSubmitting = false;
    }
  }



  cancel(): void {
    this.router.navigate(['/']);
  }
}