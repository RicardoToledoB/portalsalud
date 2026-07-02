import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTabsModule } from '@angular/material/tabs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { PortalImageRequest, PortalTopic, RequestStatus, SupportPortal } from '../../core/models';
import { RequestDetailDialogComponent } from './request-detail-dialog.component';

@Component({
  selector: 'app-requests',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatPaginatorModule, MatSelectModule, MatTableModule, MatDialogModule, MatTooltipModule, MatTabsModule],
  templateUrl: './requests.component.html',
  styleUrl: './requests.component.scss'
})
export class RequestsComponent implements OnInit {
  rows = signal<PortalImageRequest[]>([]);
  total = signal(0);
  page = 0;
  size = 20;
  displayedColumns = ['folio', 'portalName', 'createdAt', 'fullName', 'rut', 'difficultyType', 'attachments', 'status', 'actions'];

  filters = this.fb.nonNullable.group({
    portalId: [''],
    status: [''],
    topicId: [''],
    rut: [''],
    folio: ['']
  });

  portals = signal<SupportPortal[]>([]);
  topics = signal<PortalTopic[]>([]);
  activePortalId: number | null = null;
  selectedTabIndex = 0;
  statuses: RequestStatus[] = ['PENDIENTE', 'EN_REVISION', 'CONTACTADO', 'RESUELTO', 'NO_CORRESPONDE'];

  constructor(private fb: FormBuilder, private api: ApiService, private dialog: MatDialog, private auth: AuthService) {}

  ngOnInit(): void {
    this.loadPortals();
    this.filters.controls.portalId.valueChanges.subscribe(value => this.onPortalFilterChange(value));
  }

  loadPortals(): void {
    const user = this.auth.currentUser();
    this.api.getPublicPortals().subscribe(portals => {
      const assignedIds = this.assignedPortalIds();
      if (user?.role === 'REFERENTE_DSSM' && assignedIds.length) {
        const assigned = portals.filter(p => assignedIds.includes(p.id));
        this.portals.set(assigned);
        this.activePortalId = assigned.length === 1 ? assigned[0].id : null;
        this.selectedTabIndex = assigned.length === 1 ? 0 : 0;
        const topicId = assigned.length === 1 ? this.assignedTopicIdForPortal(assigned[0].id) : '';
        this.filters.patchValue({ portalId: this.activePortalId ? String(this.activePortalId) : '', topicId: topicId ? String(topicId) : '' });
        if (this.activePortalId) this.loadTopics(this.activePortalId);
        this.load();
        return;
      }
      this.portals.set(portals);
      this.load();
    });
  }

  onPortalTabChange(index: number): void {
    this.selectedTabIndex = index;
    const showAll = this.showAllPortalsTab();
    const portal = showAll ? (index === 0 ? null : this.portals()[index - 1]) : this.portals()[index];
    this.activePortalId = portal?.id ?? null;
    const topicId = this.activePortalId ? this.assignedTopicIdForPortal(this.activePortalId) : '';
    this.filters.patchValue({ portalId: this.activePortalId ? String(this.activePortalId) : '', topicId: topicId ? String(topicId) : '' });
    this.search();
  }

  onPortalFilterChange(value: string): void {
    const portalId = value ? Number(value) : null;
    this.activePortalId = portalId;
    const showAll = this.showAllPortalsTab();
    this.selectedTabIndex = portalId ? Math.max(0, this.portals().findIndex(p => p.id === portalId) + (showAll ? 1 : 0)) : 0;
    this.loadTopics(portalId);
  }

  loadTopics(portalId: number | null): void {
    if (!portalId) { this.topics.set([]); this.filters.patchValue({ topicId: '' }); return; }
    this.api.getPublicTopics(portalId).subscribe(topics => this.topics.set(topics));
  }

