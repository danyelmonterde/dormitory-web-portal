import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { NavbarComponent } from '../navbar/navbar';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, NavbarComponent, ReactiveFormsModule, FormsModule],
  templateUrl: './admin-dashboard.html',
  styleUrls: ['./admin-dashboard.css']
})
export class AdminDashboardComponent implements OnInit {
  private http = inject(HttpClient);
  protected authService = inject(AuthService);
  private fb = inject(FormBuilder);

  activeTab = 'users';

  // System Users
  users: any[] = [];
  usersLoading = true;
  userForm!: FormGroup;
  showUserForm = false;
  editingUserId: number | null = null;
  userSuccessMsg: string | null = null;

  // Rooms
  rooms: any[] = [];
  roomsLoading = true;
  roomForm!: FormGroup;
  showRoomForm = false;
  editingRoomId: number | null = null;
  roomSuccessMsg: string | null = null;

  // Facilities
  facilities: any[] = [];
  facilitiesLoading = true;
  facilityForm!: FormGroup;
  showFacilityForm = false;
  editingFacilityId: number | null = null;
  facilitySuccessMsg: string | null = null;

  // Bills & Invoices
  bills: any[] = [];
  billsLoading = true;
  billForm!: FormGroup;
  showBillForm = false;
  billSuccessMsg: string | null = null;

  // Maintenance & Incidents
  maintenanceReports: any[] = [];
  incidentReports: any[] = [];
  reportsLoading = true;
  resolvingReportId: number | null = null;
  resolutionForm!: FormGroup;

  // Leaves
  leaves: any[] = [];
  leavesLoading = true;

  // Webhook Simulator Form
  webhookForm!: FormGroup;
  webhookSuccessMsg: string | null = null;
  submittingWebhook = false;

  ngOnInit(): void {
    this.initForms();
    this.loadUsers();
    this.loadRooms();
    this.loadFacilities();
    this.loadBills();
    this.loadReports();
    this.loadLeaves();
  }

