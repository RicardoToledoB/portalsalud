import { Routes } from '@angular/router';
import { RequestFormComponent } from './public/request-form/request-form.component';
import { RequestTrackingComponent } from './public/request-tracking/request-tracking.component';
import { LoginComponent } from './auth/login/login.component';
import { AdminLayoutComponent } from './admin/layout/admin-layout.component';
import { DashboardComponent } from './admin/dashboard/dashboard.component';
import { RequestsComponent } from './admin/requests/requests.component';
import { UsersComponent } from './admin/users/users.component';
import { PortalsComponent } from './admin/portals/portals.component';
import { authGuard, adminGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'solicitud', pathMatch: 'full' },
  { path: 'solicitud', component: RequestFormComponent },
  { path: 'seguimiento', component: RequestTrackingComponent },
  { path: 'login', component: LoginComponent },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'solicitudes', component: RequestsComponent },
      { path: 'usuarios', component: UsersComponent, canActivate: [adminGuard] },
      { path: 'portales', component: PortalsComponent, canActivate: [adminGuard] }
    ]
  },
  { path: '**', redirectTo: 'solicitud' }
];
