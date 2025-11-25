import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService, Product, ProductWithImagesDTO, ProductUpdateWithImagesDTO } from '../../services/product';
import { AuthService } from '../../services/auth';
import { ImageUtilsService } from '../../services/image-utils';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-edit-product',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-product.html',
  styleUrl: './edit-product.scss'
})
export class EditProductComponent implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  
  productId: string;
  product: Product | null = null;
  productForm: FormGroup;
  isSubmitting = false;
  loading = true;
  error: string | null = null;
  
  // Image management - simplified approach
  existingImages: string[] = [];           // Just track IDs to keep
  newImageBase64Array: string[] = [];     // Only new images
  existingImagePreviews: string[] = [];   // For UI display
  newImagePreviews: string[] = [];        // For UI display
  newFiles: File[] = [];                  // Keep files for conversion

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private authService: AuthService,
    private imageUtils: ImageUtilsService,
    private http: HttpClient
  ) {
    this.productId = this.route.snapshot.paramMap.get('id') || '';
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      price: ['', [Validators.required, Validators.min(0.01)]],
      quantity: ['', [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit() {
    if (!this.productId) {
      this.error = 'Product ID not found';
      this.loading = false;
      return;
    }

    this.loadProduct();
  }

  loadProduct() {
    this.loading = true;
    this.error = null;
    
    this.productService.getProductById(this.productId).subscribe({
      next: (product) => {
        this.product = product;
        this.checkOwnership();
        this.populateForm();
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

  checkOwnership() {
    if (!this.product) {
      return;
    }

    const currentUser = this.authService.getCurrentUserValue();
    if (!currentUser) {
      this.error = 'You must be logged in to edit products';
      return;
    }

    if (!this.authService.hasRole('seller')) {
      this.error = 'Only sellers can edit products';
      return;
    }

    // Check if current user is the owner of the product
    if (this.product.sellerName !== currentUser.name) {
      this.error = 'You can only edit your own products';
      return;
    }
  }

  populateForm() {
    if (!this.product) {
      return;
    }

    this.productForm.patchValue({
      name: this.product.name,
      description: this.product.description,
      price: this.product.price.toString(),
      quantity: this.product.quantity.toString()
    });
  }

  loadProductImages() {
    if (!this.product || !this.product.imageMediaIds || this.product.imageMediaIds.length === 0) {
      return;
    }

    this.product.imageMediaIds.forEach((mediaId, index) => {
      this.http.get(`${environment.apiUrl}/media/${mediaId}`).subscribe({
        next: (response: any) => {
          const imageUrl = response.base64Data.startsWith('data:') 
            ? response.base64Data 
            : 'data:' + response.contentType + ';base64,' + response.base64Data;
          
          this.existingImages[index] = mediaId;
          this.existingImagePreviews[index] = imageUrl;
        },
        error: () => {
          // If image fails to load, skip it
          console.warn(`Failed to load image ${mediaId}`);
        }
      });
    });
  }

  private async loadImageAsBase64(mediaId: string): Promise<string> {
    return new Promise((resolve, reject) => {
      this.http.get(`${environment.apiUrl}/media/${mediaId}`).subscribe({
        next: (response: any) => {
          const base64Data = response.base64Data.startsWith('data:') 
            ? response.base64Data 
            : 'data:' + response.contentType + ';base64,' + response.base64Data;
          resolve(base64Data);
        },
        error: (error) => {
          reject(error);
        }
      });
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
          // Clear the input to allow re-selection
          input.value = '';
          return;
        }
        validFiles.push(file);
      }

      this.newFiles = [...this.newFiles, ...validFiles];

      // Generate previews and base64 data
      for (const file of validFiles) {
        const { previewUrl, base64 } = await this.imageUtils.convertFileToBase64(file);
        this.newImagePreviews.push(previewUrl);
        this.newImageBase64Array.push(base64);
      }

      // Clear the input to allow re-selection of the same files
      if (this.fileInput) {
        this.fileInput.nativeElement.value = '';
      }
    }
  }

  removeNewImage(index: number): void {
    this.newFiles.splice(index, 1);
    this.newImagePreviews.splice(index, 1);
    this.newImageBase64Array.splice(index, 1);
  }

  clearAllNewImages(): void {
    this.newFiles = [];
    this.newImagePreviews = [];
    this.newImageBase64Array = [];
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }

  removeExistingImage(index: number): void {
    if (this.existingImages[index]) {
      this.existingImages.splice(index, 1);
      this.existingImagePreviews.splice(index, 1);
    }
  }



  async onSubmit(): Promise<void> {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    // Create simplified request data
    const productData: ProductUpdateWithImagesDTO = {
      name: this.productForm.value.name,
      description: this.productForm.value.description,
      price: parseFloat(this.productForm.value.price),
      quantity: parseInt(this.productForm.value.quantity, 10),
      images: this.newImageBase64Array,        // Only NEW images
      retainedImageIds: this.existingImages       // Images to KEEP
    };

    this.isSubmitting = true;

    // Use NEW endpoint
    this.productService.updateProductWithImages(this.productId, productData).subscribe({
      next: (updatedProduct) => {
        console.log('Product updated successfully:', updatedProduct);
        this.isSubmitting = false;
        this.router.navigate(['/products', this.productId]);
      },
      error: (error) => {
        console.error('Error updating product:', error);
        this.error = error.message || 'Failed to update product';
        this.isSubmitting = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/products', this.productId]);
  }
}