import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

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
    private router: Router
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      price: ['', [Validators.required, Validators.min(0.01)]],
      stock: ['', [Validators.required, Validators.min(1)]]
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

  onSubmit(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    if (this.selectedFiles.length === 0) {
      alert('Please add at least one product image');
      return;
    }

    this.isSubmitting = true;
    
    // Create FormData for file upload
    const formData = new FormData();
    formData.append('name', this.productForm.value.name);
    formData.append('description', this.productForm.value.description);
    formData.append('price', this.productForm.value.price);
    formData.append('stock', this.productForm.value.stock);
    
    this.selectedFiles.forEach((file, index) => {
      formData.append(`images[${index}]`, file);
    });
    
    // TODO: Implement actual product creation logic with file upload
    console.log('Product data:', this.productForm.value);
    console.log('Files to upload:', this.selectedFiles);
    
    setTimeout(() => {
      this.isSubmitting = false;
      // Navigate back to dashboard or products list
      this.router.navigate(['/']);
    }, 2000);
  }

  cancel(): void {
    this.router.navigate(['/']);
  }
}