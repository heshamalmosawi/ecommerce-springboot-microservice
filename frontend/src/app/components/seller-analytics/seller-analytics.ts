import { Component, OnInit, AfterViewChecked, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService, SellerAnalyticsSummary, AnalyticsFilters, OrderStatus } from '../../services/order';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

// Register Chart.js components
Chart.register(...registerables);

@Component({
  selector: 'app-seller-analytics',
  imports: [CommonModule, FormsModule],
  templateUrl: './seller-analytics.html',
  styleUrl: './seller-analytics.scss'
})
export class SellerAnalytics implements OnInit, AfterViewChecked {
  @ViewChild('bestSellingChartCanvas', { static: false }) bestSellingChartCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('topRevenueChartCanvas', { static: false }) topRevenueChartCanvas?: ElementRef<HTMLCanvasElement>;

  analytics: SellerAnalyticsSummary | null = null;
  loading = false;
  error = '';

  // Filter properties
  filterStatus: string = '';
  filterStartDate: string = '';
  filterEndDate: string = '';
  showFilters = false;

  // Chart instances
  private bestSellingChart: Chart | null = null;
  private topRevenueChart: Chart | null = null;

  // Flag to track if charts need to be created
  private needsChartUpdate = false;
  private chartsCreated = false;

  // Order status options for dropdown
  orderStatusOptions = [
    { value: '', label: 'All Statuses' },
    { value: OrderStatus.PENDING, label: 'Pending' },
    { value: OrderStatus.PROCESSING, label: 'Processing' },
    { value: OrderStatus.SHIPPED, label: 'Shipped' },
    { value: OrderStatus.DELIVERED, label: 'Delivered' },
    { value: OrderStatus.FAILED, label: 'Failed' }
  ];

  constructor(
    private orderService: OrderService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.loadAnalytics();
  }

  ngAfterViewChecked(): void {
    // Create charts only once after data is loaded and DOM is ready
    if (this.needsChartUpdate && !this.chartsCreated && this.analytics) {
      const hasBestSellingCanvas = this.bestSellingChartCanvas?.nativeElement;
      const hasTopRevenueCanvas = this.topRevenueChartCanvas?.nativeElement;

      console.log('[ngAfterViewChecked] Checking for canvases', {
        needsUpdate: this.needsChartUpdate,
        chartsCreated: this.chartsCreated,
        hasAnalytics: !!this.analytics,
        hasBestSellingCanvas: !!hasBestSellingCanvas,
        hasTopRevenueCanvas: !!hasTopRevenueCanvas,
        bestSellingCount: this.analytics?.bestSellingProducts?.length,
        topRevenueCount: this.analytics?.topRevenueProducts?.length
      });

      if (hasBestSellingCanvas || hasTopRevenueCanvas) {
        this.createCharts();
        this.chartsCreated = true;
        this.needsChartUpdate = false;
      }
    }
  }

