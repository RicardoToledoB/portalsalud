import { Component, ElementRef, OnInit, ViewChild, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { PortalTopic, SupportPortal } from '../../core/models';

@Component({
  selector: 'app-request-form',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, MatButtonModule, MatCardModule, MatCheckboxModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule, MatSelectModule],
  templateUrl: './request-form.component.html',
  styleUrl: './request-form.component.scss'
})
export class RequestFormComponent implements OnInit {
  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  private readonly maxFiles = 5;
  private readonly maxFileSizeBytes = 5 * 1024 * 1024;
  private readonly allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
  private readonly allowedExtensions = ['jpg', 'jpeg', 'png', 'webp'];

  loading = signal(false);
  folio = signal<string | null>(null);
  error = signal<string | null>(null);
  captchaQuestion = signal<string>('');
  selectedFiles = signal<File[]>([]);
  attachmentError = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    portalId: [0, [Validators.required, Validators.min(1)]],
    fullName: ['', [Validators.required, Validators.maxLength(150)]],
    rut: ['', [Validators.required, Validators.maxLength(20), rutValidator()]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    phone: ['', [mobilePhoneValidator(), Validators.maxLength(50)]],
    fixedPhone: ['', [fixedPhoneValidator(), Validators.maxLength(50)]],
    topicId: [0, [Validators.required, Validators.min(1)]],
    difficultyType: ['CONTRASENA_NO_FUNCIONA', Validators.required],
    otherDetail: ['', Validators.maxLength(500)],
    userObservation: ['', Validators.maxLength(1000)],
    consentAccepted: [false, Validators.requiredTrue],
    captchaId: ['', Validators.required],
    captchaAnswer: ['', [Validators.required, Validators.maxLength(10)]],
    website: ['']
  }, { validators: [atLeastOnePhoneValidator()] });

  selectedDifficulty = computed(() => this.form.controls.difficultyType.value);

  portals = signal<SupportPortal[]>([]);
  topics = signal<PortalTopic[]>([]);

  constructor(private fb: FormBuilder, private api: ApiService) {
    this.form.controls.difficultyType.valueChanges.subscribe(value => this.applyConditionalValidators(value));
    this.form.controls.portalId.valueChanges.subscribe(portalId => this.loadTopics(Number(portalId)));
    this.form.controls.topicId.valueChanges.subscribe(topicId => this.onTopicChanged(Number(topicId))); 
  }

  ngOnInit(): void {
    this.loadPortals();
    this.applyConditionalValidators(this.form.controls.difficultyType.value);
    this.loadCaptcha();
  }

  loadPortals(): void {
    this.api.getPublicPortals().subscribe({
      next: portals => {
        this.portals.set(portals);
        const defaultPortal = portals.find(p => p.code === 'PORTAL_IMAGENES') ?? portals[0];
        if (defaultPortal) {
          this.form.patchValue({ portalId: defaultPortal.id });
          this.loadTopics(defaultPortal.id);
        }
      },
      error: () => this.error.set('No fue posible cargar los portales disponibles.')
    });
  }

  loadTopics(portalId: number): void {
    if (!portalId) {
      this.topics.set([]);
      return;
    }
    this.api.getPublicTopics(portalId).subscribe({
      next: topics => {
        this.topics.set(topics);
        const current = topics.find(t => t.id === this.form.controls.topicId.value);
        const defaultTopic = current ?? topics[0];
        if (defaultTopic) {
          this.form.patchValue({ topicId: defaultTopic.id, difficultyType: this.legacyDifficulty(defaultTopic.code) });
          this.applyConditionalValidators(this.legacyDifficulty(defaultTopic.code), defaultTopic.requiresDetail || defaultTopic.code === 'OTRO');
        }
      },
      error: () => this.error.set('No fue posible cargar las temáticas del portal seleccionado.')
    });
  }

  onTopicChanged(topicId: number): void {
    const topic = this.topics().find(t => t.id === topicId);
    if (!topic) return;
    const legacy = this.legacyDifficulty(topic.code);
    this.form.patchValue({ difficultyType: legacy }, { emitEvent: false });
    this.applyConditionalValidators(legacy, topic.requiresDetail || topic.code === 'OTRO');
  }

  loadCaptcha(): void {
    this.api.getCaptcha().subscribe({
      next: captcha => {
        this.captchaQuestion.set(captcha.question);
        this.form.patchValue({ captchaId: captcha.captchaId, captchaAnswer: '' });
      },
      error: () => this.error.set('No fue posible cargar la validación anti-spam. Recargue la página e intente nuevamente.')
    });
  }

  openFileSelector(): void {
    this.fileInput?.nativeElement.click();
  }

  onFilesSelected(event: Event): void {
    this.attachmentError.set(null);
    const input = event.target as HTMLInputElement;
    const incoming = Array.from(input.files ?? []);
    input.value = '';
    if (!incoming.length) return;

    const current = [...this.selectedFiles()];
    for (const file of incoming) {
      const validation = this.validateFile(file, current.length);
      if (validation) {
        this.attachmentError.set(validation);
        continue;
      }
      current.push(file);
    }
    this.selectedFiles.set(current.slice(0, this.maxFiles));
  }

  removeFile(index: number): void {
    const files = [...this.selectedFiles()];
    files.splice(index, 1);
    this.selectedFiles.set(files);
    this.attachmentError.set(null);
  }

  clearFiles(): void {
    this.selectedFiles.set([]);
    this.attachmentError.set(null);
  }

  fileSizeLabel(bytes: number): string {
    if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`;
    return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  }

  submit(): void {
    this.error.set(null);
    this.attachmentError.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const invalidAttachment = this.selectedFiles().map((file, index) => this.validateFile(file, index)).find(Boolean);
    if (invalidAttachment) {
      this.attachmentError.set(invalidAttachment);
      return;
    }
    this.loading.set(true);
    const raw = this.form.getRawValue();
    const payload = { ...raw, portalType: 'PORTAL_IMAGENES' };
    this.api.createPublicRequest(payload, this.selectedFiles()).subscribe({
      next: result => {
        this.folio.set(result.folio);
        this.form.reset({
          portalId: this.portals()[0]?.id ?? 0,
          topicId: this.topics()[0]?.id ?? 0,
          difficultyType: this.topics()[0] ? this.legacyDifficulty(this.topics()[0].code) : 'CONTRASENA_NO_FUNCIONA',
          consentAccepted: false,
          fullName: '', rut: '', email: '', phone: '', fixedPhone: '', otherDetail: '', userObservation: '', captchaId: '', captchaAnswer: '', website: ''
        });
        this.clearFiles();
        this.loadCaptcha();
        this.loading.set(false);
      },
      error: err => {
        this.error.set(err?.error?.message ?? 'No fue posible registrar la solicitud. Intente nuevamente.');
        this.loadCaptcha();
        this.loading.set(false);
      }
    });
  }

  private validateFile(file: File, currentIndex: number): string | null {
    if (currentIndex >= this.maxFiles) return `Puede adjuntar máximo ${this.maxFiles} imágenes.`;
    const extension = file.name.split('.').pop()?.toLowerCase() ?? '';
    if (!this.allowedExtensions.includes(extension) || (file.type && !this.allowedTypes.includes(file.type))) {
      return 'Solo se permiten imágenes JPG, PNG o WEBP.';
    }
    if (file.size > this.maxFileSizeBytes) {
      return 'Cada imagen debe pesar máximo 5 MB.';
    }
    return null;
  }

  private applyConditionalValidators(value: string, forceDetail = false): void {
    const email = this.form.controls.email;
    const other = this.form.controls.otherDetail;
    email.clearValidators();
    other.clearValidators();
    email.addValidators([Validators.email, Validators.maxLength(150)]);
    other.addValidators([Validators.maxLength(500)]);
    if (value !== 'SIN_CORREO_REGISTRADO') email.addValidators([Validators.required]);
    if (forceDetail) other.addValidators([Validators.required]);
    email.updateValueAndValidity({ emitEvent: false });
    other.updateValueAndValidity({ emitEvent: false });
    this.form.updateValueAndValidity({ emitEvent: false });
  }

  requiresOtherDetail(): boolean {
    const topic = this.topics().find(t => t.id === this.form.controls.topicId.value);
    return !!topic?.requiresDetail || topic?.code === 'OTRO';
  }

  private legacyDifficulty(code: string): 'CONTRASENA_NO_FUNCIONA' | 'DATOS_CONTACTO_NO_ACTUALIZADOS' | 'SIN_CORREO_REGISTRADO' | 'OTRO' {
    const allowed = ['CONTRASENA_NO_FUNCIONA', 'DATOS_CONTACTO_NO_ACTUALIZADOS', 'SIN_CORREO_REGISTRADO', 'OTRO'];
    return allowed.includes(code) ? code as any : 'OTRO';
  }
}

function rutValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = String(control.value ?? '').replace(/[.\-\s]/g, '').toUpperCase();
    if (!value) return null;
    if (value.length < 2 || value.length > 9) return { rut: true };
    const body = value.slice(0, -1);
    const dv = value.slice(-1);
    if (!/^\d+$/.test(body)) return { rut: true };
    let sum = 0;
    let multiplier = 2;
    for (let i = body.length - 1; i >= 0; i--) {
      sum += Number(body[i]) * multiplier;
      multiplier = multiplier === 7 ? 2 : multiplier + 1;
    }
    const rest = 11 - (sum % 11);
    const expected = rest === 11 ? '0' : rest === 10 ? 'K' : String(rest);
    return expected === dv ? null : { rut: true };
  };
}

function mobilePhoneValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = String(control.value ?? '').trim();
    if (!value) return null;
    const digits = value.replace(/\D/g, '');
    const ok = (digits.length === 9 && digits.startsWith('9')) || (digits.length === 11 && digits.startsWith('569'));
    return ok ? null : { mobile: true };
  };
}

function fixedPhoneValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = String(control.value ?? '').trim();
    if (!value) return null;
    const digits = value.replace(/\D/g, '');
    return digits.length >= 7 && digits.length <= 12 ? null : { fixedPhone: true };
  };
}

function atLeastOnePhoneValidator(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const phone = String(group.get('phone')?.value ?? '').trim();
    const fixedPhone = String(group.get('fixedPhone')?.value ?? '').trim();
    return phone || fixedPhone ? null : { contactPhoneRequired: true };
  };
}
