import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserResponse } from './auth.service';

export interface MeetingRequest {
  title: string;
  agenda: string;
  dateTime: string; // ISO string format
  location: string;
  participantIds: number[];
}

export interface MeetingResponse {
  id: number;
  title: string;
  agenda: string;
  dateTime: string; // ISO string format
  location: string;
  host: UserResponse;
  participants: UserResponse[];
}

@Injectable({
  providedIn: 'root'
})
export class MeetingService {
  private apiUrl = 'http://localhost:8080/api/meetings';
  private usersUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) {}

  getMeetings(): Observable<MeetingResponse[]> {
    return this.http.get<MeetingResponse[]>(this.apiUrl);
  }

  getMeetingDetails(id: number): Observable<MeetingResponse> {
    return this.http.get<MeetingResponse>(`${this.apiUrl}/${id}`);
  }

  createMeeting(meeting: MeetingRequest): Observable<MeetingResponse> {
    return this.http.post<MeetingResponse>(this.apiUrl, meeting);
  }

  getUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(this.usersUrl);
  }

  getUserAvatarUrl(userId: number): string {
    return `${this.usersUrl}/${userId}/avatar`;
  }
}
