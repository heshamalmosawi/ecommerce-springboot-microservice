import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from '../login/login';
import { RegisterComponent } from '../register/register';

@Component({
  selector: 'app-auth',
  imports: [CommonModule, LoginComponent, RegisterComponent],
  templateUrl: './auth.html',
  styleUrl: './auth.scss'
})
export class AuthComponent {
  showLogin = true;

  switchToRegister(): void {
    this.showLogin = false;
  }

  switchToLogin(): void {
    this.showLogin = true;
  }
}
