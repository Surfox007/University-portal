$baseUrl = "http://localhost:8085"

Write-Host "`n--- 1. Student Post Comment on Notice ---" -ForegroundColor Cyan
$loginRes = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body @{ username = "student"; password = "student123" } -SessionVariable stuSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue

$commentRes = Invoke-WebRequest -Uri "$baseUrl/student/notices/1/comment" -Method Post -Body @{ content = "Thank you Professor! What about lab assessments?" } -WebSession $stuSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Host "Student comment redirected to: $($commentRes.Headers.Location)"

$stuDash = Invoke-WebRequest -Uri "$baseUrl/student/dashboard" -WebSession $stuSession -UseBasicParsing
if ($stuDash.Content -like "*Thank you Professor! What about lab assessments?*") {
    Write-Host "[PASS] Student comment successfully displayed on notice thread!" -ForegroundColor Green
} else {
    Write-Host "[FAIL] Student comment not found" -ForegroundColor Red
}

Write-Host "`n--- 2. Teacher Post New Notice ---" -ForegroundColor Cyan
$teachLogin = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body @{ username = "teacher"; password = "teacher123" } -SessionVariable teachSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue

$newNotice = Invoke-WebRequest -Uri "$baseUrl/teacher/notices/create" -Method Post -Body @{
    title = "Office Hours This Week";
    category = "Academic";
    content = "I will be available in Room 402 on Wednesday from 2 to 4 PM for course consultations."
} -WebSession $teachSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Host "Teacher notice redirected to: $($newNotice.Headers.Location)"

$stuDash2 = Invoke-WebRequest -Uri "$baseUrl/student/dashboard" -WebSession $stuSession -UseBasicParsing
if ($stuDash2.Content -like "*Office Hours This Week*") {
    Write-Host "[PASS] Teacher notice is immediately visible to students!" -ForegroundColor Green
} else {
    Write-Host "[FAIL] Notice not visible to students" -ForegroundColor Red
}

Write-Host "`n--- 3. Admin View Notices Tab ---" -ForegroundColor Cyan
$adminLogin = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body @{ username = "admin"; password = "admin123" } -SessionVariable adminSession -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
$adminNotices = Invoke-WebRequest -Uri "$baseUrl/admin/dashboard?tab=notices" -WebSession $adminSession -UseBasicParsing
if ($adminNotices.Content -like "*Office Hours This Week*" -and $adminNotices.Content -like "*Delete Notice*") {
    Write-Host "[PASS] Admin can view all campus notices with full moderation/delete controls!" -ForegroundColor Green
} else {
    Write-Host "[FAIL] Admin notices tab issue" -ForegroundColor Red
}