  private initForms(): void {
    this.userForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      fullName: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      role: ['ROLE_DORMER', Validators.required],
      enabled: [true],
      roomId: [''],
      customCleaningFee: [0.0],
      password: ['']
    });

    this.roomForm = this.fb.group({
      roomNumber: ['', Validators.required],
      capacity: [4, [Validators.required, Validators.min(1)]],
      monthlyRent: [5000.0, [Validators.required, Validators.min(0)]],
      wifiPassword: ['']
    });

    this.facilityForm = this.fb.group({
      name: ['', Validators.required],
      status: ['OPERATIONAL', Validators.required],
      remarks: ['']
    });

    this.billForm = this.fb.group({
      tenantUsername: ['', Validators.required],
      billingMonth: ['', Validators.required],
      baseRent: [0.0],
      electricityPrev: [0.0, Validators.required],
      electricityCurr: [0.0, Validators.required],
      electricityPrice: [12.0, Validators.required],
      waterPrev: [0.0, Validators.required],
      waterCurr: [0.0, Validators.required],
      waterPrice: [45.0, Validators.required],
      meterEvidenceUrl: ['']
    });

    this.resolutionForm = this.fb.group({
      status: ['RESOLVED', Validators.required],
      scheduledDate: [''],
      resolverRemarks: ['', Validators.required]
    });

    this.webhookForm = this.fb.group({
      paymentId: ['paymongo_sim_' + Date.now(), Validators.required],
      status: ['SUCCESS', Validators.required],
      billId: ['', Validators.required]
    });
  }

  setTab(tab: string): void {
    this.activeTab = tab;
  }

  // Loader calls
  loadUsers(): void {
    this.usersLoading = true;
    this.http.get<any[]>(`${this.authService.getApiUrl()}/admin/users`).subscribe({
      next: (res) => {
        this.users = res;
        this.usersLoading = false;
      },
      error: () => (this.usersLoading = false)
    });
  }

  loadRooms(): void {
    this.roomsLoading = true;
    this.http.get<any[]>(`${this.authService.getApiUrl()}/admin/rooms`).subscribe({
      next: (res) => {
        this.rooms = res;
        this.roomsLoading = false;
      },
      error: () => (this.roomsLoading = false)
    });
  }

  loadFacilities(): void {
    this.facilitiesLoading = true;
    this.http.get<any[]>(`${this.authService.getApiUrl()}/admin/facilities`).subscribe({
      next: (res) => {
        this.facilities = res;
        this.facilitiesLoading = false;
      },
      error: () => (this.facilitiesLoading = false)
    });
  }

  loadBills(): void {
    this.billsLoading = true;
    this.http.get<any[]>(`${this.authService.getApiUrl()}/admin/bills`).subscribe({
      next: (res) => {
        this.bills = res;
        this.billsLoading = false;
      },
      error: () => (this.billsLoading = false)
    });
  }

  loadReports(): void {
    this.reportsLoading = true;
    this.http.get<any[]>(`${this.authService.getApiUrl()}/admin/maintenance-reports`).subscribe({
      next: (res) => {
        this.maintenanceReports = res;
        this.http.get<any[]>(`${this.authService.getApiUrl()}/admin/incident-reports`).subscribe({
          next: (inc) => {
            this.incidentReports = inc;
            this.reportsLoading = false;
          },
          error: () => (this.reportsLoading = false)
        });
      },
      error: () => (this.reportsLoading = false)
    });
  }

  loadLeaves(): void {
    this.leavesLoading = true;
    this.http.get<any[]>(`${this.authService.getApiUrl()}/admin/leaves`).subscribe({
      next: (res) => {
        this.leaves = res;
        this.leavesLoading = false;
      },
      error: () => (this.leavesLoading = false)
    });
  }

  // Users Management
  openNewUser(): void {
    this.editingUserId = null;
    this.userForm.reset({ role: 'ROLE_DORMER', enabled: true, customCleaningFee: 0.0 });
    this.userForm.get('password')?.setValidators(Validators.required);
    this.userForm.get('password')?.updateValueAndValidity();
    this.showUserForm = true;
  }

  openEditUser(user: any): void {
    this.editingUserId = user.id;
    this.userForm.patchValue({
      username: user.username,
      email: user.email,
      fullName: user.fullName,
      phoneNumber: user.phoneNumber,
      role: user.role,
      enabled: user.enabled,
      roomId: user.roomId || '',
      customCleaningFee: user.customCleaningFee,
      password: ''
    });
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.updateValueAndValidity();
    this.showUserForm = true;
  }

  onSubmitUser(): void {
    if (this.userForm.invalid) return;
    this.userSuccessMsg = null;
    const body = { ...this.userForm.value };
    if (!body.roomId) body.roomId = null;

    if (this.editingUserId) {
      this.http.put(`${this.authService.getApiUrl()}/admin/users/${this.editingUserId}`, body).subscribe({
        next: () => {
          this.userSuccessMsg = 'User updated successfully!';
          this.showUserForm = false;
          this.loadUsers();
        },
        error: (err) => alert(err.error?.message || 'Error updating user')
      });
    } else {
      this.http.post(`${this.authService.getApiUrl()}/admin/users`, body).subscribe({
        next: () => {
          this.userSuccessMsg = 'User created successfully!';
          this.showUserForm = false;
          this.loadUsers();
        },
        error: (err) => alert(err.error?.message || 'Error creating user')
      });
    }
  }

  deleteUser(id: number): void {
    if (!confirm('Are you sure you want to delete this user?')) return;
    this.http.delete(`${this.authService.getApiUrl()}/admin/users/${id}`).subscribe({
      next: () => {
        this.loadUsers();
      }
    });
  }

  // Rooms Management
  openNewRoom(): void {
    this.editingRoomId = null;
    this.roomForm.reset({ capacity: 4, monthlyRent: 5000.0 });
    this.showRoomForm = true;
  }

  openEditRoom(room: any): void {
    this.editingRoomId = room.id;
    this.roomForm.patchValue(room);
    this.showRoomForm = true;
  }

  onSubmitRoom(): void {
    if (this.roomForm.invalid) return;
    this.roomSuccessMsg = null;

    if (this.editingRoomId) {
      this.http.put(`${this.authService.getApiUrl()}/admin/rooms/${this.editingRoomId}`, this.roomForm.value).subscribe({
        next: () => {
          this.roomSuccessMsg = 'Room updated successfully!';
          this.showRoomForm = false;
          this.loadRooms();
        },
        error: (err) => alert(err.error?.message || 'Error updating room')
      });
    } else {
      this.http.post(`${this.authService.getApiUrl()}/admin/rooms`, this.roomForm.value).subscribe({
        next: () => {
          this.roomSuccessMsg = 'Room created successfully!';
          this.showRoomForm = false;
          this.loadRooms();
        },
        error: (err) => alert(err.error?.message || 'Error creating room')
      });
    }
  }

  deleteRoom(id: number): void {
    if (!confirm('Are you sure you want to delete this room?')) return;
    this.http.delete(`${this.authService.getApiUrl()}/admin/rooms/${id}`).subscribe({
      next: () => this.loadRooms()
    });
  }

  // Facility Management
  openNewFacility(): void {
    this.editingFacilityId = null;
    this.facilityForm.reset({ status: 'OPERATIONAL' });
    this.showFacilityForm = true;
  }

  openEditFacility(facility: any): void {
    this.editingFacilityId = facility.id;
    this.facilityForm.patchValue(facility);
    this.showFacilityForm = true;
  }

  onSubmitFacility(): void {
    if (this.facilityForm.invalid) return;
    this.facilitySuccessMsg = null;

    if (this.editingFacilityId) {
      this.http.put(`${this.authService.getApiUrl()}/admin/facilities/${this.editingFacilityId}`, this.facilityForm.value).subscribe({
        next: () => {
          this.facilitySuccessMsg = 'Facility updated successfully!';
          this.showFacilityForm = false;
          this.loadFacilities();
        },
        error: (err) => alert(err.error?.message || 'Error updating facility')
      });
    } else {
      this.http.post(`${this.authService.getApiUrl()}/admin/facilities`, this.facilityForm.value).subscribe({
        next: () => {
          this.facilitySuccessMsg = 'Facility created successfully!';
          this.showFacilityForm = false;
          this.loadFacilities();
        },
        error: (err) => alert(err.error?.message || 'Error creating facility')
      });
    }
  }

  deleteFacility(id: number): void {
    if (!confirm('Are you sure you want to delete this facility?')) return;
    this.http.delete(`${this.authService.getApiUrl()}/admin/facilities/${id}`).subscribe({
      next: () => this.loadFacilities()
    });
  }

  // Billing Management
  openNewBill(): void {
    this.billForm.reset({
      electricityPrice: 12.0,
      waterPrice: 45.0,
      electricityPrev: 0.0,
      electricityCurr: 0.0,
      waterPrev: 0.0,
      waterCurr: 0.0,
      baseRent: 0.0
    });
    this.showBillForm = true;
  }

  onSubmitBill(): void {
    if (this.billForm.invalid) return;
    this.billSuccessMsg = null;

    this.http.post(`${this.authService.getApiUrl()}/admin/bills`, this.billForm.value).subscribe({
      next: () => {
        this.billSuccessMsg = 'Invoice generated successfully!';
        this.showBillForm = false;
        this.loadBills();
        // reload users (custom cleaning fee resets after billing)
        this.loadUsers();
      },
      error: (err) => alert(err.error?.message || 'Error generating invoice')
    });
  }

  deleteBill(id: number): void {
    if (!confirm('Are you sure you want to delete this invoice?')) return;
    this.http.delete(`${this.authService.getApiUrl()}/admin/bills/${id}`).subscribe({
      next: () => this.loadBills()
    });
  }

  // Reports
  openResolveReport(report: any): void {
    this.resolvingReportId = report.id;
    this.resolutionForm.reset({
      status: report.status || 'RESOLVED',
      scheduledDate: report.scheduledDate ? report.scheduledDate.substring(0, 16) : '',
      resolverRemarks: report.resolverRemarks || ''
    });
  }

  onSubmitResolution(): void {
    if (this.resolutionForm.invalid || !this.resolvingReportId) return;

    this.http.put(`${this.authService.getApiUrl()}/admin/maintenance-reports/${this.resolvingReportId}`, this.resolutionForm.value).subscribe({
      next: () => {
        this.resolvingReportId = null;
        this.loadReports();
      }
    });
  }

  // Leaves
  updateLeave(id: number, status: string): void {
    this.http.patch(`${this.authService.getApiUrl()}/admin/leaves/${id}`, { status }).subscribe({
      next: () => this.loadLeaves()
    });
  }

  // Webhook Simulator
  onSubmitWebhook(): void {
    if (this.webhookForm.invalid) return;
    this.submittingWebhook = true;
    this.webhookSuccessMsg = null;

    this.http.post(`${this.authService.getApiUrl()}/auth/paymongo-webhook`, this.webhookForm.value).subscribe({
      next: () => {
        this.submittingWebhook = false;
        this.webhookSuccessMsg = 'Simulated PayMongo Webhook processed successfully! Bill status updated.';
        this.webhookForm.reset({
          paymentId: 'paymongo_sim_' + Date.now(),
          status: 'SUCCESS',
          billId: ''
        });
        this.loadBills();
      },
      error: (err) => {
        this.submittingWebhook = false;
        alert('Webhook failed: ' + (err.error?.message || err.message));
      }
    });
  }
}
