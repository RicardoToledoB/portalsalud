import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, UserPortalAssignment } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'soporte_imagenes_token';
  private readonly userKey = 'soporte_imagenes_user';
  currentUser = signal<AuthResponse | null>(this.loadUser());

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiBaseUrl}/auth/login`, { email, password })
      .pipe(tap(response => this.saveSession(response)));
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return this.currentUser()?.role === 'ADMIN';
  }

  isReferente(): boolean {
    return this.currentUser()?.role === 'REFERENTE_DSSM';
  }

  assignedPortals(): UserPortalAssignment[] {
    const user = this.currentUser();
    if (!user) return [];
    if (user.portalAssignments?.length) return user.portalAssignments;
    if (user.portalId) {
      return [{ portalId: user.portalId, portalName: user.portalName ?? 'Portal asignado', topicId: user.topicId, topicName: user.topicName }];
    }
    return [];
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  private saveSession(response: AuthResponse): void {
    localStorage.setItem(this.tokenKey, response.token);
    localStorage.setItem(this.userKey, JSON.stringify(response));
    this.currentUser.set(response);
  }

  private loadUser(): AuthResponse | null {
    const raw = localStorage.getItem(this.userKey);
    return raw ? JSON.parse(raw) as AuthResponse : null;
  }
}
