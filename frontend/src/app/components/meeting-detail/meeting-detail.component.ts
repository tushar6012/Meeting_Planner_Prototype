import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MeetingService, MeetingResponse } from '../../services/meeting.service';
import { AuthService, UserResponse } from '../../services/auth.service';

@Component({
  selector: 'app-meeting-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './meeting-detail.component.html',
  styleUrls: ['./meeting-detail.component.css']
})
export class MeetingDetailComponent implements OnInit {
  meetingId!: number;
  meeting: MeetingResponse | null = null;
  currentUser: UserResponse | null = null;
  isLoading = true;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private meetingService: MeetingService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    this.route.paramMap.subscribe(params => {
      const idStr = params.get('id');
      if (idStr) {
        this.meetingId = +idStr;
        this.loadMeetingDetails();
      } else {
        this.errorMessage = 'Invalid meeting URL';
        this.isLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  loadMeetingDetails(): void {
    this.isLoading = true;
    this.meetingService.getMeetingDetails(this.meetingId).subscribe({
      next: (data) => {
        this.meeting = data;
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 403) {
          this.errorMessage = 'Access Denied. You are not a participant or host of this meeting.';
        } else if (err.status === 404) {
          this.errorMessage = 'Meeting not found.';
        } else {
          this.errorMessage = 'Failed to load meeting details. Please try again.';
        }
        this.cdr.markForCheck();
        console.error(err);
      }
    });
  }

  getAvatarUrl(userId: number): string {
    return this.meetingService.getUserAvatarUrl(userId);
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      weekday: 'long',
      month: 'long',
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
