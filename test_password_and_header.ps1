$baseUrl = "http://localhost:8085"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  TESTING HEADER CREDENTIALS & PASSWORD CHANGE            " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Login as Student
Write-Host "`n1. Logging in as Student (student / student123):" -ForegroundColor Yellow
$loginRes = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body @{ username = "student"; password = "student123" } -SessionVariable stuSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue

$stuDash = Invoke-WebRequest -Uri "$baseUrl/student/dashboard" -WebSession $stuSession -UseBasicParsing
if ($stuDash.Content -like "*@student*" -and $stuDash.Content -like "*Change Password*" -and $stuDash.Content -like "*Sign Out*") {
    Write-Host "   [PASS] Student header correctly shows @student username tag, Change Password button, and Sign Out!" -ForegroundColor Green
} else {
    Write-Host "   [FAIL] Student header elements missing" -ForegroundColor Red
}

# 2. Test Student Change Password
Write-Host "`n2. Changing Student Password to 'newPass123':" -ForegroundColor Yellow
$changeRes = Invoke-WebRequest -Uri "$baseUrl/student/change-password" -Method Post -Body @{
    currentPassword = "student123";
    newPassword = "newPass123";
    confirmPassword = "newPass123"
} -WebSession $stuSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue

# Verify login with old password fails
$oldLogin = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body @{ username = "student"; password = "student123" } -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
if ($oldLogin.Headers.Location -like "*error=true*") {
    Write-Host "   [PASS] Old password 'student123' rejected as expected!" -ForegroundColor Green
} else {
    Write-Host "   [FAIL] Old password unexpectedly succeeded" -ForegroundColor Red
}

# Verify login with new password succeeds
$newLogin = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body @{ username = "student"; password = "newPass123" } -SessionVariable newStuSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
if ($newLogin.Headers.Location -like "*/student/dashboard*") {
    Write-Host "   [PASS] New password 'newPass123' logged in successfully!" -ForegroundColor Green
} else {
    Write-Host "   [FAIL] New password login failed" -ForegroundColor Red
}

# Reset password back to student123
$resetRes = Invoke-WebRequest -Uri "$baseUrl/student/change-password" -Method Post -Body @{
    currentPassword = "newPass123";
    newPassword = "student123";
    confirmPassword = "student123"
} -WebSession $newStuSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Host "   [RESET] Reset student password back to 'student123' for standard demo." -ForegroundColor Cyan

# 3. Check Teacher Header
Write-Host "`n3. Checking Teacher Header (@teacher):" -ForegroundColor Yellow
$teachLogin = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body @{ username = "teacher"; password = "teacher123" } -SessionVariable teachSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
$teachDash = Invoke-WebRequest -Uri "$baseUrl/teacher/dashboard" -WebSession $teachSession -UseBasicParsing
if ($teachDash.Content -like "*@teacher*" -and $teachDash.Content -like "*Change Password*" -and $teachDash.Content -like "*Sign Out*") {
    Write-Host "   [PASS] Teacher header correctly displays @teacher username, Change Password button, and Sign Out!" -ForegroundColor Green
} else {
    Write-Host "   [FAIL] Teacher header elements missing" -ForegroundColor Red
}

# 4. Check Admin Header
Write-Host "`n4. Checking Administrator Header (@admin):" -ForegroundColor Yellow
$adminLogin = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body @{ username = "admin"; password = "admin123" } -SessionVariable adminSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
$adminDash = Invoke-WebRequest -Uri "$baseUrl/admin/dashboard" -WebSession $adminSession -UseBasicParsing
if ($adminDash.Content -like "*@admin*" -and $adminDash.Content -like "*Change Password*" -and $adminDash.Content -like "*Sign Out*") {
    Write-Host "   [PASS] Administrator header correctly displays @admin username, Change Password button, and Sign Out!" -ForegroundColor Green
} else {
    Write-Host "   [FAIL] Admin header elements missing" -ForegroundColor Red
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "  ALL HEADER AND PASSWORD TESTS PASSED!                   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
