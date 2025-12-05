import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { EditProductComponent } from './edit-product';
import { ProductService, Product } from '../../services/product';
import { AuthService } from '../../services/auth';
import { ImageUtilsService } from '../../services/image-utils';
import { FormBuilder } from '@angular/forms';

describe('EditProductComponent', () => {
  let component: EditProductComponent;
  let fixture: ComponentFixture<EditProductComponent>;
  let mockProductService: jasmine.SpyObj<ProductService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockImageUtils: jasmine.SpyObj<ImageUtilsService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockActivatedRoute: any;

  const mockProduct: Product = {
    id: '1',
    name: 'Test Product',
    description: 'Test Description',
    price: 10.99,
    quantity: 5,
    sellerName: 'Test Seller',
    imageMediaIds: ['media1', 'media2']
  };

  const mockUser = {
    id: 'user1',
    email: 'test@example.com',
    role: 'seller' as const,
    name: 'Test Seller'
  };

  beforeEach(async () => {
    mockProductService = jasmine.createSpyObj('ProductService', ['getProductById', 'updateProduct', 'updateProductWithImages']);
    mockAuthService = jasmine.createSpyObj('AuthService', ['getCurrentUserValue', 'hasRole']);
    mockImageUtils = jasmine.createSpyObj('ImageUtilsService', ['validateImageFile', 'convertFileToBase64']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    
    mockActivatedRoute = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy('get').and.returnValue('1')
        }
      }
    };

    // Configure default mock return values
    mockProductService.getProductById.and.returnValue(of(mockProduct));
    mockProductService.updateProduct.and.returnValue(of({ ...mockProduct }));
    mockProductService.updateProductWithImages.and.returnValue(of({ ...mockProduct }));
    mockAuthService.getCurrentUserValue.and.returnValue(mockUser);
    mockAuthService.hasRole.and.returnValue(true);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, EditProductComponent],
      providers: [
        FormBuilder,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ProductService, useValue: mockProductService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: ImageUtilsService, useValue: mockImageUtils },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditProductComponent);
    component = fixture.componentInstance;
    
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load product on init', () => {
    mockProductService.getProductById.and.returnValue(of(mockProduct));
    
    component.ngOnInit();
    
    expect(mockProductService.getProductById).toHaveBeenCalledWith('1');
    expect(component.product).toEqual(mockProduct);
  });

  it('should populate form with product data', () => {
    mockProductService.getProductById.and.returnValue(of(mockProduct));
    
    component.ngOnInit();
    fixture.detectChanges();
    
    expect(component.productForm.value.name).toBe(mockProduct.name);
    expect(component.productForm.value.description).toBe(mockProduct.description);
    expect(component.productForm.value.price).toBe(mockProduct.price.toString());
    expect(component.productForm.value.quantity).toBe(mockProduct.quantity.toString());
  });

  it('should check ownership', () => {
    mockProductService.getProductById.and.returnValue(of(mockProduct));
    
    component.ngOnInit();
    fixture.detectChanges();
    
    expect(mockAuthService.hasRole).toHaveBeenCalledWith('seller');
    expect(component.error).toBeNull();
  });

  it('should show error for non-owners', () => {
    const differentUser = { ...mockUser, name: 'Different Seller' };
    mockAuthService.getCurrentUserValue.and.returnValue(differentUser);
    mockProductService.getProductById.and.returnValue(of(mockProduct));
    
    component.ngOnInit();
    fixture.detectChanges();
    
    expect(component.error).toBe('You can only edit your own products');
  });

  it('should show error for non-sellers', () => {
    mockAuthService.hasRole.and.returnValue(false);
    mockProductService.getProductById.and.returnValue(of(mockProduct));
    
    component.ngOnInit();
    fixture.detectChanges();
    
    expect(component.error).toBe('Only sellers can edit products');
  });

  it('should handle file selection', async () => {
    const mockFile = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    const mockEvent = {
      target: {
        files: [mockFile]
      }
    } as any;

    mockImageUtils.validateImageFile.and.returnValue({ isValid: true });
    mockImageUtils.convertFileToBase64.and.returnValue(Promise.resolve({ 
      base64: 'data:image/jpeg;base64,test', 
      previewUrl: 'data:image/jpeg;base64,test' 
    }));

    await component.onFileSelect(mockEvent);

    expect(component.newFiles).toContain(mockFile);
    expect(component.newImagePreviews).toContain('data:image/jpeg;base64,test');
    expect(component.newImageBase64Array).toContain('data:image/jpeg;base64,test');
  });

  it('should remove new images', () => {
    component.newFiles = [new File(['test'], 'test.jpg')];
    component.newImagePreviews = ['preview1'];
    component.newImageBase64Array = ['base64data'];

    component.removeNewImage(0);

    expect(component.newFiles.length).toBe(0);
    expect(component.newImagePreviews.length).toBe(0);
    expect(component.newImageBase64Array.length).toBe(0);
  });

  it('should remove existing images', () => {
    component.existingImages = ['media1'];
    component.existingImagePreviews = ['preview1'];

    component.removeExistingImage(0);

    expect(component.existingImages.length).toBe(0);
    expect(component.existingImagePreviews.length).toBe(0);
  });

  it('should submit form with valid data', async () => {
    mockProductService.getProductById.and.returnValue(of(mockProduct));
    mockProductService.updateProductWithImages.and.returnValue(of(mockProduct));
    mockImageUtils.convertFileToBase64.and.returnValue(Promise.resolve({ 
      base64: 'data:image/jpeg;base64,test', 
      previewUrl: 'data:image/jpeg;base64,test' 
    }));

    component.ngOnInit();
    fixture.detectChanges();

    component.productForm.setValue({
      name: 'Updated Product',
      description: 'Updated Description',
      price: '15.99',
      quantity: '10'
    });

    await component.onSubmit();

    expect(mockProductService.updateProductWithImages).toHaveBeenCalledWith('1', jasmine.objectContaining({
      name: 'Updated Product',
      description: 'Updated Description',
      price: 15.99,
      quantity: 10,
      images: [],
      retainedImageIds: []
    }));
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/products', '1']);
  });

  it('should not submit invalid form', () => {
    component.productForm.setValue({
      name: 'ab', // too short
      description: 'short', // too short
      price: '0', // too low
      quantity: '0' // too low
    });

    component.onSubmit();

    expect(mockProductService.updateProductWithImages).not.toHaveBeenCalled();
  });

  it('should cancel navigation', () => {
    component.cancel();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/products', '1']);
  });
});