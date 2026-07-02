import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '../../core/api.service';
import { PortalTopic, SupportPortal } from '../../core/models';

@Component({
  selector: 'app-portals',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatCardModule, MatCheckboxModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule, MatTableModule, MatTooltipModule],
  templateUrl: './portals.component.html',
  styleUrl: './portals.component.scss'
})
export class PortalsComponent implements OnInit {
  portals = signal<SupportPortal[]>([]);
  topics = signal<PortalTopic[]>([]);
  selectedPortal = signal<SupportPortal | null>(null);
  editingPortalId = signal<number | null>(null);
  editingTopicId = signal<number | null>(null);
  error = signal<string | null>(null);
  success = signal<string | null>(null);

  portalColumns = ['name', 'code', 'observation', 'active', 'order', 'actions'];
  topicColumns = ['name', 'code', 'requiresDetail', 'requiresTutorContact', 'active', 'order', 'actions'];

  portalForm = this.fb.group({
    code: ['', [Validators.required, Validators.maxLength(80)]],
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', Validators.maxLength(500)],
    active: [true],
    allowUserObservation: [true],
    displayOrder: [0]
  });

  topicForm = this.fb.group({
    code: ['', [Validators.required, Validators.maxLength(80)]],
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', Validators.maxLength(500)],
    active: [true],
    requiresDetail: [false],
    requiresTutorContact: [false],
    displayOrder: [0]
  });

  constructor(private fb: FormBuilder, private api: ApiService) {}

  ngOnInit(): void {
    this.loadPortals();
  }

  loadPortals(): void {
    this.api.getAdminPortals().subscribe({
      next: portals => {
        this.portals.set(portals);
        const current = this.selectedPortal();
        const selected = current ? portals.find(p => p.id === current.id) : (portals.find(p => p.code === 'PORTAL_IMAGENES') ?? portals[0]);
        if (selected) this.selectPortal(selected);
      },
      error: err => this.error.set(err?.error?.message ?? 'No fue posible cargar los portales.')
    });
  }

  selectPortal(portal: SupportPortal): void {
    this.selectedPortal.set(portal);
    this.cancelTopicEdit();
    this.loadTopics(portal.id);
  }

  newPortal(): void {
    this.cancelPortalEdit();
    this.success.set(null);
    this.error.set(null);
  }

  loadTopics(portalId: number): void {
    this.api.getAdminTopics(portalId).subscribe({
      next: topics => this.topics.set(topics),
      error: err => this.error.set(err?.error?.message ?? 'No fue posible cargar las temáticas.')
    });
  }

  editPortal(portal: SupportPortal): void {
    this.editingPortalId.set(portal.id);
    this.portalForm.setValue({
      code: portal.code,
      name: portal.name,
      description: portal.description ?? '',
      active: portal.active,
      allowUserObservation: portal.allowUserObservation !== false,
      displayOrder: portal.displayOrder ?? 0
    });
  }

  cancelPortalEdit(): void {
    this.editingPortalId.set(null);
    this.portalForm.reset({ code: '', name: '', description: '', active: true, allowUserObservation: true, displayOrder: 0 });
  }

  savePortal(): void {
    this.error.set(null); this.success.set(null);
    if (this.portalForm.invalid) { this.portalForm.markAllAsTouched(); return; }
    const raw = this.portalForm.getRawValue();
    const payload = {
      code: raw.code ?? '',
      name: raw.name ?? '',
      description: raw.description ?? '',
      active: !!raw.active,
      allowUserObservation: raw.allowUserObservation !== false,
      displayOrder: Number(raw.displayOrder ?? 0)
    };
    const id = this.editingPortalId();
    const req = id ? this.api.updatePortal(id, payload) : this.api.createPortal(payload);
    req.subscribe({
      next: portal => {
        this.success.set(id ? 'Portal actualizado correctamente.' : 'Portal creado correctamente.');
        this.cancelPortalEdit();
        this.selectedPortal.set(portal);
        this.topics.set([]);
        this.loadPortals();
      },
      error: err => this.error.set(err?.error?.message ?? 'No fue posible guardar el portal.')
    });
  }

  editTopic(topic: PortalTopic): void {
    this.editingTopicId.set(topic.id);
    this.topicForm.setValue({
      code: topic.code,
      name: topic.name,
      description: topic.description ?? '',
      active: topic.active,
      requiresDetail: topic.requiresDetail,
      requiresTutorContact: topic.requiresTutorContact,
      displayOrder: topic.displayOrder ?? 0
    });
  }

  cancelTopicEdit(): void {
    this.editingTopicId.set(null);
    this.topicForm.reset({ code: '', name: '', description: '', active: true, requiresDetail: false, requiresTutorContact: false, displayOrder: 0 });
  }

  saveTopic(): void {
    this.error.set(null); this.success.set(null);
    const portal = this.selectedPortal();
    if (!portal) { this.error.set('Seleccione un portal antes de crear temáticas.'); return; }
    if (this.topicForm.invalid) { this.topicForm.markAllAsTouched(); return; }
    const raw = this.topicForm.getRawValue();
    const payload = {
      code: raw.code ?? '',
      name: raw.name ?? '',
      description: raw.description ?? '',
      active: !!raw.active,
      requiresDetail: !!raw.requiresDetail,
      requiresTutorContact: !!raw.requiresTutorContact,
      displayOrder: Number(raw.displayOrder ?? 0)
    };
    const id = this.editingTopicId();
    const req = id ? this.api.updateTopic(portal.id, id, payload) : this.api.createTopic(portal.id, payload);
    req.subscribe({
      next: () => {
        this.success.set(id ? 'Temática actualizada correctamente.' : 'Temática creada correctamente.');
        this.cancelTopicEdit();
        this.loadTopics(portal.id);
        this.loadPortals();
      },
      error: err => this.error.set(err?.error?.message ?? 'No fue posible guardar la temática.')
    });
  }
}
