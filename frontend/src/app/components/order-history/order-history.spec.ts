import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { OrderHistory } from './order-history';
import { OrderService } from '../../services/order';
import { AuthService } from '../../services/auth';
import { of } from 'rxjs';

describe('OrderHistory', () => {
  let component: OrderHistory;
  let fixture: ComponentFixture<OrderHistory>;
  let mockOrderService: jasmine.SpyObj<OrderService>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    mockOrderService = jasmine.createSpyObj('OrderService', ['getUserOrders']);
    const mockAuthService = jasmine.createSpyObj('AuthService', ['getCurrentUserValue']);
    mockAuthService.getCurrentUserValue.and.returnValue({ role: 'client' });

    await TestBed.configureTestingModule({
      imports: [OrderHistory],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: OrderService, useValue: mockOrderService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(OrderHistory);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load orders on init', () => {
    const mockPage = {
      content: [],
      pageable: {
        pageNumber: 0,
        pageSize: 10,
        sort: { sorted: true, unsorted: false, empty: false }
      },
      totalPages: 0,
      totalElements: 0,
      last: true,
      first: true,
      numberOfElements: 0,
      size: 10,
      number: 0,
      empty: true
    };

    mockOrderService.getUserOrders.and.returnValue(of(mockPage));

    component.ngOnInit();

    expect(mockOrderService.getUserOrders).toHaveBeenCalledWith(0, 10, 'createdAt', 'desc', undefined, undefined, undefined);
  });

  it('should toggle order expansion', () => {
    const orderId = 'test-order-id';

    expect(component.isOrderExpanded(orderId)).toBe(false);

    component.toggleOrderExpand(orderId);
    expect(component.isOrderExpanded(orderId)).toBe(true);

    component.toggleOrderExpand(orderId);
    expect(component.isOrderExpanded(orderId)).toBe(false);
  });

  it('should return correct status class', () => {
    expect(component.getStatusClass('PENDING')).toBe('status-pending');
    expect(component.getStatusClass('PROCESSING')).toBe('status-processing');
    expect(component.getStatusClass('SHIPPED')).toBe('status-shipped');
    expect(component.getStatusClass('DELIVERED')).toBe('status-delivered');
    expect(component.getStatusClass('FAILED')).toBe('status-failed');
    expect(component.getStatusClass('UNKNOWN')).toBe('status-unknown');
  });
});