import { Injectable } from "@angular/core";
import { Product } from "./product";
import { BehaviorSubject, debounceTime, map } from "rxjs";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";


@Injectable({
    providedIn: 'root'
})
export class CartService {
    private cart = new BehaviorSubject<Product[]>(this.loadCart());

    // 
    cart$ = this.cart.asObservable();

    // derived streams
    totalItems$ = this.cart$.pipe(map(items => items.reduce((sum, item) => sum + item.quantity, 0)));
    totalPrice$ = this.cart$.pipe(map(items => items.reduce((sum, item) => sum + item.price * item.quantity, 0)));

    constructor() {
        this.cart$.pipe(
            takeUntilDestroyed(),
            debounceTime(300)
        ).subscribe(cart => localStorage.setItem('cart', JSON.stringify(cart)));
    }

    private loadCart(): Product[] {
        const data = localStorage.getItem('cart');
        if (!data) {
            return [];
        }
        
        try {
            return JSON.parse(data);
        } catch (error) {
            console.error('Failed to parse cart from localStorage:', error);
            return [];
        }
    }

    addOrUpdateItem(item: Product, increment?: boolean): void {
        const cart = this.cart.value;
        const existing = cart.find(p => p.id == item.id);

        if (existing) {
            if (increment !== undefined) {
                existing.quantity += increment ? 1 : -1;
            } else {
                existing.quantity += item.quantity;
            }
            this.cart.next([...cart]);
            !increment && existing.quantity <= 0 && this.removeItem(item.id);
        } else {
            this.cart.next([...cart, item]);
        }
    }

    removeItem(id: string): void {
        this.cart.next(
            this.cart.value.filter(p => p.id !== id)
        );
    }

    clear(): void {
        this.cart.next([]);
    }

    get cartValue(): Product[] {
        return this.cart.value;
    }
}