import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '../../core/api.service';
import { PortalTopic, SupportPortal, UserDto, UserRole } from '../../core/models';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule, MatTableModule, MatTooltipModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss'
})
export class UsersComponent implements OnInit {
  users = signal<UserDto[]>([]);
  portals = signal<SupportPortal[]>([]);
  topics = signal<PortalTopic[]>([]);
  error = signal<string | null>(null);
  success = signal<string | null>(null);
  editingUser = signal<UserDto | null>(null);
  displayedColumns = ['fullName', 'email', 'role', 'assignment', 'active', 'actions'];
  roles: UserRole[] = ['ADMIN', 'REFERENTE_DSSM'];

  form = this.fb.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['REFERENTE_DSSM' as UserRole, Validators.required],
    portalIds: [[] as number[]],
    topicId: [0]
  });

  constructor(private fb: FormBuilder, private api: ApiService) {}

  ngOnInit(): void {
    this.load();
    this.loadPortals();
    this.form.controls.portalIds.valueChanges.subscribe(value => this.onPortalSelectionChange(value ?? []));
    this.form.controls.role.valueChanges.subscribe(role => this.onRoleChange(role));
  }

  load(): void { this.api.getUsers().subscribe(users => this.users.set(users)); }

  loadPortals(): void {
    this.api.getAdminPortals().subscribe(portals => {
      const active = portals.filter(p => p.active);
      this.portals.set(active);
      const defaultPortal = active.find(p => p.code === 'PORTAL_IMAGENES') ?? active[0];
      if (defaultPortal && !this.form.controls.portalIds.value?.length) {
        this.form.patchValue({ portalIds: [defaultPortal.id] });
      }
    });
  }

  onRoleChange(role: UserRole | null): void {
    if (role !== 'REFERENTE_DSSM') {
      this.form.patchValue({ portalIds: [], topicId: 0 }, { emitEvent: false });
      this.topics.set([]);
    } else if (!this.form.controls.portalIds.value?.length && this.portals().length) {
      this.form.patchValue({ portalIds: [this.portals()[0].id] });
    }
  }

  onPortalSelectionChange(portalIds: number[]): void {
    if (this.form.controls.role.value !== 'REFERENTE_DSSM') return;
    if (!portalIds.length) {
      this.topics.set([]);
      this.form.patchValue({ topicId: 0 }, { emitEvent: false });
      return;
    }
    if (portalIds.length === 1) {
      this.loadTopics(portalIds[0], this.form.controls.topicId.value ?? 0);
    } else {
      this.topics.set([]);
      this.form.patchValue({ topicId: 0 }, { emitEvent: false });
    }
  }

  loadTopics(portalId: number, selectedTopicId = 0): void {
    this.api.getAdminTopics(portalId).subscribe(topics => {
      this.topics.set(topics.filter(t => t.active));
      const exists = selectedTopicId && topics.some(t => t.id === selectedTopicId);
      this.form.patchValue({ topicId: exists ? selectedTopicId : 0 }, { emitEvent: false });
    });
  }

  submit(): void {
    this.error.set(null); this.success.set(null);
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const raw = this.form.getRawValue();
    const role = raw.role ?? 'REFERENTE_DSSM';
    const portalIds = raw.portalIds ?? [];
    if (role === 'REFERENTE_DSSM' && !portalIds.length) { this.error.set('Debe asignar al menos un portal al referente DSSM.'); return; }

    const singleTopicId: number | undefined = portalIds.length === 1 && raw.topicId ? raw.topicId : undefined;
    const portalAssignments = role === 'REFERENTE_DSSM'
      ? portalIds.map(portalId => {
          const assignment: { portalId: number; topicId?: number } = { portalId };
          if (singleTopicId) assignment.topicId = singleTopicId;
          return assignment;
        })
      : [];

    const current = this.editingUser();
    if (current) {
      const payload: any = {
        fullName: raw.fullName ?? '',
        email: raw.email ?? '',
        role,
        portalId: role === 'REFERENTE_DSSM' && portalIds.length ? portalIds[0] : null,
        topicId: singleTopicId,
        portalAssignments,
        difficultyType: null
      };
      if (raw.password) payload.password = raw.password;
      this.api.updateUser(current.id, payload).subscribe({
        next: () => {
          this.success.set('Usuario actualizado correctamente.');
          this.cancelEdit();
          this.load();
        },
        error: err => this.error.set(err?.error?.message ?? 'No fue posible actualizar el usuario.')
      });
      return;
    }

    this.api.createUser({
      fullName: raw.fullName ?? '',
      email: raw.email ?? '',
      password: raw.password ?? '',
      role,
      portalId: role === 'REFERENTE_DSSM' && portalIds.length ? portalIds[0] : null,
      topicId: singleTopicId,
      portalAssignments,
      difficultyType: null
    }).subscribe({
      next: () => {
        this.success.set('Usuario creado correctamente.');
        this.resetCreateForm();
        this.load();
      },
      error: err => this.error.set(err?.error?.message ?? 'No fue posible crear el usuario.')
    });
  }

  edit(user: UserDto): void {
    this.error.set(null); this.success.set(null);
    this.editingUser.set(user);
    this.form.controls.password.clearValidators();
    this.form.controls.password.setValidators([Validators.minLength(8)]);
    this.form.controls.password.updateValueAndValidity();
    const portalIds = user.portalAssignments?.length
      ? user.portalAssignments.map(a => a.portalId)
      : (user.portalId ? [user.portalId] : []);
    const topicId = user.portalAssignments?.length === 1 ? (user.portalAssignments[0].topicId ?? 0) : (user.topicId ?? 0);
    this.form.patchValue({
      fullName: user.fullName,
      email: user.email,
      password: '',
      role: user.role,
      portalIds,
      topicId
    });
    if (portalIds.length === 1) this.loadTopics(portalIds[0], topicId);
  }

  cancelEdit(): void {
    this.editingUser.set(null);
    this.form.controls.password.clearValidators();
    this.form.controls.password.setValidators([Validators.required, Validators.minLength(8)]);
    this.form.controls.password.updateValueAndValidity();
    this.resetCreateForm();
  }

  resetCreateForm(): void {
    this.form.reset({
      role: 'REFERENTE_DSSM',
      portalIds: this.portals()[0]?.id ? [this.portals()[0].id] : [],
      topicId: 0,
      fullName: '',
      email: '',
      password: ''
    });
  }

  resetPassword(user: UserDto): void {
    const password = prompt(`Nueva contraseña temporal para ${user.fullName}`);
    if (!password) return;
    if (password.length < 8) { this.error.set('La contraseña debe tener al menos 8 caracteres.'); return; }
    this.api.resetUserPassword(user.id, password).subscribe({
      next: () => this.success.set('Contraseña temporal actualizada.'),
      error: err => this.error.set(err?.error?.message ?? 'No fue posible resetear la contraseña.')
    });
  }

  toggleActive(user: UserDto): void {
    const req = user.active ? this.api.disableUser(user.id) : this.api.enableUser(user.id);
    req.subscribe({
      next: () => this.load(),
      error: err => this.error.set(err?.error?.message ?? 'No fue posible cambiar el estado del usuario.')
    });
  }

  labelRole(role: UserRole): string {
    return role === 'ADMIN' ? 'Administrador' : 'Referente DSSM';
  }

  assignmentText(user: UserDto): string {
    if (user.role === 'ADMIN') return 'Todos los portales';
    const assignments = user.portalAssignments ?? [];
    if (!assignments.length) return user.portalName || 'Sin portal';
    return assignments.map(a => a.portalName).join(', ');
  }

  assignmentHint(user: UserDto): string {
    if (user.role === 'ADMIN') return 'Acceso administrador';
    const assignments = user.portalAssignments ?? [];
    if (!assignments.length) return user.topicName || 'Todas las temáticas del portal';
    if (assignments.length > 1) return 'Acceso a más de un portal';
    return assignments[0].topicName || 'Todas las temáticas del portal';
  }

  multiplePortalsSelected(): boolean {
    return (this.form.controls.portalIds.value ?? []).length > 1;
  }
}
