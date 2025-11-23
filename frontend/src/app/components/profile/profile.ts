import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { AuthService, User } from '../../services/auth';
import { ImageUtilsService, ImageData } from '../../services/image-utils';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class Profile implements OnInit {
  profileForm: FormGroup;
  user: User | null = null;
  isEditing = false;
  loading = false;
  saving = false;
  message = '';
  messageType: 'success' | 'error' = 'success';
  avatarUrl = '';
  avatarBase64 = '';
  originalUserData: User | null = null;
  avatarImageData: ImageData[] = [];
  private justUploadedAvatar = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private imageUtils: ImageUtilsService,
    private router: Router,
    private http: HttpClient
  ) {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: [{ value: '', disabled: true }],
      role: [{ value: '', disabled: true }]
    });
  }

  ngOnInit(): void {
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.loading = true;
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.originalUserData = user ? { ...user } : null;

        if (this.user) {
          this.profileForm.patchValue({
            name: this.user.name || '',
            email: this.user.email,
            role: this.user.role ? this.user.role.charAt(0).toUpperCase() + this.user.role.slice(1) : ''
          });
          // Set avatar using mediaId to fetch from backend (more reliable)
          // But only fetch if we haven't just uploaded a new avatar
          if (this.user.avatarMediaId && !this.justUploadedAvatar) {
            this.fetchAvatarByMediaId(this.user.avatarMediaId);
          } else if (!this.justUploadedAvatar) {
            this.avatarImageData = [];
            this.avatarUrl = '';
            this.avatarBase64 = '';
          }

          // Reset the flag after checking
          this.justUploadedAvatar = false;
        }
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Error loading profile:', error);
        this.loading = false;

        // Handle specific error cases
        if (error.status === 401) {
          this.showMessage('Session expired. Please login again.', 'error');
          // Redirect to login after a delay
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        } else if (error.status === 404) {
          this.showMessage('User profile not found', 'error');
        } else if (error.status === 0) {
          this.showMessage('Network error. Please check your connection.', 'error');
        } else {
          this.showMessage('Failed to load profile', 'error');
        }
      }
    });
  }

  toggleEditMode(): void {
    if (this.isEditing) {
      this.cancelEdit();
    } else {
      this.isEditing = true;
      this.message = '';
    }
  }

  cancelEdit(): void {
    this.isEditing = false;
    if (this.originalUserData) {
      this.profileForm.patchValue({
        name: this.originalUserData.name,
        email: this.originalUserData.email,
        role: this.originalUserData.role
      });
    }
    this.message = '';
  }

  async onSubmit(): Promise<void> {
    if (this.profileForm.invalid || !this.user) {
      return;
    }

    this.saving = true;
    try {
      const formData = this.profileForm.value;
      const updateData: any = {
        name: formData.name
      };

      // Include avatar if changed
      if (this.avatarBase64 && this.avatarBase64 !== this.user.avatarBase64) {
        updateData.avatarBase64 = this.avatarBase64;
      }

      // Call the profile update API
      const updatedUser = await firstValueFrom(this.authService.updateProfile(updateData));

      if (updatedUser) {
        this.user = updatedUser;
        this.originalUserData = { ...updatedUser };

        // If we uploaded an avatar, keep the flag set to prevent fetching from backend
        // The uploaded avatar will continue to display until next page refresh
        if (!updateData.avatarBase64) {
          // Only reset flag if we didn't upload a new avatar
          this.justUploadedAvatar = false;
        }

        this.isEditing = false;
        this.showMessage('Profile updated successfully', 'success');
      }
    } catch (error: any) {
      console.error('Error updating profile:', error);

      // Handle specific error cases
      if (error.status === 400) {
        this.showMessage('Invalid data provided', 'error');
      } else if (error.status === 401) {
        this.showMessage('Session expired. Please login again.', 'error');
        // Redirect to login after a delay
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      } else if (error.status === 404) {
        this.showMessage('User not found', 'error');
      } else {
        this.showMessage('Failed to update profile', 'error');
      }
    } finally {
      this.saving = false;
    }
  }

  async onAvatarChange(event: Event): Promise<void> {
    const file = this.imageUtils.onFileSelected(event);
    if (!file) return;

    // Validate file using ImageUtilsService
    const validation = this.imageUtils.validateImageFile(file);
    if (!validation.isValid) {
      this.showMessage(validation.error || 'Invalid image file', 'error');
      return;
    }

    // Process image using ImageUtilsService (same pattern as add-product)
    const imageData = await this.imageUtils.processImageFile(file);
    if (imageData.base64) {
      this.avatarImageData = [imageData];
      this.avatarUrl = imageData.previewUrl || '';
      this.avatarBase64 = this.imageUtils.extractBase64Data(imageData.base64);
      this.justUploadedAvatar = true; // Flag that we just uploaded an avatar
    }
  }

  private fetchAvatarByMediaId(mediaId: string): void {
    this.http.get(`${environment.apiUrl}/media/${mediaId}`).subscribe({
      next: (mediaResponse: any) => {
        if (mediaResponse && mediaResponse.base64Data && mediaResponse.contentType) {
          // Create data URL from base64Data and contentType
          const dataUrl = `data:${mediaResponse.contentType};base64,${mediaResponse.base64Data}`;
          this.avatarImageData = [{
            file: null,
            previewUrl: dataUrl,
            base64: mediaResponse.base64Data
          }];
          this.avatarUrl = dataUrl;
          this.avatarBase64 = mediaResponse.base64Data;
        } else {
          console.error('Invalid media response format:', mediaResponse);
          this.avatarImageData = [];
          this.avatarUrl = '';
          this.avatarBase64 = '';
        }
      },
      error: (error: any) => {
        console.error('Error fetching avatar:', error);
        this.avatarImageData = [];
        this.avatarUrl = '';
        this.avatarBase64 = '';
      }
    });
  }

  private showMessage(message: string, type: 'success' | 'error'): void {
    this.message = message;
    this.messageType = type;

    // Auto-hide message after 5 seconds
    setTimeout(() => {
      this.message = '';
    }, 5000);
  }
}
