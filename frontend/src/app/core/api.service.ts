import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CaptchaChallenge, DashboardSummary, DifficultyType, Page, PortalImageRequest, PublicRequestStatus, RequestLog, RequestStatus, UserDto, UserRole, PortalType, SupportPortal, PortalTopic, UserPortalAssignment } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  getCaptcha(): Observable<CaptchaChallenge> {
    return this.http.get<CaptchaChallenge>(`${environment.apiBaseUrl}/public/solicitudes/captcha`);
  }

  getPublicPortals(): Observable<SupportPortal[]> {
    return this.http.get<SupportPortal[]>(`${environment.apiBaseUrl}/public/portales`);
  }

  getPublicTopics(portalId: number): Observable<PortalTopic[]> {
    return this.http.get<PortalTopic[]>(`${environment.apiBaseUrl}/public/portales/${portalId}/tematicas`);
  }

  createPublicRequest(payload: unknown, files: File[] = []): Observable<PortalImageRequest> {
    if (!files.length) {
      return this.http.post<PortalImageRequest>(`${environment.apiBaseUrl}/public/solicitudes`, payload);
    }
    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
    files.forEach(file => formData.append('files', file, file.name));
    return this.http.post<PortalImageRequest>(`${environment.apiBaseUrl}/public/solicitudes`, formData);
  }

  getPublicRequestStatus(folio: string, rut: string): Observable<PublicRequestStatus> {
    const params = new HttpParams().set('folio', folio).set('rut', rut);
    return this.http.get<PublicRequestStatus>(`${environment.apiBaseUrl}/public/solicitudes/seguimiento`, { params });
  }

  getDashboard(portalId?: number | string | null): Observable<DashboardSummary> {
    let params = new HttpParams();
    if (portalId !== undefined && portalId !== null && portalId !== '') {
      params = params.set('portalId', String(portalId));
    }
    return this.http.get<DashboardSummary>(`${environment.apiBaseUrl}/dashboard/summary`, { params });
  }

  getRequests(filters: { portalType?: string; portalId?: number | string; status?: string; difficultyType?: string; topicId?: number | string; rut?: string; folio?: string; page?: number; size?: number }): Observable<Page<PortalImageRequest>> {
    let params = new HttpParams()
      .set('page', filters.page ?? 0)
      .set('size', filters.size ?? 20)
      .set('sort', 'createdAt,desc');
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '' && !['page', 'size'].includes(key)) {
        params = params.set(key, String(value));
      }
    });
    return this.http.get<Page<PortalImageRequest>>(`${environment.apiBaseUrl}/solicitudes`, { params });
  }

  getRequestLogs(id: number): Observable<RequestLog[]> {
    return this.http.get<RequestLog[]>(`${environment.apiBaseUrl}/solicitudes/${id}/logs`);
  }

  downloadAttachment(requestId: number, attachmentId: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${environment.apiBaseUrl}/solicitudes/${requestId}/adjuntos/${attachmentId}/download`, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  updateStatus(
    id: number,
    status: RequestStatus,
    observation?: string,
    publicResponse?: string,
    notifyRequester?: boolean,
    assignedUserId?: number | null
  ): Observable<PortalImageRequest> {
    return this.http.patch<PortalImageRequest>(`${environment.apiBaseUrl}/solicitudes/${id}/estado`, {
      status,
      observation,
      publicResponse,
      notifyRequester,
      assignedUserId
    });
  }

  updateObservation(id: number, internalObservation: string): Observable<PortalImageRequest> {
    return this.http.patch<PortalImageRequest>(`${environment.apiBaseUrl}/solicitudes/${id}/observacion`, { internalObservation });
  }

  exportRequests(filters: { portalId?: number | string; status?: string; difficultyType?: string; topicId?: number | string } = {}): void {
    window.open(`${environment.apiBaseUrl}/solicitudes/export${this.toQueryString(filters)}`, '_blank');
  }

  exportRequestsExcel(filters: { portalId?: number | string; status?: string; difficultyType?: string; topicId?: number | string } = {}): void {
    window.open(`${environment.apiBaseUrl}/solicitudes/export/excel${this.toQueryString(filters)}`, '_blank');
  }

  private toQueryString(filters: Record<string, unknown>): string {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.set(key, String(value));
      }
    });
    const query = params.toString();
    return query ? `?${query}` : '';
  }

  getUsers(): Observable<UserDto[]> {
    return this.http.get<UserDto[]>(`${environment.apiBaseUrl}/users`);
  }

  createUser(payload: { fullName: string; email: string; password: string; role: UserRole; portalType?: PortalType | null; portalId?: number | null; topicId?: number | null; portalAssignments?: Partial<UserPortalAssignment>[]; difficultyType?: DifficultyType | null }): Observable<UserDto> {
    return this.http.post<UserDto>(`${environment.apiBaseUrl}/users`, payload);
  }

  updateUser(id: number, payload: Partial<{ fullName: string; email: string; password: string; role: UserRole; active: boolean; portalType: PortalType | null; portalId: number | null; topicId: number | null; portalAssignments: Partial<UserPortalAssignment>[]; difficultyType: DifficultyType | null }>): Observable<UserDto> {
    return this.http.put<UserDto>(`${environment.apiBaseUrl}/users/${id}`, payload);
  }

  resetUserPassword(id: number, password: string): Observable<UserDto> {
    return this.http.patch<UserDto>(`${environment.apiBaseUrl}/users/${id}/reset-password`, { password });
  }

  enableUser(id: number): Observable<UserDto> {
    return this.http.patch<UserDto>(`${environment.apiBaseUrl}/users/${id}/enable`, {});
  }

  disableUser(id: number): Observable<UserDto> {
    return this.http.patch<UserDto>(`${environment.apiBaseUrl}/users/${id}/disable`, {});
  }

  getAdminPortals(): Observable<SupportPortal[]> {
    return this.http.get<SupportPortal[]>(`${environment.apiBaseUrl}/admin/portales`);
  }

  createPortal(payload: { code: string; name: string; description?: string; active: boolean; allowUserObservation?: boolean; displayOrder?: number }): Observable<SupportPortal> {
    return this.http.post<SupportPortal>(`${environment.apiBaseUrl}/admin/portales`, payload);
  }

  updatePortal(id: number, payload: { code: string; name: string; description?: string; active: boolean; allowUserObservation?: boolean; displayOrder?: number }): Observable<SupportPortal> {
    return this.http.put<SupportPortal>(`${environment.apiBaseUrl}/admin/portales/${id}`, payload);
  }

  getAdminTopics(portalId: number): Observable<PortalTopic[]> {
    return this.http.get<PortalTopic[]>(`${environment.apiBaseUrl}/admin/portales/${portalId}/tematicas`);
  }

  createTopic(portalId: number, payload: { code: string; name: string; description?: string; active: boolean; requiresDetail?: boolean; requiresTutorContact?: boolean; displayOrder?: number }): Observable<PortalTopic> {
    return this.http.post<PortalTopic>(`${environment.apiBaseUrl}/admin/portales/${portalId}/tematicas`, payload);
  }

  updateTopic(portalId: number, topicId: number, payload: { code: string; name: string; description?: string; active: boolean; requiresDetail?: boolean; requiresTutorContact?: boolean; displayOrder?: number }): Observable<PortalTopic> {
    return this.http.put<PortalTopic>(`${environment.apiBaseUrl}/admin/portales/${portalId}/tematicas/${topicId}`, payload);
  }
}
