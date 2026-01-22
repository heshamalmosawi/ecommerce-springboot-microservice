import { Component, OnInit, AfterViewChecked, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService, PurchaseSummary, AnalyticsFilters, OrderStatus } from '../../services/order';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

// Register Chart.js components
Chart.register(...registerables);

@Component({
  selector: 'app-purchase-analytics',
  imports: [CommonModule, FormsModule],
  templateUrl: './purchase-analytics.html',
  styleUrl: './purchase-analytics.scss'
})
export class PurchaseAnalytics implements OnInit, AfterViewChecked {
  @ViewChild('frequencyChartCanvas', { static: false }) frequencyChartCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('spendingChartCanvas', { static: false }) spendingChartCanvas?: ElementRef<HTMLCanvasElement>;

  analytics: PurchaseSummary | null = null;
  loading = false;
  error = '';
  
  // Filter properties
  filterStatus: string = '';
  filterStartDate: string = '';
  filterEndDate: string = '';
  showFilters = false;
  
  // Chart instances
  private frequencyChart: Chart | null = null;
  private spendingChart: Chart | null = null;
  
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
  ) {}

  ngOnInit(): void {
    this.loadAnalytics();
  }

  ngAfterViewChecked(): void {
    // Create charts only once after data is loaded and DOM is ready
    if (this.needsChartUpdate && !this.chartsCreated && this.analytics) {
      const hasFrequencyCanvas = this.frequencyChartCanvas?.nativeElement;
      const hasSpendingCanvas = this.spendingChartCanvas?.nativeElement;
      
      console.log('[ngAfterViewChecked] Checking for canvases', {
        needsUpdate: this.needsChartUpdate,
        chartsCreated: this.chartsCreated,
        hasAnalytics: !!this.analytics,
        hasFrequencyCanvas: !!hasFrequencyCanvas,
        hasSpendingCanvas: !!hasSpendingCanvas,
        mostPurchasedCount: this.analytics?.mostPurchasedProducts?.length,
        topSpendingCount: this.analytics?.topSpendingProducts?.length
      });
      
      if ((hasFrequencyCanvas || hasSpendingCanvas)) {
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
    
    this.orderService.getPurchaseAnalytics(filters).subscribe({
      next: (data) => {
        this.analytics = data;
        this.loading = false;
        this.chartsCreated = false;
        this.needsChartUpdate = true;
      },
      error: (err) => {
        console.error('Error loading analytics:', err);
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
    if (this.frequencyChart) {
      this.frequencyChart.destroy();
      this.frequencyChart = null;
    }
    if (this.spendingChart) {
      this.spendingChart.destroy();
      this.spendingChart = null;
    }
    
    // Create frequency chart (pie chart)
    if (this.frequencyChartCanvas && this.analytics.mostPurchasedProducts.length > 0) {
      console.log('Creating frequency pie chart');
      const ctx = this.frequencyChartCanvas.nativeElement.getContext('2d');
      if (ctx) {
        const config: ChartConfiguration = {
          type: 'pie',
          data: {
            labels: this.analytics.mostPurchasedProducts.map(p => p.productName),
            datasets: [{
              label: 'Number of Orders',
              data: this.analytics.mostPurchasedProducts.map(p => p.orderCount),
              backgroundColor: [
                'rgba(255, 99, 132, 0.8)',
                'rgba(54, 162, 235, 0.8)',
                'rgba(255, 206, 86, 0.8)',
                'rgba(75, 192, 192, 0.8)',
                'rgba(153, 102, 255, 0.8)'
              ],
              borderColor: [
                'rgba(255, 99, 132, 1)',
                'rgba(54, 162, 235, 1)',
                'rgba(255, 206, 86, 1)',
                'rgba(75, 192, 192, 1)',
                'rgba(153, 102, 255, 1)'
              ],
              borderWidth: 2
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                display: true,
                position: 'bottom',
                labels: {
                  padding: 15,
                  font: {
                    size: 12
                  }
                }
              },
              tooltip: {
                callbacks: {
                  label: (context) => {
                    const product = this.analytics!.mostPurchasedProducts[context.dataIndex];
                    return [
                      `${product.productName}`,
                      `Orders: ${product.orderCount}`,
                      `Total Quantity: ${product.totalQuantity}`,
                      `Total Spent: $${product.totalSpent.toFixed(2)}`
                    ];
                  }
                }
              }
            }
          }
        };
        this.frequencyChart = new Chart(ctx, config);
        console.log('Frequency pie chart created successfully');
      } else {
        console.error('Could not get 2d context for frequency chart');
      }
    } else {
      console.log('Frequency chart canvas not available or no products', {
        hasCanvas: !!this.frequencyChartCanvas,
        productCount: this.analytics.mostPurchasedProducts.length
      });
    }
    
    // Create spending chart
    if (this.spendingChartCanvas && this.analytics.topSpendingProducts.length > 0) {
      console.log('Creating spending chart');
      const ctx = this.spendingChartCanvas.nativeElement.getContext('2d');
      if (ctx) {
        const config: ChartConfiguration = {
          type: 'bar',
          data: {
            labels: this.analytics.topSpendingProducts.map(p => p.productName),
            datasets: [{
              label: 'Total Spent ($)',
              data: this.analytics.topSpendingProducts.map(p => p.totalSpent),
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
                    const product = this.analytics!.topSpendingProducts[context.dataIndex];
                    return [
                      `Total Spent: $${product.totalSpent.toFixed(2)}`,
                      `Orders: ${product.orderCount}`,
                      `Total Quantity: ${product.totalQuantity}`
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
        this.spendingChart = new Chart(ctx, config);
        console.log('Spending chart created successfully');
      } else {
        console.error('Could not get 2d context for spending chart');
      }
    } else {
      console.log('Spending chart canvas not available or no products', {
        hasCanvas: !!this.spendingChartCanvas,
        productCount: this.analytics.topSpendingProducts.length
      });
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
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
