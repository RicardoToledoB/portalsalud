import { Component, Inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { PortalImageRequest, RequestAttachment, RequestLog, RequestStatus, UserDto } from '../../core/models';

@Component({
  selector: 'app-request-detail-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatCheckboxModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatIconModule, MatTooltipModule],
  template: `
    <div class="dialog-title" mat-dialog-title>
      <div>
        <span class="eyebrow">Gestión de requerimiento</span>
        <h2>{{ data.folio }}</h2>
        <p>{{ data.fullName }}</p>
      </div>
      <span class="status-chip" [ngClass]="chipClass(data.status)">{{ labelStatus(data.status) }}</span>
    </div>

    <mat-dialog-content>
      <div class="info-grid">
        <div><span>Portal</span><strong>{{ data.portalName }}</strong></div>
        <div><span>RUT</span><strong>{{ data.rut }}</strong></div>
        <div><span>Correo</span><strong>{{ data.email || 'No informado' }}</strong></div>
        <div><span>Celular</span><strong>{{ data.phone || 'No informado' }}</strong></div>
        <div><span>Teléfono fijo</span><strong>{{ data.fixedPhone || 'No informado' }}</strong></div>
        <div><span>Ingreso</span><strong>{{ data.createdAt | date:'dd-MM-yyyy HH:mm' }}</strong></div>
        <div><span>Correo recepción</span><strong>{{ data.acknowledgementSentAt ? (data.acknowledgementSentAt | date:'dd-MM-yyyy HH:mm') : 'No enviado' }}</strong></div>
        <div><span>Última respuesta correo</span><strong>{{ data.responseSentAt ? (data.responseSentAt | date:'dd-MM-yyyy HH:mm') : 'No enviada' }}</strong></div>
        <div><span>Responsable</span><strong>{{ data.assignedUserName || 'Sin asignación' }}</strong></div>
      </div>

      <div class="request-box">
        <div><strong>Dificultad:</strong> {{ data.topicName || labelDifficulty(data.difficultyType) }}</div>
        <div *ngIf="data.otherDetail"><strong>Detalle:</strong> {{ data.otherDetail }}</div>
        <div *ngIf="data.userObservation"><strong>Observación usuario:</strong> {{ data.userObservation }}</div>
      </div>

      <section class="attachments-box" *ngIf="data.attachments?.length">
        <div class="attachments-title">
          <mat-icon>attach_file</mat-icon>
          <strong>Imágenes adjuntas por el solicitante</strong>
        </div>
        <div class="attachment-admin-item" *ngFor="let attachment of (data.attachments ?? [])">
          <mat-icon>image</mat-icon>
          <div>
            <strong>{{ attachment.originalFilename }}</strong>
            <span>{{ fileSizeLabel(attachment.sizeBytes) }} · {{ attachment.createdAt | date:'dd-MM-yyyy HH:mm' }}</span>
          </div>
          <button mat-stroked-button color="primary" type="button" (click)="downloadAttachment(attachment)">
            <mat-icon>download</mat-icon>
            Descargar
          </button>
        </div>
      </section>
      <p class="warning" *ngIf="data.lastNotificationError"><strong>Última alerta de correo:</strong> {{ data.lastNotificationError }}</p>

      <form [formGroup]="form">
        <div class="form-grid">
          <mat-form-field appearance="outline">
            <mat-label>Estado</mat-label>
            <mat-select formControlName="status">
              <mat-option *ngFor="let s of statuses" [value]="s">{{ labelStatus(s) }}</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" *ngIf="isAdmin()">
            <mat-label>Responsable</mat-label>
            <mat-select formControlName="assignedUserId">
              <mat-option [value]="null">Sin asignación</mat-option>
              <mat-option *ngFor="let u of referents()" [value]="u.id">{{ u.fullName }} - {{ u.portalName || 'General' }}</mat-option>
            </mat-select>
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Observación interna / gestión realizada</mat-label>
          <textarea matInput rows="4" formControlName="observation" placeholder="Esta información solo será visible para funcionarios."></textarea>
        </mat-form-field>
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Respuesta visible para el solicitante</mat-label>
          <textarea matInput rows="4" formControlName="publicResponse" placeholder="Ej: Sus datos fueron actualizados. Intente recuperar su contraseña nuevamente desde el portal correspondiente."></textarea>
        </mat-form-field>
        <div class="security-note">
          <mat-icon>security</mat-icon>
          No escriba claves, contraseñas ni credenciales en la respuesta visible al solicitante.
        </div>
        <div class="mail-box">
          <mat-checkbox formControlName="notifyRequester" [disabled]="!data.email">
            Enviar respuesta por correo electrónico al solicitante
          </mat-checkbox>
          <p *ngIf="!data.email">No se puede enviar correo porque el solicitante no informó correo electrónico.</p>
        </div>
      </form>

      <section class="history-box">
        <div class="history-title"><mat-icon>history</mat-icon><strong>Historial de gestiones</strong></div>
        <div class="history-empty" *ngIf="!logs().length">Sin registros de historial.</div>
        <div class="history-item" *ngFor="let log of logs()">
          <div>
            <strong>{{ labelAction(log.action) }}</strong>
            <span>{{ log.createdAt | date:'dd-MM-yyyy HH:mm' }} · {{ log.userName }}</span>
          </div>
          <p *ngIf="log.observation">{{ log.observation }}</p>
        </div>
      </section>
      <div class="error" *ngIf="error()">{{ error() }}</div>
    </mat-dialog-content>
    <mat-dialog-actions align="end" class="dialog-icon-actions">
      <button mat-icon-button mat-dialog-close matTooltip="Cancelar" aria-label="Cancelar">
        <mat-icon>close</mat-icon>
      </button>
      <button mat-mini-fab color="primary" (click)="save()" matTooltip="Guardar gestión" aria-label="Guardar gestión">
        <mat-icon>save</mat-icon>
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-title { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; padding: 22px 24px 8px; }
    .dialog-title h2 { margin: 4px 0 2px; color: var(--ssm-text); font-size: 26px; font-weight: 900; }
    .dialog-title p { margin: 0; color: var(--ssm-muted); }
    .eyebrow { display: block; color: var(--ssm-blue-700); font-weight: 900; font-size: 12px; text-transform: uppercase; letter-spacing: .08em; }
    .info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; margin-bottom: 14px; }
    .info-grid div { background: #f5f8fc; border: 1px solid var(--ssm-border); border-radius: 14px; padding: 12px; }
    .info-grid span { display: block; color: var(--ssm-muted); font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: .04em; }
    .info-grid strong { color: var(--ssm-text); word-break: break-word; }
    .request-box { display: grid; gap: 8px; background: #fffaf0; border: 1px solid #ffe3af; border-radius: 16px; padding: 14px; margin-bottom: 14px; color: #554020; }
    .attachments-box { display: grid; gap: 10px; margin: 0 0 14px; padding: 14px; border: 1px solid var(--ssm-border); border-radius: 16px; background: #f8fbff; }
    .attachments-title { display: flex; align-items: center; gap: 8px; color: var(--ssm-text); }
    .attachment-admin-item { display: grid; grid-template-columns: 32px 1fr auto; gap: 10px; align-items: center; padding: 10px; border: 1px solid var(--ssm-border); border-radius: 14px; background: #fff; }
    .attachment-admin-item > mat-icon { color: var(--ssm-blue-700); }
    .attachment-admin-item strong { display: block; word-break: break-word; color: var(--ssm-text); }
    .attachment-admin-item span { display: block; color: var(--ssm-muted); font-size: 12px; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
    .full-width { width: 100%; }
    .error, .warning { color: #b00020; font-weight: 700; }
    .security-note { display: flex; align-items: center; gap: 8px; background: #eef5ff; border: 1px solid #cfe0f7; border-radius: 14px; padding: 10px 12px; margin: 0 0 10px; color: var(--ssm-text); font-weight: 700; }
    .security-note mat-icon { color: var(--ssm-blue-700); }
    .mail-box { background: #eef5ff; border: 1px solid #cfe0f7; border-radius: 16px; padding: 12px; margin-top: 2px; }
    .mail-box p { margin: 6px 0 0; color: var(--ssm-muted); }
    .history-box { margin-top: 14px; padding: 14px; border: 1px solid var(--ssm-border); border-radius: 16px; background: #fbfdff; }
    .history-title { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; color: var(--ssm-text); }
    .history-item { border-top: 1px solid var(--ssm-border); padding: 10px 0; }
    .history-item:first-of-type { border-top: 0; }
    .history-item div { display: flex; justify-content: space-between; gap: 12px; }
    .history-item span, .history-empty { color: var(--ssm-muted); font-size: 13px; }
    .history-item p { margin: 6px 0 0; color: #33415c; }
    mat-dialog-actions { padding: 14px 24px 22px; }
    .dialog-icon-actions { gap: 8px; }
    .dialog-icon-actions button { flex-shrink: 0; }
    @media (max-width: 640px) { .dialog-title, .info-grid, .form-grid { grid-template-columns: 1fr; } .dialog-title { flex-direction: column; } .history-item div { flex-direction: column; } .attachment-admin-item { grid-template-columns: 32px 1fr; } .attachment-admin-item button { grid-column: 1 / -1; } }
  `]
})
export class RequestDetailDialogComponent implements OnInit {
  statuses: RequestStatus[] = ['PENDIENTE', 'EN_REVISION', 'CONTACTADO', 'RESUELTO', 'NO_CORRESPONDE'];
  error = signal<string | null>(null);
  logs = signal<RequestLog[]>([]);
  users = signal<UserDto[]>([]);
  form = this.fb.group({
    status: [this.data.status],
    assignedUserId: [this.data.assignedUserId ?? null as number | null],
    observation: [this.data.internalObservation ?? ''],
    publicResponse: [this.data.publicResponse ?? ''],
    notifyRequester: [Boolean(this.data.email)]
  });

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: PortalImageRequest,
    private dialogRef: MatDialogRef<RequestDetailDialogComponent>,
    private fb: FormBuilder,
    private api: ApiService,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.api.getRequestLogs(this.data.id).subscribe(logs => this.logs.set(logs));
    if (this.isAdmin()) {
      this.api.getUsers().subscribe(users => this.users.set(users.filter(u => u.active && u.role === 'REFERENTE_DSSM')));
    }
  }

  isAdmin(): boolean {
    return this.auth.currentUser()?.role === 'ADMIN';
  }

  referents(): UserDto[] {
    return this.users().filter(u => {
      if (!this.data.portalId) return true;
      if (u.portalAssignments?.length) return u.portalAssignments.some(a => a.portalId === this.data.portalId);
      return !u.portalId || u.portalId === this.data.portalId;
    });
  }

  save(): void {
    const raw = this.form.getRawValue();
    this.api.updateStatus(
      this.data.id,
      raw.status ?? this.data.status,
      raw.observation ?? undefined,
      raw.publicResponse ?? undefined,
      Boolean(raw.notifyRequester),
      raw.assignedUserId ?? null
    ).subscribe({
      next: () => this.dialogRef.close(true),
      error: err => this.error.set(err?.error?.message ?? 'No fue posible guardar la gestión.')
    });
  }

  downloadAttachment(attachment: RequestAttachment): void {
    this.error.set(null);
    this.api.downloadAttachment(this.data.id, attachment.id).subscribe({
      next: response => {
        const blob = response.body;
        if (!blob) {
          this.error.set('No fue posible descargar el adjunto.');
          return;
        }
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = attachment.originalFilename;
        link.click();
        setTimeout(() => window.URL.revokeObjectURL(url), 1000);
      },
      error: () => this.error.set('No fue posible descargar el adjunto.')
    });
  }

  fileSizeLabel(bytes: number): string {
    if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`;
    return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  }

  chipClass(status: RequestStatus): string { return status.toLowerCase(); }

  labelStatus(status: string): string {
    const labels: Record<string, string> = {
      PENDIENTE: 'Pendiente',
      EN_REVISION: 'En revisión',
      CONTACTADO: 'Contactado',
      RESUELTO: 'Resuelto',
      NO_CORRESPONDE: 'No corresponde'
    };
    return labels[status] ?? status;
  }

  labelDifficulty(value: string): string {
    const labels: Record<string, string> = {
      CONTRASENA_NO_FUNCIONA: 'Contraseña no funciona',
      DATOS_CONTACTO_NO_ACTUALIZADOS: 'Datos no actualizados',
      SIN_CORREO_REGISTRADO: 'Sin correo registrado',
      OTRO: 'Otro'
    };
    return labels[value] ?? value;
  }

  labelAction(value: string): string {
    const labels: Record<string, string> = {
      CREACION_SOLICITUD: 'Creación de solicitud',
      CAMBIO_ESTADO: 'Cambio de estado',
      OBSERVACION_INTERNA: 'Observación interna',
      ASIGNACION: 'Asignación',
      ACTUALIZACION_SOLICITUD: 'Actualización',
      ENVIO_CORREO: 'Envío de correo',
      ERROR_ENVIO_CORREO: 'Error de correo',
      ADJUNTO_SOLICITUD: 'Adjunto de imágenes'
    };
    return labels[value] ?? value;
  }
}
