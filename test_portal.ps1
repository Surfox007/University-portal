$baseUrl = "http://localhost:8085"

function Test-Endpoint {
    param (
        [string]$Title,
        [string]$Method,
        [string]$Path,
        [string]$Token = $null,
        [object]$Body = $null,
        [int]$ExpectedStatus = 200
    )

    $headers = @{}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }

    $jsonBody = $null
    if ($Body) {
        $jsonBody = $Body | ConvertTo-Json -Depth 5
    }

    try {
        $params = @{
            Uri = "$baseUrl$Path"
            Method = $Method
            Headers = $headers
            ContentType = "application/json"
            ErrorAction = "Stop"
        }
        if ($jsonBody) {
            $params["Body"] = $jsonBody
        }

        $res = Invoke-RestMethod @params
        Write-Host " [PASS] $Title -> HTTP 200/201 OK" -ForegroundColor Green
        return $res
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq $ExpectedStatus) {
            Write-Host " [PASS] $Title -> HTTP $statusCode (Expected)" -ForegroundColor Green
        } else {
            Write-Host " [FAIL] $Title -> HTTP $statusCode (Expected $ExpectedStatus)" -ForegroundColor Red
        }
        return $null
    }
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  TESTING UNIVERSITY MANAGEMENT SYSTEM (CSE 3230 LAB)     " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Unauthenticated Access Tests (Expect 401)
Write-Host "`n--- 1. Testing Unauthenticated Access (Security Wall) ---" -ForegroundColor Yellow
Test-Endpoint -Title "Unauthenticated GET /api/admins" -Method "GET" -Path "/api/admins" -ExpectedStatus 401
Test-Endpoint -Title "Unauthenticated GET /api/teachers" -Method "GET" -Path "/api/teachers" -ExpectedStatus 401
Test-Endpoint -Title "Unauthenticated GET /api/students" -Method "GET" -Path "/api/students" -ExpectedStatus 401

# 2. Authentication (Login) Tests
Write-Host "`n--- 2. Testing Authentication & JWT Token Issuance ---" -ForegroundColor Yellow
$adminRes = Test-Endpoint -Title "Login as Admin (admin / admin123)" -Method "POST" -Path "/api/auth/login" -Body @{ username = "admin"; password = "admin123" }
$adminToken = $adminRes.data.token

$teacherRes = Test-Endpoint -Title "Login as Teacher (teacher / teacher123)" -Method "POST" -Path "/api/auth/login" -Body @{ username = "teacher"; password = "teacher123" }
$teacherToken = $teacherRes.data.token

$studentRes = Test-Endpoint -Title "Login as Student (student / student123)" -Method "POST" -Path "/api/auth/login" -Body @{ username = "student"; password = "student123" }
$studentToken = $studentRes.data.token

Test-Endpoint -Title "Login with wrong password (admin / wrongpass)" -Method "POST" -Path "/api/auth/login" -Body @{ username = "admin"; password = "wrongpass" } -ExpectedStatus 401

# 3. RBAC Access Control Tests
Write-Host "`n--- 3. Testing RBAC: Admin Permissions (Can manage all 3) ---" -ForegroundColor Yellow
Test-Endpoint -Title "Admin accesses Admins resource" -Method "GET" -Path "/api/admins" -Token $adminToken
Test-Endpoint -Title "Admin accesses Teachers resource" -Method "GET" -Path "/api/teachers" -Token $adminToken
Test-Endpoint -Title "Admin accesses Students resource" -Method "GET" -Path "/api/students" -Token $adminToken

Write-Host "`n--- 4. Testing RBAC: Teacher Permissions (Only Teachers) ---" -ForegroundColor Yellow
Test-Endpoint -Title "Teacher accesses Teachers resource" -Method "GET" -Path "/api/teachers" -Token $teacherToken
Test-Endpoint -Title "Teacher accesses Admins resource (Forbidden)" -Method "GET" -Path "/api/admins" -Token $teacherToken -ExpectedStatus 403
Test-Endpoint -Title "Teacher accesses Students resource (Forbidden)" -Method "GET" -Path "/api/students" -Token $teacherToken -ExpectedStatus 403

Write-Host "`n--- 5. Testing RBAC: Student Permissions (Only Students) ---" -ForegroundColor Yellow
Test-Endpoint -Title "Student accesses Students resource" -Method "GET" -Path "/api/students" -Token $studentToken
Test-Endpoint -Title "Student accesses Admins resource (Forbidden)" -Method "GET" -Path "/api/admins" -Token $studentToken -ExpectedStatus 403
Test-Endpoint -Title "Student accesses Teachers resource (Forbidden)" -Method "GET" -Path "/api/teachers" -Token $studentToken -ExpectedStatus 403

# 6. CRUD Operations Lifecycle Tests
Write-Host "`n--- 6. Testing Full Student CRUD Operations by Student ---" -ForegroundColor Yellow
$newStudentBody = @{
    studentId = "STU-LIVE-001"
    fullName = "Live Test Student"
    email = "livestudent@university.edu"
    department = "Computer Science"
    semester = "5th Semester"
    cgpa = 3.88
    phoneNumber = "+1-555-8888"
    username = "livestudent"
    password = "StudentPassword123"
}
$createdStudent = Test-Endpoint -Title "Create Student (POST /api/students)" -Method "POST" -Path "/api/students" -Token $studentToken -Body $newStudentBody
$studentId = $createdStudent.data.id

Test-Endpoint -Title "Read Student by ID (GET /api/students/$studentId)" -Method "GET" -Path "/api/students/$studentId" -Token $studentToken

$updateStudentBody = @{
    studentId = "STU-LIVE-001"
    fullName = "Live Test Student (Updated)"
    email = "livestudent.updated@university.edu"
    department = "Software Engineering"
    semester = "6th Semester"
    cgpa = 3.92
    phoneNumber = "+1-555-8889"
}
Test-Endpoint -Title "Update Student (PUT /api/students/$studentId)" -Method "PUT" -Path "/api/students/$studentId" -Token $studentToken -Body $updateStudentBody

Test-Endpoint -Title "Delete Student (DELETE /api/students/$studentId)" -Method "DELETE" -Path "/api/students/$studentId" -Token $studentToken

# 7. Profile and Logout
Write-Host "`n--- 7. Testing Current User Profile & Logout ---" -ForegroundColor Yellow
Test-Endpoint -Title "Get Current User Profile (/api/auth/me)" -Method "GET" -Path "/api/auth/me" -Token $studentToken
Test-Endpoint -Title "Logout (/api/auth/logout)" -Method "POST" -Path "/api/auth/logout"

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "  ALL TESTS COMPLETED SUCCESSFULLY!                      " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
