import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AddProductComponent } from './add-product';

describe('AddProductComponent', () => {
  let component: AddProductComponent;
  let fixture: ComponentFixture<AddProductComponent>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, AddProductComponent],
      providers: [
        { provide: Router, useValue: mockRouter }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddProductComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with required fields', () => {
    expect(component.productForm.contains('name')).toBeTruthy();
    expect(component.productForm.contains('description')).toBeTruthy();
    expect(component.productForm.contains('price')).toBeTruthy();
    expect(component.productForm.contains('category')).toBeTruthy();
    expect(component.productForm.contains('stock')).toBeTruthy();
  });

  it('should validate required fields', () => {
    const form = component.productForm;
    expect(form.valid).toBeFalsy();

    form.controls['name'].setValue('Test Product');
    form.controls['description'].setValue('Test Description');
    form.controls['price'].setValue('10.99');
    form.controls['category'].setValue('electronics');
    form.controls['stock'].setValue('5');

    expect(form.valid).toBeTruthy();
  });

  it('should navigate to home on cancel', () => {
    component.cancel();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });
});