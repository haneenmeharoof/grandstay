# GrandStay Hotel Management System - Launch Script
Write-Host "Starting GrandStay Hotel Management System..." -ForegroundColor Cyan

$m2 = "$env:USERPROFILE\.m2\repository"
$jars = @(
    "target\GrandStayHotelManagement.jar",
    "$m2\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar",
    "$m2\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar",
    "$m2\com\formdev\flatlaf\3.4.1\flatlaf-3.4.1.jar",
    "$m2\com\formdev\flatlaf-extras\3.4.1\flatlaf-extras-3.4.1.jar",
    "$m2\org\slf4j\slf4j-api\2.0.12\slf4j-api-2.0.12.jar"
)

$cp = $jars -join ";"
java -cp $cp hotel.Main
