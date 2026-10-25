Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  Starting University Management System Web Application on Port 8085   " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Application URL: http://localhost:8085" -ForegroundColor Green
Write-Host "H2 Console URL:  http://localhost:8085/h2-console" -ForegroundColor Green
Write-Host ""

java -jar target\university-portal-1.0.0.jar
