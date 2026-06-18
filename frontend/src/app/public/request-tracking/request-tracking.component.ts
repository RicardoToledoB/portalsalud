import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ApiService } from '../../core/api.service';
import { PublicRequestStatus } from '../../core/models';

@Component({
  selector: 'app-request-tracking',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule],
  templateUrl: './request-tracking.component.html',
  styleUrl: './request-tracking.component.scss'
})
export class RequestTrackingComponent implements OnInit {
  loading = signal(false);
  error = signal<string | null>(null);
  result = signal<PublicRequestStatus | null>(null);

  form = this.fb.nonNullable.group({
    folio: ['', [Validators.required, Validators.maxLength(30)]],
    rut: ['', [Validators.required, Validators.maxLength(20)]]
  });

  constructor(private fb: FormBuilder, private api: ApiService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    const folio = this.route.snapshot.queryParamMap.get('folio');
    if (folio) {
      this.form.patchValue({ folio });
    }
  }

  search(): void {
    this.error.set(null);
    this.result.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    this.loading.set(true);
    this.api.getPublicRequestStatus(raw.folio.trim(), raw.rut.trim()).subscribe({
      next: result => {
        this.result.set(result);
        this.loading.set(false);
      },
      error: err => {
        this.error.set(err?.error?.message ?? 'No fue posible encontrar la solicitud con los datos ingresados.');
        this.loading.set(false);
      }
    });
  }
}
