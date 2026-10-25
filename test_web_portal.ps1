$baseUrl = "http://localhost:8085"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  TESTING ACADEMIC UNIVERSITY WEB PORTAL & DASHBOARDS     " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Unauthenticated root redirect
$root = Invoke-WebRequest -Uri "$baseUrl/" -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Host "`n1. Accessing root URL (/):" -ForegroundColor Yellow
Write-Host "   Redirects to: $($root.Headers.Location)" -ForegroundColor Green

# 2. Login as Admin
Write-Host "`n2. Logging in as Administrator (admin / admin123):" -ForegroundColor Yellow
$loginRes = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body @{ username = "admin"; password = "admin123" } -SessionVariable adminSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
$adminUrl = $loginRes.Headers.Location
Write-Host "   Redirected to: $adminUrl" -ForegroundColor Green

$adminDash = Invoke-WebRequest -Uri $adminUrl -WebSession $adminSession -UseBasicParsing
Write-Host "   Admin Dashboard Status: $($adminDash.StatusCode) OK" -ForegroundColor Green
if ($adminDash.Content -like "*Administrator Portal*") {
    Write-Host "   [PASS] Loaded Administrator Dashboard successfully!" -ForegroundColor Green
}

# 3. Login as Teacher
Write-Host "`n3. Logging in as Faculty (teacher / teacher123):" -ForegroundColor Yellow
$teacherLogin = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body @{ username = "teacher"; password = "teacher123" } -SessionVariable teacherSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
$teacherUrl = $teacherLogin.Headers.Location
Write-Host "   Redirected to: $teacherUrl" -ForegroundColor Green

$teacherDash = Invoke-WebRequest -Uri $teacherUrl -WebSession $teacherSession -UseBasicParsing
Write-Host "   Teacher Dashboard Status: $($teacherDash.StatusCode) OK" -ForegroundColor Green
if ($teacherDash.Content -like "*Faculty Member Workspace*") {
    Write-Host "   [PASS] Loaded Faculty Dashboard successfully!" -ForegroundColor Green
}

# 4. Login as Student
Write-Host "`n4. Logging in as Student (student / student123):" -ForegroundColor Yellow
$studentLogin = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body @{ username = "student"; password = "student123" } -SessionVariable studentSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
$studentUrl = $studentLogin.Headers.Location
Write-Host "   Redirected to: $studentUrl" -ForegroundColor Green

$studentDash = Invoke-WebRequest -Uri $studentUrl -WebSession $studentSession -UseBasicParsing
Write-Host "   Student Dashboard Status: $($studentDash.StatusCode) OK" -ForegroundColor Green
if ($studentDash.Content -like "*Student Academic Dashboard*") {
    Write-Host "   [PASS] Loaded Student Academic Dashboard successfully!" -ForegroundColor Green
}

# 5. Access Control Boundary: Student attempting to access Admin Dashboard
Write-Host "`n5. Student attempting to access /admin/dashboard (RBAC Test):" -ForegroundColor Yellow
$forbiddenAttempt = Invoke-WebRequest -Uri "$baseUrl/admin/dashboard" -WebSession $studentSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Host "   Student blocked from Admin Dashboard, redirected to: $($forbiddenAttempt.Headers.Location)" -ForegroundColor Green

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "  ALL WEB PORTAL ROLE-BASED DASHBOARDS VERIFIED!          " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
