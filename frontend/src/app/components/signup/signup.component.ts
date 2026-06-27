import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  fullName = '';
  email = '';
  password = '';
  avatarFile: File | null = null;
  avatarPreview: string | null = null;
  errorMessage = '';
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 1024 * 1024) {
        this.errorMessage = 'Avatar image size must not exceed 1 MB.';
        this.avatarFile = null;
        this.avatarPreview = null;
        event.target.value = '';
        this.cdr.markForCheck();
        return;
      }
      
      this.errorMessage = '';
      this.avatarFile = file;
      
      // Generate preview URL
      const reader = new FileReader();
      reader.onload = () => {
        this.avatarPreview = reader.result as string;
        this.cdr.markForCheck();
      };
      reader.readAsDataURL(file);
    }
  }

  onSubmit(): void {
    if (!this.fullName || !this.email || !this.password) {
      this.errorMessage = 'Please fill out all required fields';
      return;
    }

    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(this.email)) {
      this.errorMessage = 'Please enter a valid email address.';
      return;
    }

    if (this.avatarFile && this.avatarFile.size > 1024 * 1024) {
      this.errorMessage = 'Avatar image size must not exceed 1 MB.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const formData = new FormData();
    formData.append('fullName', this.fullName);
    formData.append('email', this.email);
    formData.append('password', this.password);
    if (this.avatarFile) {
      formData.append('avatar', this.avatarFile, this.avatarFile.name);
    }

    this.authService.signup(formData).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error || 'Registration failed. Check if email is already registered.';
        this.cdr.markForCheck();
      }
    });
  }
}
