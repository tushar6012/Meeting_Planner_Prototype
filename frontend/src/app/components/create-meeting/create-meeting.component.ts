import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MeetingService, MeetingRequest } from '../../services/meeting.service';
import { AuthService, UserResponse } from '../../services/auth.service';

@Component({
  selector: 'app-create-meeting',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './create-meeting.component.html',
  styleUrls: ['./create-meeting.component.css']
})
export class CreateMeetingComponent implements OnInit {
  title = '';
  agenda = '';
  dateTime = '';
  location = '';
  minDateTime = '';
  
  participantsList: UserResponse[] = [];
  selectedParticipantIds = new Set<number>();
  
  isLoading = false;
  errorMessage = '';

  constructor(
    private meetingService: MeetingService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.minDateTime = this.getMinDateTime();
    this.loadUsers();
  }

  getMinDateTime(): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  loadUsers(): void {
    this.meetingService.getUsers().subscribe({
      next: (users) => {
        this.participantsList = users;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load participants', err);
        this.errorMessage = 'Could not load registered users list. Please try again.';
        this.cdr.markForCheck();
      }
    });
  }

  toggleParticipant(userId: number): void {
    if (this.selectedParticipantIds.has(userId)) {
      this.selectedParticipantIds.delete(userId);
    } else {
      this.selectedParticipantIds.add(userId);
    }
  }

  isParticipantSelected(userId: number): boolean {
    return this.selectedParticipantIds.has(userId);
  }

  getAvatarUrl(userId: number): string {
    return this.meetingService.getUserAvatarUrl(userId);
  }

  onSubmit(): void {
    if (!this.title || !this.dateTime || !this.location) {
      this.errorMessage = 'Please fill out all required fields (Title, Date & Time, Location)';
      return;
    }

    const selectedDate = new Date(this.dateTime);
    const now = new Date();
    if (selectedDate <= now) {
      this.errorMessage = 'Meeting date and time must be in the future.';
      return;
    }

    if (this.selectedParticipantIds.size === 0) {
      this.errorMessage = 'Please select at least one participant.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const request: MeetingRequest = {
      title: this.title,
      agenda: this.agenda,
      dateTime: this.dateTime, // String format 'YYYY-MM-DDTHH:mm' matches Spring's LocalDateTime
      location: this.location,
      participantIds: Array.from(this.selectedParticipantIds)
    };

    this.meetingService.createMeeting(request).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Failed to create meeting. Please check inputs.';
        this.cdr.markForCheck();
        console.error(err);
      }
    });
  }
}
