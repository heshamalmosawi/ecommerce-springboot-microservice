import { Component, OnInit, HostListener } from '@angular/core';
import { RouterLink, RouterOutlet, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, User } from '../../services/auth';

@Component({
  selector: 'app-layout',
  imports: [RouterLink, RouterOutlet, CommonModule],
  templateUrl: './layout.html',
  styleUrl: './layout.scss'
})
export class LayoutComponent implements OnInit {
  currentUser: User | null = null;
  isDropdownOpen = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.authService.getCurrentUser().subscribe(user => {
      this.currentUser = user;
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth']);
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  isSeller(): boolean {
    return this.authService.hasRole('seller');
  }

  navigateToAddProduct(): void {
    this.router.navigate(['/products/add']);
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  closeDropdown(): void {
    this.isDropdownOpen = false;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu')) {
      this.closeDropdown();
    }
  }
}
