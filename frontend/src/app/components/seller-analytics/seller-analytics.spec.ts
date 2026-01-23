import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SellerAnalytics } from './seller-analytics';
import { OrderService, SellerAnalyticsSummary, OrderStatus } from '../../services/order';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';

describe('SellerAnalytics', () => {
  let component: SellerAnalytics;
  let fixture: ComponentFixture<SellerAnalytics>;
  let orderService: jasmine.SpyObj<OrderService>;

  const mockAnalyticsData: SellerAnalyticsSummary = {
    totalRevenue: 15250.50,
    totalOrders: 42,
    totalUnitsSold: 156,
    productCount: 8,
    dateRange: null,
    bestSellingProducts: [
      {
        productId: '1',
        productName: 'Wireless Mouse',
        unitsSold: 45,
        orderCount: 38,
        totalRevenue: 2250.00
      }
    ],
    topRevenueProducts: [
      {
        productId: '2',
        productName: 'Mechanical Keyboard',
        unitsSold: 12,
        orderCount: 11,
        totalRevenue: 3600.00
      }
    ]
  };

  beforeEach(async () => {
    const orderServiceSpy = jasmine.createSpyObj('OrderService', ['getSellerAnalytics']);

    await TestBed.configureTestingModule({
      imports: [SellerAnalytics, HttpClientTestingModule, FormsModule],
      providers: [
        { provide: OrderService, useValue: orderServiceSpy }
      ]
    }).compileComponents();

    orderService = TestBed.inject(OrderService) as jasmine.SpyObj<OrderService>;
    fixture = TestBed.createComponent(SellerAnalytics);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load analytics on init', () => {
    orderService.getSellerAnalytics.and.returnValue(of(mockAnalyticsData));

    component.ngOnInit();

    expect(orderService.getSellerAnalytics).toHaveBeenCalled();
    expect(component.analytics).toEqual(mockAnalyticsData);
    expect(component.loading).toBeFalse();
  });

  it('should handle error when loading analytics fails', () => {
    const errorMessage = 'Failed to load seller analytics';
    orderService.getSellerAnalytics.and.returnValue(
      throwError(() => new Error(errorMessage))
    );

    component.ngOnInit();

    expect(component.error).toBe(errorMessage);
    expect(component.loading).toBeFalse();
  });

  it('should format currency correctly', () => {
    expect(component.formatCurrency(1234.56)).toBe('$1,234.56');
    expect(component.formatCurrency(0)).toBe('$0.00');
  });

  it('should format numbers correctly', () => {
    expect(component.formatNumber(1234)).toBe('1,234');
    expect(component.formatNumber(0)).toBe('0');
  });

  it('should display "All Time" when no date range is set', () => {
    component.analytics = { ...mockAnalyticsData, dateRange: null };
    expect(component.getDateRangeDisplay()).toBe('All Time');
  });

  it('should apply filters correctly', () => {
    orderService.getSellerAnalytics.and.returnValue(of(mockAnalyticsData));

    component.filterStartDate = '2024-01-01';
    component.filterEndDate = '2024-12-31';
    component.filterStatus = OrderStatus.DELIVERED;

    component.applyFilters();

    expect(orderService.getSellerAnalytics).toHaveBeenCalledWith(
      jasmine.objectContaining({
        startDate: '2024-01-01',
        endDate: '2024-12-31',
        status: OrderStatus.DELIVERED
      })
    );
  });

  it('should validate date range and show error if start date is after end date', () => {
    component.filterStartDate = '2024-12-31';
    component.filterEndDate = '2024-01-01';

    component.applyFilters();

    expect(component.error).toBe('Start date must be before or equal to end date');
  });

  it('should clear filters', () => {
    orderService.getSellerAnalytics.and.returnValue(of(mockAnalyticsData));

    component.filterStartDate = '2024-01-01';
    component.filterEndDate = '2024-12-31';
    component.filterStatus = OrderStatus.DELIVERED;

    component.clearFilters();

    expect(component.filterStartDate).toBe('');
    expect(component.filterEndDate).toBe('');
    expect(component.filterStatus).toBe('');
    expect(orderService.getSellerAnalytics).toHaveBeenCalledWith({});
  });

  it('should detect active filters', () => {
    expect(component.hasActiveFilters()).toBeFalse();

    component.filterStartDate = '2024-01-01';
    expect(component.hasActiveFilters()).toBeTrue();

    component.filterStartDate = '';
    component.filterStatus = OrderStatus.DELIVERED;
    expect(component.hasActiveFilters()).toBeTrue();
  });

  it('should toggle filters panel', () => {
    expect(component.showFilters).toBeFalse();

    component.toggleFilters();
    expect(component.showFilters).toBeTrue();

    component.toggleFilters();
    expect(component.showFilters).toBeFalse();
  });
});
