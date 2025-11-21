# Image Utilities

This directory contains reusable image handling utilities and components for the e-commerce application.

## ImageUtilsService

A service that provides common image handling functionality.

### Features:
- File validation (type and size)
- Base64 conversion
- Image preview generation
- Data URL manipulation

### Usage:

```typescript
import { ImageUtilsService } from '../../services/image-utils';

constructor(private imageUtils: ImageUtilsService) {}

// Validate file
const validation = this.imageUtils.validateImageFile(file, 5); // 5MB max
if (!validation.isValid) {
  console.error(validation.error);
  return;
}

// Process image file
const imageData = await this.imageUtils.processImageFile(file);
console.log(imageData.previewUrl); // For display
console.log(imageData.base64);     // For API calls

// Trigger file input
this.imageUtils.triggerFileInput('my-input-id');
```

## ImageUploadComponent

A reusable component for image uploads with preview functionality.

### Inputs:
- `inputId`: ID for the file input (default: 'image-upload')
- `multiple`: Allow multiple files (default: false)
- `maxFiles`: Maximum number of files (default: 1)
- `maxSizeMB`: Maximum file size in MB (default: 5)
- `acceptTypes`: Accepted file types (default: 'image/*')
- `placeholderText`: Text for upload placeholder (default: 'Click to upload image')
- `hint`: Hint text below placeholder (default: 'PNG, JPG up to 5MB')
- `images`: Array of ImageData objects

### Outputs:
- `imagesChange`: Emitted when images array changes
- `imageSelected`: Emitted when an image is selected
- `imageRemoved`: Emitted when an image is removed

### Usage Examples:

#### Single Image Upload (Profile Picture):
```html
<app-image-upload
  inputId="avatar-upload"
  [multiple]="false"
  [maxFiles]="1"
  placeholderText="Click to upload profile picture"
  hint="PNG, JPG up to 5MB"
  [(images)]="avatarImage"
></app-image-upload>
```

#### Multiple Images Upload (Product Gallery):
```html
<app-image-upload
  inputId="product-images"
  [multiple]="true"
  [maxFiles]="5"
  maxSizeMB="3"
  placeholderText="Click to upload product images"
  hint="PNG, JPG, WebP up to 3MB each"
  [(images)]="productImages"
></app-image-upload>
```

#### Component Usage:
```typescript
export class MyComponent {
  avatarImage: ImageData[] = [];
  productImages: ImageData[] = [];

  constructor(private imageUtils: ImageUtilsService) {}

  onSubmit() {
    // Get base64 for API calls
    const avatarBase64 = this.avatarImage.length > 0 ? this.avatarImage[0].base64 : null;
    const productBase64Array = this.productImages.map(img => img.base64).filter(Boolean);
    
    // Send to API...
  }
}
```

## ImageData Interface

```typescript
interface ImageData {
  file: File | null;        // Original file object
  previewUrl: string | null; // Data URL for preview display
  base64: string | null;     // Base64 string for API calls
}
```

## Integration with Existing Components

The image utilities have been integrated into:
- `RegisterComponent` - Profile picture upload
- `AddProductComponent` - Product images upload

## Best Practices

1. **Always validate files** before processing
2. **Use appropriate size limits** based on use case
3. **Provide clear feedback** to users about file requirements
4. **Handle errors gracefully** when image processing fails
5. **Use the reusable component** instead of implementing custom image handling

## File Structure

```
src/app/
├── services/
│   └── image-utils.ts          # Core image utility service
└── components/
    └── image-upload/
        ├── image-upload.ts      # Reusable image upload component
        ├── image-upload.html    # Component template
        ├── image-upload.scss    # Component styles
        └── image-upload.spec.ts # Component tests
```