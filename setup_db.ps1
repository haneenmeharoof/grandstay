# GrandStay Hotel Management System - Database Setup Script
Param(
    [string]$MySqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    [string]$User = "root",
    [string]$Password = "root"
)

Write-Host "Setting up GrandStay Hotel Management Database..." -ForegroundColor Cyan

if (-not (Test-Path $MySqlPath)) {
    Write-Host "mysql.exe not found at $MySqlPath. Please pass the correct path with -MySqlPath." -ForegroundColor Yellow
} else {
    Write-Host "Executing schema.sql..."
    & $MySqlPath -u $User -p$Password -e "source database/schema.sql"
    Write-Host "Executing seed.sql..."
    & $MySqlPath -u $User -p$Password -e "source database/seed.sql"
    Write-Host "Database hotel_management initialized and seeded successfully!" -ForegroundColor Green
}
