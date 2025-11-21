import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, RegisterRequest } from '../../../services/auth';
import { ImageUtilsService, ImageData } from '../../../services/image-utils';

@Component({
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class RegisterComponent implements OnInit {
  @Output() switchToLogin = new EventEmitter<void>();
  registerForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  imageData: ImageData = {
    file: null,
    previewUrl: null,
    base64: null
  };

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private imageUtils: ImageUtilsService
  ) {
    this.registerForm = this.fb.group({
      name: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      role: ['client', [Validators.required]],
      avatar_b64: ['']
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void { }

  passwordMatchValidator(form: FormGroup): { [key: string]: boolean } | null {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');

    if (password && confirmPassword && password.value !== confirmPassword.value) {
      return { passwordMismatch: true };
    }
    return null;
  }

  async onFileSelected(event: Event): Promise<void> {
    const file = this.imageUtils.onFileSelected(event);
    if (file) {
      const validation = this.imageUtils.validateImageFile(file);
      if (!validation.isValid) {
        this.errorMessage = validation.error || 'Invalid image file';
        return;
      }

      this.imageData = await this.imageUtils.processImageFile(file);
      if (this.imageData.base64) {
        this.registerForm.patchValue({ avatar_b64: this.imageData.base64 });
      }
    }
  }

  triggerFileInput(): void {
    this.imageUtils.triggerFileInput('avatar');
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const formData = { ...this.registerForm.value };
    delete formData.confirmPassword;

    const registerData: RegisterRequest = formData;

    this.authService.register(registerData).subscribe({
      next: () => {
        this.isLoading = false;
        this.switchToLogin.emit();
      },
      error: (error) => {
        this.isLoading = false;
        if (error.status === 400) {
          this.errorMessage = error.error?.message || 'Invalid input. Please check all fields and try again.';
        } else if (error.status === 409) {
          this.errorMessage = 'Email already exists. Please use a different email or login.';
        } else {
          this.errorMessage = error.error?.message || 'Registration failed. Please try again.';
        }
      }
    });
  }

  get name() { return this.registerForm.get('name'); }
  get email() { return this.registerForm.get('email'); }
  get password() { return this.registerForm.get('password'); }
  get confirmPassword() { return this.registerForm.get('confirmPassword'); }
  get role() { return this.registerForm.get('role'); }
  get avatar_b64() { return this.registerForm.get('avatar_b64'); }
}