  loadAnalytics(): void {
    this.loading = true;
    this.error = '';

    const filters: AnalyticsFilters = {};

    if (this.filterStartDate) {
      filters.startDate = this.filterStartDate;
    }
    if (this.filterEndDate) {
      filters.endDate = this.filterEndDate;
    }
    if (this.filterStatus) {
      filters.status = this.filterStatus as OrderStatus;
    }

    this.orderService.getSellerAnalytics(filters).subscribe({
      next: (data) => {
        this.analytics = data;
        this.loading = false;
        this.chartsCreated = false;
        this.needsChartUpdate = true;
      },
      error: (err) => {
        console.error('Error loading seller analytics:', err);
        this.error = err.message || 'Failed to load analytics. Please try again.';
        this.loading = false;
      }
    });
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  applyFilters(): void {
    // Validate date range
    if (this.filterStartDate && this.filterEndDate) {
      const startDate = new Date(this.filterStartDate);
      const endDate = new Date(this.filterEndDate);

      if (startDate > endDate) {
        this.error = 'Start date must be before or equal to end date';
        return;
      }
    }

    this.loadAnalytics();
  }

  clearFilters(): void {
    this.filterStatus = '';
    this.filterStartDate = '';
    this.filterEndDate = '';
    this.error = '';
    this.loadAnalytics();
  }

  hasActiveFilters(): boolean {
    return !!(this.filterStatus || this.filterStartDate || this.filterEndDate);
  }

  private createCharts(): void {
    if (!this.analytics) {
      console.log('No analytics data available');
      return;
    }

    console.log('Creating charts with data:', this.analytics);

    // Destroy existing charts
    if (this.bestSellingChart) {
      this.bestSellingChart.destroy();
      this.bestSellingChart = null;
    }
    if (this.topRevenueChart) {
      this.topRevenueChart.destroy();
      this.topRevenueChart = null;
    }

    // Create best-selling products chart (bar chart - units sold)
    if (this.bestSellingChartCanvas && this.analytics.bestSellingProducts.length > 0) {
      console.log('Creating best-selling products chart');
      const ctx = this.bestSellingChartCanvas.nativeElement.getContext('2d');
      if (ctx) {
        const config: ChartConfiguration = {
          type: 'bar',
          data: {
            labels: this.analytics.bestSellingProducts.map(p => p.productName),
            datasets: [{
              label: 'Units Sold',
              data: this.analytics.bestSellingProducts.map(p => p.unitsSold),
              backgroundColor: 'rgba(54, 162, 235, 0.7)',
              borderColor: 'rgba(54, 162, 235, 1)',
              borderWidth: 1
            }]
          },
          options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                display: false
              },
              tooltip: {
                callbacks: {
                  label: (context) => {
                    const product = this.analytics!.bestSellingProducts[context.dataIndex];
                    return [
                      `Units Sold: ${product.unitsSold}`,
                      `Orders: ${product.orderCount}`,
                      `Revenue: $${product.totalRevenue.toFixed(2)}`
                    ];
                  }
                }
              }
            },
            scales: {
              x: {
                beginAtZero: true,
                ticks: {
                  precision: 0
                }
              }
            }
          }
        };
        this.bestSellingChart = new Chart(ctx, config);
        console.log('Best-selling products chart created successfully');
      }
    }

    // Create top revenue products chart (bar chart - revenue)
    if (this.topRevenueChartCanvas && this.analytics.topRevenueProducts.length > 0) {
      console.log('Creating top revenue products chart');
      const ctx = this.topRevenueChartCanvas.nativeElement.getContext('2d');
      if (ctx) {
        const config: ChartConfiguration = {
          type: 'bar',
          data: {
            labels: this.analytics.topRevenueProducts.map(p => p.productName),
            datasets: [{
              label: 'Total Revenue ($)',
              data: this.analytics.topRevenueProducts.map(p => p.totalRevenue),
              backgroundColor: 'rgba(75, 192, 192, 0.7)',
              borderColor: 'rgba(75, 192, 192, 1)',
              borderWidth: 1
            }]
          },
          options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                display: false
              },
              tooltip: {
                callbacks: {
                  label: (context) => {
                    const product = this.analytics!.topRevenueProducts[context.dataIndex];
                    return [
                      `Revenue: $${product.totalRevenue.toFixed(2)}`,
                      `Units Sold: ${product.unitsSold}`,
                      `Orders: ${product.orderCount}`
                    ];
                  }
                }
              }
            },
            scales: {
              x: {
                beginAtZero: true,
                ticks: {
                  callback: (value) => '$' + value
                }
              }
            }
          }
        };
        this.topRevenueChart = new Chart(ctx, config);
        console.log('Top revenue products chart created successfully');
      }
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount);
  }

  formatNumber(num: number): string {
    return new Intl.NumberFormat('en-US').format(num);
  }

  getDateRangeDisplay(): string {
    if (!this.analytics?.dateRange) {
      return 'All Time';
    }

    const start = this.analytics.dateRange.start;
    const end = this.analytics.dateRange.end;

    if (start && end) {
      return `${this.formatDate(start)} - ${this.formatDate(end)}`;
    } else if (start) {
      return `From ${this.formatDate(start)}`;
    } else if (end) {
      return `Until ${this.formatDate(end)}`;
    }

    return 'All Time';
  }

  private formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }
}
