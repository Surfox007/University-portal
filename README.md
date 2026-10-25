# 🎓 University Management System (CSE 3230 Lab Task)

A robust, secure University Management System RESTful web application built with **Java 21**, **Spring Boot 3**, **Spring Security 6**, **Spring Data JPA**, **MySQL / H2**, and containerized using **Docker & Docker Compose**.

---

## 📋 Features & Requirements Implemented

1. **REST API**:
   - Full CRUD operations (Create, Read, Update, Delete) for **Admin**, **Teacher**, and **Student** resources.
   - Standard RESTful conventions, descriptive JSON responses, and accurate HTTP status codes (`200 OK`, `201 CREATED`, `400 BAD REQUEST`, `401 UNAUTHORIZED`, `403 FORBIDDEN`, `404 NOT FOUND`).

2. **Spring Security & RBAC (Role-Based Access Control)**:
   - Stateless JWT (JSON Web Token) authentication with login (`/api/auth/login`), logout (`/api/auth/logout`), and profile inspection (`/api/auth/me`).
   - Secure password hashing using **BCrypt** (`BCryptPasswordEncoder`).
   - **Role-Based Permissions**:
     - 🛡️ **Admin**: Can manage **all three resources** (Admin, Teacher, Student).
     - 👨‍🏫 **Teacher**: Can manage **only Teacher resources** (Forbidden from Admin and Student resources).
     - 👨‍🎓 **Student**: Can manage **only Student resources** (Forbidden from Admin and Teacher resources).

3. **Dockerization**:
   - Multi-stage build `Dockerfile` optimizing image size and runtime security (non-root user).
   - `compose.yaml` orchestrating the Spring Boot application and MySQL 8.0 database with automated health checks, persistent volumes, and custom bridge networking.

---

## 👥 Pre-seeded Default Accounts

On startup, `DataInitializer` seeds default accounts so the system is immediately testable:

| Role | Username | Password | Email | Full Name | Associated Resource ID |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | `admin@university.edu` | System Administrator | `ADM-001` |
| **Teacher** | `teacher` | `teacher123` | `teacher@university.edu` | Dr. Alan Turing | `T-101` |
| **Student** | `student` | `student123` | `student@university.edu` | Ada Lovelace | `STU-2024-001` |

---

## 🔐 Role-Based Access Control (RBAC) Matrix

| Endpoint Group | Method | Admin (`ROLE_ADMIN`) | Teacher (`ROLE_TEACHER`) | Student (`ROLE_STUDENT`) | Unauthenticated |
| :--- | :--- | :---: | :---: | :---: | :---: |
| `/api/auth/login` | POST | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 |
| `/api/auth/logout` | POST | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 |
| `/api/auth/me` | GET | ✅ 200 | ✅ 200 | ✅ 200 | ❌ 401 |
| `/api/admins/**` | GET / POST / PUT / DELETE | ✅ Allowed | 🚫 403 Forbidden | 🚫 403 Forbidden | ❌ 401 Unauthorized |
| `/api/teachers/**` | GET / POST / PUT / DELETE | ✅ Allowed | ✅ Allowed | 🚫 403 Forbidden | ❌ 401 Unauthorized |
| `/api/students/**` | GET / POST / PUT / DELETE | ✅ Allowed | 🚫 403 Forbidden | ✅ Allowed | ❌ 401 Unauthorized |

---

## 🚀 How to Run the Application

### Option 1: Run with Docker Compose (Recommended for Production / Lab Submission)

Ensure Docker Desktop or Docker Engine is running, then execute:

```bash
# Build the container image and run both Spring Boot and MySQL
docker compose up --build
```

- Application will be live at: `http://localhost:8085`
- MySQL will be listening on: `localhost:3306`

To shut down:
```bash
docker compose down
```

---

### Option 2: Run Standalone Locally (Instant Development Mode)

The project includes an in-memory **H2 Database** profile enabled by default for rapid local development without needing a local MySQL server installed:

```bash
# On Linux/macOS:
./mvnw spring-boot:run

# On Windows PowerShell:
.\mvnw.cmd spring-boot:run
```

- Application will be live at: `http://localhost:8085`
- Built-in H2 Web Console: `http://localhost:8085/h2-console`
  - JDBC URL: `jdbc:h2:mem:universitydb`
  - Username: `sa`
  - Password: `password`

---

## 🧪 Running Automated Tests

Run the full integration test suite verifying authentication, JWT creation, RBAC authorization, and CRUD operations:

```bash
# Linux / macOS
./mvnw test

# Windows
.\mvnw.cmd test
```

---

## 📡 REST API Documentation & Testing Guide

All protected endpoints require the HTTP Header:
```http
Authorization: Bearer <YOUR_JWT_TOKEN>
```

### 1. Authentication Endpoints

#### **Login**
- **Method**: `POST`
- **URL**: `/api/auth/login`
- **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
- **Response (`200 OK`)**:
  ```json
  {
    "success": true,
    "message": "Authentication successful",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "type": "Bearer",
      "id": 1,
      "username": "admin",
      "email": "admin@university.edu",
      "role": "ROLE_ADMIN"
    },
    "timestamp": "2026-10-24T21:20:00"
  }
  ```

