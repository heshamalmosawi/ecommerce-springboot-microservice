import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImageUtilsService, ImageData } from '../../services/image-utils';

@Component({
  selector: 'app-image-upload',
  imports: [CommonModule],
  templateUrl: './image-upload.html',
  styleUrl: './image-upload.scss'
})
export class ImageUploadComponent {
  @Input() inputId: string = 'image-upload';
  @Input() multiple: boolean = false;
  @Input() maxFiles: number = 1;
  @Input() maxSizeMB: number = 5;
  @Input() acceptTypes: string = 'image/*';
  @Input() placeholderText: string = 'Click to upload image';
  @Input() hint: string = 'PNG, JPG up to 5MB';
  @Input() images: ImageData[] = [];

  @Output() imagesChange = new EventEmitter<ImageData[]>();
  @Output() imageSelected = new EventEmitter<ImageData>();
  @Output() imageRemoved = new EventEmitter<number>();

  constructor(private imageUtils: ImageUtilsService) { }

  async onFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const files = this.getFilesFromInput(input);
    if (!files) return;

    const validationResult = this.validateFileSelection(files);
    if (!validationResult.isValid) {
      alert(validationResult.error);
      return;
    }

    const newImages = await this.processFiles(files);
    if (newImages.length === 0) return;

    this.updateImages(newImages);
  }

  private getFilesFromInput(input: HTMLInputElement | null): File[] | null {
    if (!input?.files || input.files.length === 0) {
      return null;
    }
    return Array.from(input.files);
  }

  private validateFileSelection(files: File[]): { isValid: boolean; error?: string } {
    if (this.multiple && this.images.length + files.length > this.maxFiles) {
      return { isValid: false, error: `Maximum ${this.maxFiles} files allowed` };
    }

    if (!this.multiple && files.length > 1) {
      return { isValid: false, error: 'Only one file allowed' };
    }

    return { isValid: true };
  }

  private async processFiles(files: File[]): Promise<ImageData[]> {
    const newImages: ImageData[] = [];

    for (const file of files) {
      const validation = this.imageUtils.validateImageFile(file, this.maxSizeMB);
      if (!validation.isValid) {
        alert(validation.error);
        return [];
      }

      const imageData = await this.imageUtils.processImageFile(file);
      if (imageData.base64) {
        newImages.push(imageData);
      }
    }

    return newImages;
  }

  private updateImages(newImages: ImageData[]): void {
    if (this.multiple) {
      this.images = [...this.images, ...newImages];
    } else {
      this.images = newImages.length > 0 ? [newImages[0]] : [];
    }

    this.imagesChange.emit(this.images);

    newImages.forEach((image) => {
      this.imageSelected.emit(image);
    });
  }

  triggerFileInput(): void {
    this.imageUtils.triggerFileInput(this.inputId);
  }

  removeImage(index: number): void {
    this.images.splice(index, 1);
    this.imagesChange.emit(this.images);
    this.imageRemoved.emit(index);
  }

  clearAllImages(): void {
    this.images = [];
    this.imagesChange.emit(this.images);
  }

  // Helper method to get base64 array for API calls
  getBase64Array(): string[] {
    return this.images.map(img => img.base64 || '').filter(Boolean);
  }

  // Helper method to get first base64 (for single image scenarios)
  getFirstBase64(): string | null {
    return this.images.length > 0 ? this.images[0].base64 : null;
  }
}