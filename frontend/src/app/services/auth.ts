import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

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
  expiresAt: number;
  user: User;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = environment.apiUrl;
  private readonly TOKEN_KEY = 'jwt_token';
  private readonly EXPIRES_AT_KEY = 'jwt_expires_at';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  
  constructor(private http: HttpClient) {
    this.initializeAuth();
  }

  private initializeAuth(): void {
    const token = this.getToken();
    const expiresAt = this.getExpiresAt();
    if (token && expiresAt && expiresAt > Date.now()) {
      this.currentUserSubject.next(null); // Will be set by server response
    } else if (token) {
      this.logout(); // Clear expired token
    }
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/users/auth/login`, credentials).pipe(
      map(response => {
        this.setToken(response.token, response.expiresAt);
        this.currentUserSubject.next(response.user);
        return response;
      })
    );
  }

  register(userData: RegisterRequest): Observable<string> {
    return this.http.post(`${this.API_URL}/users/auth/register`, userData, {
      responseType: 'text'
    });
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.EXPIRES_AT_KEY);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private getExpiresAt(): number | null {
    const expiresAt = localStorage.getItem(this.EXPIRES_AT_KEY);
    return expiresAt ? parseInt(expiresAt, 10) : null;
  }

  private setToken(token: string, expiresAt: number): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.EXPIRES_AT_KEY, expiresAt.toString());
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    const expiresAt = this.getExpiresAt();
    return token !== null && expiresAt !== null && expiresAt > Date.now();
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