#### **Get Current User**
- **Method**: `GET`
- **URL**: `/api/auth/me`
- **Headers**: `Authorization: Bearer <TOKEN>`

#### **Logout**
- **Method**: `POST`
- **URL**: `/api/auth/logout`

---

### 2. Admin Resource Endpoints (`/api/admins`)
*(Accessible only by `ROLE_ADMIN`)*

- `GET /api/admins` — Get all admins (`200 OK`)
- `GET /api/admins/{id}` — Get admin by ID (`200 OK`)
- `POST /api/admins` — Create a new admin (`201 CREATED`)
  ```json
  {
    "adminCode": "ADM-002",
    "fullName": "Grace Hopper",
    "email": "hopper@university.edu",
    "department": "Computer Systems",
    "phoneNumber": "+1-555-0104",
    "username": "grace",
    "password": "Password123"
  }
  ```
- `PUT /api/admins/{id}` — Update admin (`200 OK`)
- `DELETE /api/admins/{id}` — Delete admin (`200 OK`)

---

### 3. Teacher Resource Endpoints (`/api/teachers`)
*(Accessible by `ROLE_ADMIN` and `ROLE_TEACHER`)*

- `GET /api/teachers` — Get all teachers (`200 OK`)
- `GET /api/teachers/{id}` — Get teacher by ID (`200 OK`)
- `POST /api/teachers` — Create a new teacher (`201 CREATED`)
  ```json
  {
    "teacherCode": "T-102",
    "fullName": "Dr. Donald Knuth",
    "email": "knuth@university.edu",
    "department": "Computer Science",
    "designation": "Professor Emeritus",
    "phoneNumber": "+1-555-0105",
    "username": "knuth",
    "password": "Password123"
  }
  ```
- `PUT /api/teachers/{id}` — Update teacher (`200 OK`)
- `DELETE /api/teachers/{id}` — Delete teacher (`200 OK`)

---

### 4. Student Resource Endpoints (`/api/students`)
*(Accessible by `ROLE_ADMIN` and `ROLE_STUDENT`)*

- `GET /api/students` — Get all students (`200 OK`)
- `GET /api/students/{id}` — Get student by ID (`200 OK`)
- `POST /api/students` — Create a new student (`201 CREATED`)
  ```json
  {
    "studentId": "STU-2024-002",
    "fullName": "Katherine Johnson",
    "email": "katherine@university.edu",
    "department": "Mathematics & Computing",
    "semester": "6th Semester",
    "cgpa": 3.98,
    "phoneNumber": "+1-555-0106",
    "username": "katherine",
    "password": "Password123"
  }
  ```
- `PUT /api/students/{id}` — Update student (`200 OK`)
- `DELETE /api/students/{id}` — Delete student (`200 OK`)

---

## 🏛️ Interactive Academic Web Portal (Thymeleaf & Spring MVC)

In addition to the REST API, the application features an authentic university web portal designed using classic academic **Navy Blue (`#0F2B5C`) & White (`#FFFFFF`)** styling:

### Portal URLs & Flow:
- **Dedicated Login Page**: `http://localhost:8085/login`
  - Includes quick 1-click test credential fill buttons for **Admin**, **Teacher**, and **Student**.
- **Administrator Dashboard**: `http://localhost:8085/admin/dashboard`
  - Full system metrics, directory management for Students, Faculty, and Administrators with interactive Create, Edit, and Delete modals.
- **Faculty Dashboard**: `http://localhost:8085/teacher/dashboard`
  - Faculty directory, department rosters, colleague registration, and profile editing.
- **Student Academic Dashboard**: `http://localhost:8085/student/dashboard`
  - Student academic transcript, enrolled courses, CGPA tracking, student directory, and profile editing.
- **Access Denied Page**: `http://localhost:8085/access-denied`
  - Clear university institutional security page displayed when a user attempts to navigate outside their authorized role.

---

## ⚡ Quick cURL Verification Commands

### Step 1: Login as Student & Obtain Token
```bash
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student","password":"student123"}'
```

### Step 2: Access Student Resources as Student (Allowed ✅ -> 200 OK)
```bash
curl -X GET http://localhost:8085/api/students \
  -H "Authorization: Bearer <STUDENT_TOKEN>"
```

### Step 3: Access Admin Resources as Student (Blocked 🚫 -> 403 Forbidden)
```bash
curl -X GET http://localhost:8085/api/admins \
  -H "Authorization: Bearer <STUDENT_TOKEN>"
```

### Step 4: Login as Admin & Access All Resources (Allowed ✅ -> 200 OK)
```bash
# Get Admin Token
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Access Admins, Teachers, and Students using Admin Token:
curl -X GET http://localhost:8085/api/admins -H "Authorization: Bearer <ADMIN_TOKEN>"
curl -X GET http://localhost:8085/api/teachers -H "Authorization: Bearer <ADMIN_TOKEN>"
curl -X GET http://localhost:8085/api/students -H "Authorization: Bearer <ADMIN_TOKEN>"
```
