import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';

export interface User {
  id: string;
  email: string;
  role: 'client' | 'seller';
  name?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: 'client' | 'seller';
  avatar_b64?: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080'; // API Gateway URL
  private readonly TOKEN_KEY = 'jwt_token';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  
  constructor(private http: HttpClient) {
    this.initializeAuth();
  }

  private initializeAuth(): void {
    const token = this.getToken();
    if (token) {
      // Server will handle token expiration validation
      this.currentUserSubject.next(null); // Will be set by server response
    }
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/auth/login`, credentials).pipe(
      map(response => {
        this.setToken(response.token);
        this.currentUserSubject.next(response.user);
        return response;
      })
    );
  }

  register(userData: RegisterRequest): Observable<string> {
    return this.http.post(`${this.API_URL}/auth/register`, userData, {
      responseType: 'text'
    });
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  getCurrentUser(): Observable<User | null> {
    return this.currentUserSubject.asObservable();
  }

  getCurrentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  hasRole(role: 'client' | 'seller'): boolean {
    const user = this.getCurrentUserValue();
    return user ? user.role === role : false;
  }
}
