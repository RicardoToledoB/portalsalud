export type UserRole = 'ADMIN' | 'REFERENTE_DSSM';
export type RequestStatus = 'PENDIENTE' | 'EN_REVISION' | 'CONTACTADO' | 'RESUELTO' | 'NO_CORRESPONDE';
export type DifficultyType = 'CONTRASENA_NO_FUNCIONA' | 'DATOS_CONTACTO_NO_ACTUALIZADOS' | 'SIN_CORREO_REGISTRADO' | 'OTRO';
export type PortalType = 'PORTAL_IMAGENES';

export interface UserPortalAssignment {
  portalId: number;
  portalName: string;
  topicId?: number;
  topicName?: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  fullName: string;
  email: string;
  role: UserRole;
  portalId?: number;
  portalName?: string;
  topicId?: number;
  topicName?: string;
  portalAssignments?: UserPortalAssignment[];
  difficultyType?: DifficultyType;
}

export interface SupportPortal {
  id: number;
  code: string;
  name: string;
  description?: string;
  active: boolean;
  displayOrder: number;
  topicCount: number;
  createdAt: string;
  updatedAt?: string;
}

export interface PortalTopic {
  id: number;
  portalId: number;
  portalName: string;
  code: string;
  name: string;
  description?: string;
  active: boolean;
  requiresDetail: boolean;
  displayOrder: number;
  createdAt: string;
  updatedAt?: string;
}

export interface RequestAttachment {
  id: number;
  originalFilename: string;
  contentType: string;
  sizeBytes: number;
  downloadUrl: string;
  createdAt: string;
}

export interface PortalImageRequest {
  id: number;
  folio: string;
  portalType: PortalType;
  portalId?: number;
  portalCode?: string;
  portalName: string;
  fullName: string;
  rut: string;
  email?: string;
  phone?: string;
  fixedPhone?: string;
  difficultyType: DifficultyType;
  topicId?: number;
  topicCode?: string;
  topicName?: string;
  otherDetail?: string;
  userObservation?: string;
  consentAccepted: boolean;
  source: string;
  status: RequestStatus;
  internalObservation?: string;
  publicResponse?: string;
  acknowledgementSentAt?: string;
  responseSentAt?: string;
  lastNotificationError?: string;
  assignedUserId?: number;
  assignedUserName?: string;
  resolvedAt?: string;
  createdAt: string;
  updatedAt?: string;
  attachmentCount?: number;
  attachments?: RequestAttachment[];
}

export interface PublicRequestStatus {
  folio: string;
  portalType: PortalType;
  portalId?: number;
  portalName: string;
  status: RequestStatus;
  publicResponse?: string;
  createdAt: string;
  updatedAt?: string;
  resolvedAt?: string;
}

export interface RequestLog {
  id: number;
  action: string;
  previousStatus?: RequestStatus;
  newStatus?: RequestStatus;
  observation?: string;
  userName: string;
  createdAt: string;
}

export interface CaptchaChallenge {
  captchaId: string;
  question: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface UserDto {
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
  active: boolean;
  portalType?: PortalType;
  portalId?: number;
  portalName?: string;
  topicId?: number;
  topicName?: string;
  portalAssignments?: UserPortalAssignment[];
  difficultyType?: DifficultyType;
  createdAt: string;
  updatedAt?: string;
}

export interface DashboardSummary {
  total: number;
  pendientes: number;
  enRevision: number;
  contactados: number;
  resueltos: number;
  noCorresponde: number;
  porTipoDificultad: Record<string, number>;
  porPortal: Record<string, number>;
  ultimos7Dias: number;
  ultimos30Dias: number;
}