  load(): void {
    const raw = this.filters.getRawValue();
    this.api.getRequests({
      page: this.page,
      size: this.size,
      portalId: raw.portalId || undefined,
      status: raw.status || undefined,
      topicId: raw.topicId || undefined,
      rut: raw.rut || undefined,
      folio: raw.folio || undefined
    }).subscribe(page => {
      this.rows.set(page.content);
      this.total.set(page.totalElements);
    });
  }

  search(): void { this.page = 0; this.load(); }

  resetFilters(): void {
    const user = this.auth.currentUser();
    if (user?.role === 'REFERENTE_DSSM') {
      const assigned = this.auth.assignedPortals();
      if (assigned.length === 1) {
        this.filters.reset({ portalId: String(assigned[0].portalId), status: '', topicId: assigned[0].topicId ? String(assigned[0].topicId) : '', rut: '', folio: '' });
        this.activePortalId = assigned[0].portalId;
        this.selectedTabIndex = 0;
        this.loadTopics(assigned[0].portalId);
      } else {
        this.filters.reset({ portalId: '', status: '', topicId: '', rut: '', folio: '' });
        this.activePortalId = null;
        this.selectedTabIndex = 0;
        this.topics.set([]);
      }
    } else {
      this.filters.reset({ portalId: '', status: '', topicId: '', rut: '', folio: '' });
      this.activePortalId = null;
      this.selectedTabIndex = 0;
      this.topics.set([]);
    }
    this.page = 0;
    this.load();
  }

  pageChange(event: PageEvent): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.load();
  }

  open(row: PortalImageRequest): void {
    const ref = this.dialog.open(RequestDetailDialogComponent, { width: '860px', maxWidth: '96vw', data: row });
    ref.afterClosed().subscribe(changed => { if (changed) this.load(); });
  }

  export(): void {
    const raw = this.filters.getRawValue();
    this.api.exportRequests({ portalId: raw.portalId || undefined, status: raw.status || undefined, topicId: raw.topicId || undefined });
  }

  exportExcel(): void {
    const raw = this.filters.getRawValue();
    this.api.exportRequestsExcel({ portalId: raw.portalId || undefined, status: raw.status || undefined, topicId: raw.topicId || undefined });
  }

  showAllPortalsTab(): boolean {
    const user = this.auth.currentUser();
    return user?.role !== 'REFERENTE_DSSM' || this.auth.assignedPortals().length > 1;
  }

  allTabLabel(): string {
    return this.auth.currentUser()?.role === 'REFERENTE_DSSM' ? 'Todos mis portales' : 'Todos los portales';
  }

  lockPortalFilter(): boolean {
    return this.auth.currentUser()?.role === 'REFERENTE_DSSM' && this.auth.assignedPortals().length <= 1;
  }

  assignedPortalIds(): number[] {
    return this.auth.assignedPortals().map(a => a.portalId);
  }

  assignedTopicIdForPortal(portalId: number): number | string {
    const assignment = this.auth.assignedPortals().find(a => a.portalId === portalId);
    return assignment?.topicId ?? '';
  }

  currentPortalLabel(): string {
    if (!this.activePortalId) return this.allTabLabel();
    return this.portals().find(p => p.id === this.activePortalId)?.name ?? 'Portal seleccionado';
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
      CONTRASENA_NO_FUNCIONA: 'Portal no reconoce usuario/contraseña',
      DATOS_CONTACTO_NO_ACTUALIZADOS: 'Actualizar datos de contacto',
      SIN_CORREO_REGISTRADO: 'Sin correo asociado al Portal',
      NO_RECIBI_COMPARTIR_ESTUDIOS: 'No recibió información para compartir estudios',
      NO_RECIBI_RECUPERAR_CONTRASENA: 'No recibió tarea para recuperar contraseña',
      TUTOR_RESPONSABLE_SIN_ACCESO: 'Tutor/responsable sin acceso',
      OTRO: 'Otro'
    };
    return labels[value] ?? value;
  }

  topicLabel(row: PortalImageRequest): string {
    return row.topicName || this.labelDifficulty(row.difficultyType);
  }
}
