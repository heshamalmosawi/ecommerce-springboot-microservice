import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HttpBackend } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { map, take } from 'rxjs/operators';
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
  name: string;
  token: string;
  expiresAt: number;
}

export interface DecodedToken {
  sub?: string;
  id?: string;
  email?: string;
  role?: 'client' | 'seller';
  name?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = environment.apiUrl;
  private readonly TOKEN_KEY = 'jwt_token';
  private readonly EXPIRES_AT_KEY = 'jwt_expires_at';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  private httpWithoutInterceptor: HttpClient;

  constructor(private http: HttpClient, private httpBackend: HttpBackend) {
    this.httpWithoutInterceptor = new HttpClient(httpBackend);
    this.initializeAuth();
  }

  private initializeAuth(): void {
    const token = this.getToken();
    const expiresAt = this.getExpiresAt();

    if (token && expiresAt && expiresAt > Date.now()) {
      // First set basic user info from token to avoid "TODO: ur name"
      const decodedToken = this.decodeToken(token);
      if (decodedToken) {
        console.log('Decoded token on init:', decodedToken);
        const tempUser: User = {
          id: decodedToken.sub || decodedToken.id || '',
          email: decodedToken.email || '',
          role: decodedToken.role || 'client',
          name: decodedToken.name || 'User name unavailable'
        };
        this.currentUserSubject.next(tempUser);
      }

      // Then validate with backend to get complete user data
      this.validateToken().pipe(
        // Automatically unsubscribe after the first emission
        // to prevent memory leaks
        take(1)
      ).subscribe({
        next: (backendUser: User) => {
          this.currentUserSubject.next(backendUser);
          console.log('Backend token validation successful, user set:', backendUser);
        },
        error: (error) => {
          console.warn('Backend token validation failed, keeping local token data:', error);
          // Keep user logged in with local token data
          // Only logout if we couldn't even decode the token locally
          if (!decodedToken) {
            this.logout();
          }
        }
      });
    } else if (token) {
      this.logout(); // Clear expired token
    }
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/users/auth/login`, credentials).pipe(
      map(response => {
        this.setToken(response.token, response.expiresAt);
        // Decode token to get user information
        const decodedToken = this.decodeToken(response.token);
        const user: User = {
          id: decodedToken?.sub || decodedToken?.id || '',
          email: decodedToken?.email || credentials.email,
          role: decodedToken?.role || 'client',
          name: response.name
        };
        this.currentUserSubject.next(user);
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

  private decodeToken(token: string): DecodedToken | null {
    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(payload));
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  validateToken(): Observable<User> {
    const token = this.getToken();
    if (!token) {
      return throwError(() => new Error('No token found'));
    }

    console.log('Validating token with backend:', `${this.API_URL}/users/authenticate`);
    
    return this.httpWithoutInterceptor.get<User>(`${this.API_URL}/users/authenticate`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    }).pipe(
      map(user => {
        console.log('Token validation successful, user:', user);
        // Update stored user data with fresh data from server
        return user;
      })
    );
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
