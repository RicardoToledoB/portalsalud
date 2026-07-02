import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { DashboardSummary, SupportPortal } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatCardModule, MatIconModule, MatProgressBarModule, MatTabsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  portals = signal<SupportPortal[]>([]);
  summaries = signal<Record<string, DashboardSummary>>({});
  activePortalId: number | null = null;
  loading = signal(false);

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit(): void {
    this.loadPortals();
  }

  loadPortals(): void {
    const user = this.auth.currentUser();
    this.api.getPublicPortals().subscribe(portals => {
      const assignedIds = this.assignedPortalIds();
      if (user?.role === 'REFERENTE_DSSM' && assignedIds.length) {
        const assigned = portals.filter(p => assignedIds.includes(p.id));
        this.portals.set(assigned);
        this.activePortalId = assigned.length === 1 ? assigned[0].id : null;
        this.loadSummary(this.activePortalId);
        return;
      }
      this.portals.set(portals);
      this.loadSummary(null);
    });
  }

  onPortalTabChange(index: number): void {
    const showAll = this.showAllPortalsTab();
    const portal = showAll ? (index === 0 ? null : this.portals()[index - 1]) : this.portals()[index];
    this.activePortalId = portal?.id ?? null;
    this.loadSummary(this.activePortalId);
  }

  loadSummary(portalId: number | null): void {
    const key = this.summaryKey(portalId);
    if (this.summaries()[key]) return;

    this.loading.set(true);
    this.api.getDashboard(portalId).subscribe({
      next: summary => {
        this.summaries.update(current => ({ ...current, [key]: summary }));
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  currentSummary(): DashboardSummary | null {
    return this.summaries()[this.summaryKey(this.activePortalId)] ?? null;
  }

  showAllPortalsTab(): boolean {
    const user = this.auth.currentUser();
    return user?.role !== 'REFERENTE_DSSM' || this.assignedPortalIds().length > 1;
  }

  allTabLabel(): string {
    return this.auth.currentUser()?.role === 'REFERENTE_DSSM' ? 'Todos mis portales' : 'Todos los portales';
  }

  assignedPortalIds(): number[] {
    return this.auth.assignedPortals().map(a => a.portalId);
  }

  currentPortalLabel(): string {
    if (!this.activePortalId) return this.allTabLabel();
    return this.portals().find(p => p.id === this.activePortalId)?.name ?? 'Portal seleccionado';
  }

  private summaryKey(portalId: number | null): string {
    return portalId ? `portal-${portalId}` : 'all';
  }

  labelDifficulty(key: string): string {
    const labels: Record<string, string> = {
      CONTRASENA_NO_FUNCIONA: 'Portal no reconoce usuario/contraseña',
      DATOS_CONTACTO_NO_ACTUALIZADOS: 'Actualizar datos de contacto',
      SIN_CORREO_REGISTRADO: 'Sin correo asociado al Portal',
      NO_RECIBI_COMPARTIR_ESTUDIOS: 'No recibió información para compartir estudios',
      NO_RECIBI_RECUPERAR_CONTRASENA: 'No recibió tarea para recuperar contraseña',
      TUTOR_RESPONSABLE_SIN_ACCESO: 'Tutor/responsable sin acceso',
      OTRO: 'Otro'
    };
    return labels[key] ?? key;
  }

  percent(value: number, total: number): number {
    if (!total) return 0;
    return Math.round((value / total) * 100);
  }
}
