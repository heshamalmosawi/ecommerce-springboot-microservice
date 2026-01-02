import { Injectable } from "@angular/core";
import { Product } from "./product";
import { BehaviorSubject, map } from "rxjs";


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

    constructor(){
        this.cart$.subscribe(cart => localStorage.setItem('cart', JSON.stringify(cart)));
    }

    private loadCart(): Product[] {
        const data = localStorage.getItem('cart');
        return data ? JSON.parse(data) : [];
    }
    
    addOrUpdateItem(item: Product, increment?: boolean) {
        const cart = this.cart.value;
        const existing = cart.find(p => p.id == item.id);

        if (existing) {
            if (increment !== undefined) {
                existing.quantity += increment ? 1 : -1;
            } else {
                existing.quantity += item.quantity;
            }
            this.cart.next([...cart]);
        } else {
            this.cart.next([...cart, item]);
        }

        const totalQuantity = this.cart.value.reduce((sum, item) => sum + item.quantity, 0);
        console.log(`Total quantity in cart:`, totalQuantity);
    }
    
    removeItem(id: string) {
        this.cart.next(
            this.cart.value.filter(p => p.id == id)
        );
    }
    clear(){
        this.cart.next([]);
    }
}