import { ComponentFixture, TestBed } from '@angular/core/testing';
import { OrderHistory } from './order-history';
import { OrderService } from '../../services/order';
import { of } from 'rxjs';

describe('OrderHistory', () => {
  let component: OrderHistory;
  let fixture: ComponentFixture<OrderHistory>;
  let mockOrderService: jasmine.SpyObj<OrderService>;

  beforeEach(async () => {
    mockOrderService = jasmine.createSpyObj('OrderService', ['getUserOrders']);

    await TestBed.configureTestingModule({
      imports: [OrderHistory],
      providers: [
        { provide: OrderService, useValue: mockOrderService }
      ]
    }).compileComponents();

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

    expect(mockOrderService.getUserOrders).toHaveBeenCalledWith(0, 10, 'createdAt', 'desc');
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