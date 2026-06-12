import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { NavbarComponent } from '../navbar/navbar';

@Component({
  selector: 'app-dormer-dashboard',
  standalone: true,
  imports: [CommonModule, NavbarComponent, ReactiveFormsModule, FormsModule],
  templateUrl: './dormer-dashboard.html',
  styleUrls: ['./dormer-dashboard.css']
})
export class DormerDashboardComponent implements OnInit {
  private http = inject(HttpClient);
  protected authService = inject(AuthService);
  private fb = inject(FormBuilder);

  activeTab = 'room';

  // Room details
  roomData: any = null;
  roomLoading = true;
  roomError: string | null = null;

  // Bills
  bills: any[] = [];
  billsLoading = true;
  payingBillId: number | null = null;

  // Maintenance reports
  maintenanceReports: any[] = [];
  maintenanceLoading = true;
  maintenanceForm!: FormGroup;
  submittingMaintenance = false;
  maintenanceSuccessMsg: string | null = null;

  // Incident reports
  incidentForm!: FormGroup;
  submittingIncident = false;
  incidentSuccessMsg: string | null = null;

  // Leave requests
  leaves: any[] = [];
  leavesLoading = true;
  leaveForm!: FormGroup;
  submittingLeave = false;
  leaveSuccessMsg: string | null = null;

  // Profile Settings
  profileForm!: FormGroup;
  profileLoading = true;
  profileSuccessMsg: string | null = null;
  profileErrorMsg: string | null = null;
  submittingProfile = false;

  // Cleaning booking status
  cleaningSuccessMsg: string | null = null;
  bookingCleaning = false;

  ngOnInit(): void {
    this.initForms();
    this.loadRoomOverview();
    this.loadBills();
    this.loadMaintenanceReports();
    this.loadLeaves();
    this.loadProfile();
  }

  private initForms(): void {
    this.maintenanceForm = this.fb.group({
      remarks: ['', [Validators.required, Validators.maxLength(1000)]],
      photoUrls: [''] // simulated upload
    });

    this.incidentForm = this.fb.group({
      remarks: ['', [Validators.required, Validators.maxLength(2000)]],
      photoUrls: [''] // simulated upload
    });

    this.leaveForm = this.fb.group({
      startDate: ['', Validators.required],
      endDate: [''],
      permanentMoveOut: [false],
      remarks: ['', Validators.maxLength(1000)]
    });

    this.profileForm = this.fb.group({
      fullName: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      password: ['']
    });
  }

  setTab(tab: string): void {
    this.activeTab = tab;
  }

  loadRoomOverview(): void {
    this.roomLoading = true;
    this.roomError = null;
    this.http.get(`${this.authService.getApiUrl()}/dormer/room`).subscribe({
      next: (res: any) => {
        this.roomData = res;
        this.roomLoading = false;
      },
      error: (err) => {
        this.roomError = 'Failed to load room details.';
        this.roomLoading = false;
      }
    });
  }

  loadBills(): void {
    this.billsLoading = true;
    this.http.get<any[]>(`${this.authService.getApiUrl()}/dormer/bills`).subscribe({
      next: (res) => {
        this.bills = res;
        this.billsLoading = false;
      },
      error: () => {
        this.billsLoading = false;
      }
    });
  }

  loadMaintenanceReports(): void {
    this.maintenanceLoading = true;
    this.http.get<any[]>(`${this.authService.getApiUrl()}/dormer/maintenance-reports`).subscribe({
      next: (res) => {
        this.maintenanceReports = res;
        this.maintenanceLoading = false;
      },
      error: () => {
        this.maintenanceLoading = false;
      }
    });
  }

  loadLeaves(): void {
    this.leavesLoading = true;
    this.http.get<any[]>(`${this.authService.getApiUrl()}/dormer/leaves`).subscribe({
      next: (res) => {
        this.leaves = res;
        this.leavesLoading = false;
      },
      error: () => {
        this.leavesLoading = false;
      }
    });
  }

