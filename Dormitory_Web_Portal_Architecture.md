# Dormitory Web Portal Architecture and Implementation Guide

This document outlines the architecture, features, and deployment pipeline for the Dormitory Web Portal. The system provides a mobile-friendly Angular frontend, a Spring Boot REST API backend, and a MySQL database, designed with S.O.L.I.D principles and secure credential management.

---

## 1. System Architecture & Tech Stack

* **Frontend:** Angular (Mobile-friendly, responsive design)
* **Backend:** Spring Boot (Java 21), Maven
* **Database:** MySQL (Separate instances for Dev, UAT, Prod)
* **CI/CD:** Jenkins (Local orchestrator)
* **Containerization:** Podman (Local testing), Docker (Deployment)
* **Infrastructure:** AWS EC2 (Minimum 8GB RAM, 8GB Storage per instance)
* **Secrets Management:** AWS Secrets Manager (Remote), `.env` (Local)
* **Payment Gateway:** PayMongo API

### Security & Best Practices
* **Zero Hardcoded Secrets:** No credentials, passwords, or `.env` files will be committed to version control.
* **Data Masking:** Tenant contact information is masked in the UI.
* **Password Encryption:** All passwords are encrypted using BCrypt before database persistence.
* **Data Transfer:** Utilizing Apache Commons BeanUtils for robust and efficient Entity-to-DTO mapping.

---

## 2. Feature Specifications

### A. Authentication Module
* **Role-Based Login:** Access for `ADMIN` and `DORMER`.
* **Forgot Password:** Forgot password flow via email link.
* **Initial Seeding:** Default administrator and sample dormer accounts are seeded directly via initial database migration scripts (e.g., Flyway/Liquibase), not hardcoded in the application layer.

### B. My Room (Dormer Dashboard)
* **Room Overview:** Displays room details and co-tenant information (emails and phone numbers are masked, e.g., `j***@gmail.com`, `+63912***4567`).
* **Maintenance Reports:** Users can report broken furniture/appliances with remarks and up to 3 photo attachments.
    * *Admin Flow:* Receives notification, verifies the report, and schedules a fix. The system notifies the user via portal and email. Admins can view the user's unmasked contact info.
* **Incident Reports:** Users can confidentially report misbehaving tenants with photo evidence. Notifies Admin only.
* **WiFi Access:** View the room's current WiFi password and report connectivity issues.
* **Cleaning Requests:** Users can book room cleaning appointments. The admin-configurable cleaning fee is automatically appended to the monthly rent.

### C. My Bills
* **Monthly Breakdown:** Displays Rent, Electricity, and Water.
* **Utility Logic:**
    * **Electricity:** Previous reading, current reading, price per kWh (in PHP, ₱), total amount.
    * **Water:** Previous reading, current reading, price per cubic meter (in PHP, ₱), total amount.
* **Admin Controls:**
    * **Rent Management:** Admin can add and modify the specific **monthly rent** amount for each user.
    * **Utility Management:** Admin inputs readings, prices, and capture dates. Admins can upload photos/PDFs of the physical meters and physical bills.
* **Payment Integration:** Single-click payment for all pending bills via PayMongo.
* **Webhook Integration:** PayMongo webhook notifies the Admin upon successful payment, detailing the sender and updating the invoice status.
* **Monitoring & Reminders:** Admins view a master list of tenants and payment statuses. Admins can trigger email and dashboard reminders for pending bills.
* **History:** Both roles can access historical bills, payment receipts, and meter evidence.

### D. My Leaves
* **Leave Requests:** Users submit forms detailing vacation dates or permanent move-out dates, alerting the admin for security and billing adjustments.

### E. My Settings
* **User Management (Admin):** CRUD operations for system users.
* **Facility Management (Admin):** CRUD operations for rooms, facilities, and appliances.
* **Profile Management (All):** Users and Admins can update their personal passwords (always hashed).

---

## 3. Database Initialization Strategy

Initial accounts are injected via an SQL initialization script. Passwords must be pre-hashed using BCrypt.
