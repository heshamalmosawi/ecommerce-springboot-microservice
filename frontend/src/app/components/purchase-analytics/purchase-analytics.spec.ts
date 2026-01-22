import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PurchaseAnalytics } from './purchase-analytics';

describe('PurchaseAnalytics', () => {
  let component: PurchaseAnalytics;
  let fixture: ComponentFixture<PurchaseAnalytics>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PurchaseAnalytics]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PurchaseAnalytics);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