  loadProfile(): void {
    this.profileLoading = true;
    this.http.get(`${this.authService.getApiUrl()}/dormer/profile`).subscribe({
      next: (res: any) => {
        this.profileForm.patchValue({
          fullName: res.fullName,
          phoneNumber: res.phoneNumber
        });
        this.profileLoading = false;
      },
      error: () => {
        this.profileLoading = false;
      }
    });
  }

  bookCleaning(): void {
    this.bookingCleaning = true;
    this.cleaningSuccessMsg = null;
    this.http.post(`${this.authService.getApiUrl()}/dormer/cleaning`, {}).subscribe({
      next: (res: any) => {
        this.cleaningSuccessMsg = res.message;
        this.bookingCleaning = false;
        // Reload profile cleaning fee changes
        this.loadProfile();
      },
      error: () => {
        this.bookingCleaning = false;
      }
    });
  }

  payBill(billId: number): void {
    this.payingBillId = billId;
    this.http.post(`${this.authService.getApiUrl()}/dormer/bills/${billId}/pay`, {}).subscribe({
      next: (res: any) => {
        // Simulated PayMongo redirect. Let's trigger the payment webhook directly from client
        // to complete the simulation in the backend
        const simulatedPayload = {
          paymentId: res.payMongoPaymentId,
          status: 'SUCCESS',
          billId: billId
        };
        this.http.post(`${this.authService.getApiUrl()}/auth/paymongo-webhook`, simulatedPayload).subscribe({
          next: () => {
            this.payingBillId = null;
            this.loadBills();
          },
          error: () => {
            this.payingBillId = null;
          }
        });
      },
      error: () => {
        this.payingBillId = null;
      }
    });
  }

  onSubmitMaintenance(): void {
    if (this.maintenanceForm.invalid) return;
    this.submittingMaintenance = true;
    this.maintenanceSuccessMsg = null;

    this.http.post(`${this.authService.getApiUrl()}/dormer/maintenance-reports`, this.maintenanceForm.value).subscribe({
      next: () => {
        this.submittingMaintenance = false;
        this.maintenanceSuccessMsg = 'Maintenance request submitted successfully!';
        this.maintenanceForm.reset({ remarks: '', photoUrls: '' });
        this.loadMaintenanceReports();
      },
      error: () => {
        this.submittingMaintenance = false;
      }
    });
  }

  onSubmitIncident(): void {
    if (this.incidentForm.invalid) return;
    this.submittingIncident = true;
    this.incidentSuccessMsg = null;

    this.http.post(`${this.authService.getApiUrl()}/dormer/incident-reports`, this.incidentForm.value).subscribe({
      next: () => {
        this.submittingIncident = false;
        this.incidentSuccessMsg = 'Confidential incident report filed. Admin has been notified.';
        this.incidentForm.reset({ remarks: '', photoUrls: '' });
      },
      error: () => {
        this.submittingIncident = false;
      }
    });
  }

  onSubmitLeave(): void {
    if (this.leaveForm.invalid) return;
    this.submittingLeave = true;
    this.leaveSuccessMsg = null;

    this.http.post(`${this.authService.getApiUrl()}/dormer/leaves`, this.leaveForm.value).subscribe({
      next: () => {
        this.submittingLeave = false;
        this.leaveSuccessMsg = 'Leave request submitted successfully!';
        this.leaveForm.reset({ startDate: '', endDate: '', permanentMoveOut: false, remarks: '' });
        this.loadLeaves();
      },
      error: () => {
        this.submittingLeave = false;
      }
    });
  }

  onSubmitProfile(): void {
    if (this.profileForm.invalid) return;
    this.submittingProfile = true;
    this.profileSuccessMsg = null;
    this.profileErrorMsg = null;

    this.http.put(`${this.authService.getApiUrl()}/dormer/profile`, this.profileForm.value).subscribe({
      next: () => {
        this.submittingProfile = false;
        this.profileSuccessMsg = 'Profile updated successfully!';
        this.profileForm.patchValue({ password: '' });
      },
      error: () => {
        this.submittingProfile = false;
        this.profileErrorMsg = 'Failed to update profile details.';
      }
    });
  }
}
