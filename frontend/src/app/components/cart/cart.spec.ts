import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { CartService } from '../../services/cart';
import { AuthService } from '../../services/auth';

import { CartComponent } from './cart';

describe('Cart', () => {
  let component: CartComponent;
  let fixture: ComponentFixture<CartComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CartComponent],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        {
          provide: Router,
          useValue: { navigate: jasmine.createSpy('navigate') }
        },
        { provide: CartService, useValue: { cart$: { subscribe: jasmine.createSpy('subscribe') } } },
        { provide: AuthService, useValue: jasmine.createSpyObj('AuthService', ['getToken', 'logout']) }
      ]
    })
    .compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(CartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
