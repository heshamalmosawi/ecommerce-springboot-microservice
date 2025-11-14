import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
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

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.getCurrentUser().subscribe(user => {
      this.currentUser = user;
    });
  }

  logout(): void {
    this.authService.logout();
    window.location.reload(); // Force reload to update UI
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }
}
