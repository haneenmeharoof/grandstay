# GrandStay Hotel Management System - Unit Test Runner
Write-Host "Compiling and executing Unit Tests..." -ForegroundColor Cyan

$m2 = "$env:USERPROFILE\.m2\repository"
$jars = @(
    "target\classes",
    "target\test-classes",
    "$m2\org\junit\jupiter\junit-jupiter-api\5.10.2\junit-jupiter-api-5.10.2.jar",
    "$m2\org\junit\platform\junit-platform-commons\1.10.2\junit-platform-commons-1.10.2.jar",
    "$m2\org\opentest4j\opentest4j\1.3.0\opentest4j-1.3.0.jar",
    "$m2\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar",
    "$m2\org\slf4j\slf4j-api\2.0.12\slf4j-api-2.0.12.jar"
)

$cp = $jars -join ";"
java -cp $cp hotel.TestRunner
