import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ImageUploadComponent } from './image-upload';
import { ImageUtilsService } from '../../services/image-utils';

describe('ImageUploadComponent', () => {
  let component: ImageUploadComponent;
  let fixture: ComponentFixture<ImageUploadComponent>;
  let mockImageUtils: jasmine.SpyObj<ImageUtilsService>;

  beforeEach(async () => {
    mockImageUtils = jasmine.createSpyObj('ImageUtilsService', [
      'triggerFileInput',
      'validateImageFile',
      'processImageFile',
      'convertFileToBase64'
    ]);

    await TestBed.configureTestingModule({
      imports: [ImageUploadComponent],
      providers: [
        { provide: ImageUtilsService, useValue: mockImageUtils }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ImageUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should trigger file input when triggerFileInput is called', () => {
    component.triggerFileInput();
    expect(mockImageUtils.triggerFileInput).toHaveBeenCalledWith('image-upload');
  });

  it('should remove image at specific index', () => {
    component.images = [
      { file: null, previewUrl: 'url1', base64: 'base64_1' },
      { file: null, previewUrl: 'url2', base64: 'base64_2' }
    ];
    
    component.removeImage(0);
    
    expect(component.images.length).toBe(1);
    expect(component.images[0].previewUrl).toBe('url2');
  });

  it('should clear all images', () => {
    component.images = [
      { file: null, previewUrl: 'url1', base64: 'base64_1' },
      { file: null, previewUrl: 'url2', base64: 'base64_2' }
    ];
    
    component.clearAllImages();
    
    expect(component.images.length).toBe(0);
  });

  it('should return base64 array', () => {
    component.images = [
      { file: null, previewUrl: 'url1', base64: 'base64_1' },
      { file: null, previewUrl: 'url2', base64: 'base64_2' },
      { file: null, previewUrl: 'url3', base64: null }
    ];
    
    const base64Array = component.getBase64Array();
    
    expect(base64Array).toEqual(['base64_1', 'base64_2']);
  });

  it('should return first base64', () => {
    component.images = [
      { file: null, previewUrl: 'url1', base64: 'base64_1' },
      { file: null, previewUrl: 'url2', base64: 'base64_2' }
    ];
    
    const firstBase64 = component.getFirstBase64();
    
    expect(firstBase64).toBe('base64_1');
  });

  it('should return null for first base64 when no images', () => {
    component.images = [];
    
    const firstBase64 = component.getFirstBase64();
    
    expect(firstBase64).toBeNull();
  });
});