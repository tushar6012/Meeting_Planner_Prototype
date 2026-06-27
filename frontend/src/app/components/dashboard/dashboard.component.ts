import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService, UserResponse } from '../../services/auth.service';
import { MeetingService, MeetingResponse } from '../../services/meeting.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  currentUser: UserResponse | null = null;
  meetings: MeetingResponse[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private meetingService: MeetingService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    this.loadMeetings();
  }

  loadMeetings(): void {
    this.isLoading = true;
    this.meetingService.getMeetings().subscribe({
      next: (data) => {
        this.meetings = data;
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Failed to load meetings. Please try again.';
        this.cdr.markForCheck();
        console.error(err);
      }
    });
  }

  getAvatarUrl(userId: number): string {
    return this.meetingService.getUserAvatarUrl(userId);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  }

  formatTime(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true
    });
  }
}
