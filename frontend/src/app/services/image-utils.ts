import { Injectable } from '@angular/core';

export interface ImageData {
  file: File | null;
  previewUrl: string | null;
  base64: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class ImageUtilsService {
  
  /**
   * Handle file selection from input event
   */
  onFileSelected(event: Event): File | null {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      return input.files[0];
    }
    return null;
  }

  /**
   * Trigger file input click
   */
  triggerFileInput(inputId: string): void {
    const input = document.getElementById(inputId) as HTMLInputElement;
    if (input) {
      input.click();
    }
  }

  /**
   * Convert file to base64 and return preview URL
   */
  convertFileToBase64(file: File): Promise<{ base64: string; previewUrl: string }> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const base64 = reader.result as string;
        resolve({
          base64,
          previewUrl: base64
        });
      };
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  /**
   * Process image file selection and conversion
   */
  async processImageFile(file: File): Promise<ImageData> {
    if (!file) {
      return {
        file: null,
        previewUrl: null,
        base64: null
      };
    }

    try {
      const { base64, previewUrl } = await this.convertFileToBase64(file);
      return {
        file,
        previewUrl,
        base64
      };
    } catch (error) {
      console.error('Error processing image:', error);
      return {
        file: null,
        previewUrl: null,
        base64: null
      };
    }
  }

  /**
   * Validate image file type and size
   */
  validateImageFile(file: File, maxSizeMB: number = 5): { isValid: boolean; error?: string } {
    // Check file type
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      return {
        isValid: false,
        error: 'Invalid file type. Please select a valid image file (JPEG, PNG, GIF, or WebP).'
      };
    }

    // Check file size (convert MB to bytes)
    const maxSizeBytes = maxSizeMB * 1024 * 1024;
    if (file.size > maxSizeBytes) {
      return {
        isValid: false,
        error: `File size exceeds ${maxSizeMB}MB limit. Please select a smaller image.`
      };
    }

    return { isValid: true };
  }

  /**
   * Clear image data
   */
  clearImageData(): ImageData {
    return {
      file: null,
      previewUrl: null,
      base64: null
    };
  }

  /**
   * Extract base64 data without the data URL prefix
   */
  extractBase64Data(base64String: string): string {
    if (!base64String) return '';
    
    const commaIndex = base64String.indexOf(',');
    if (commaIndex !== -1) {
      return base64String.substring(commaIndex + 1);
    }
    return base64String;
  }

  /**
   * Create a complete data URL from base64 data
   */
  createDataUrl(base64Data: string, mimeType: string = 'image/jpeg'): string {
    if (!base64Data) return '';
    
    // If it's already a complete data URL, return as is
    if (base64Data.startsWith('data:')) {
      return base64Data;
    }
    
    return `data:${mimeType};base64,${base64Data}`;
  }
}