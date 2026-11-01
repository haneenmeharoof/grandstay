# GrandStay Hotel Management System — Testing Documentation

## 1. Test Strategy & Architecture
The testing suite verifies all core domain business rules, pricing logic, financial precision, date validation, and state machine transitions using **JUnit 5**.

Tests are completely isolated from production database dependencies, ensuring fast, deterministic, reproducible execution.

---

## 2. Test Suites Summary

| Test Suite | Package | Tests | Objective |
| :--- | :--- | :--- | :--- |
| **`DateUtilTest`** | `hotel.util` | 2 | Verifies nights calculation and mathematical date-range overlap rules |
| **`PasswordUtilTest`** | `hotel.util` | 2 | Verifies BCrypt 60-character hash generation, positive match, and negative match |
| **`CurrencyUtilTest`** | `hotel.util` | 2 | Verifies 100% `BigDecimal` calculations, `HALF_UP` rounding, taxes, and discounts |
| **`InputValidatorTest`** | `hotel.validation` | 5 | Verifies past date rejection, date sequence, room capacity bounds, and negative inventory prevention |
| **`ReservationCalculationTest`** | `hotel.service` | 1 | Verifies complete stay breakdown: rate × nights + 10% tax + service fee - discount |
| **`ModelLogicTest`** | `hotel.model` | 3 | Verifies reservation balance calculation, fully-paid status, cancellation eligibility, and room bookability |

**Total Unit Tests Executed**: 15  
**Tests Passed**: 15 (100%)  
**Tests Failed**: 0  

---

## 3. How to Run Tests
Execute the test runner script from PowerShell:
```powershell
.\test.ps1
```
Or run directly with Java:
```powershell
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
```

---

## 4. Test Execution Output
```
=======================================================================
           GRANDSTAY HOTEL MANAGEMENT SYSTEM - UNIT TEST SUITE         
=======================================================================

[RUNNING SUITE] DateUtilTest
  [PASS] ✓ Should correctly identify overlapping date ranges
  [PASS] ✓ Should correctly calculate number of nights between check-in and check-out

[RUNNING SUITE] PasswordUtilTest
  [PASS] ✓ Should hash password with BCrypt and verify successfully
  [PASS] ✓ Should reject null or empty password inputs

[RUNNING SUITE] CurrencyUtilTest
  [PASS] ✓ Should calculate exact taxes and totals with discounts using BigDecimal
  [PASS] ✓ Should strictly round monetary values to 2 decimal places using HALF_UP

[RUNNING SUITE] InputValidatorTest
  [PASS] ✓ Should reject check-in dates in the past
  [PASS] ✓ Should accept valid forward booking dates
  [PASS] ✓ Should reject guest capacity exceeding room limit
  [PASS] ✓ Should reject check-out dates on or before check-in date
  [PASS] ✓ Should reject inventory deductions that would cause negative stock

[RUNNING SUITE] ReservationCalculationTest
  [PASS] ✓ Should accurately calculate breakdown of nightly rates, taxes, fees, and total

[RUNNING SUITE] ModelLogicTest
  [PASS] ✓ Room bookability status
  [PASS] ✓ Reservation balance due and payment status logic
  [PASS] ✓ Reservation cancellation eligibility by status

=======================================================================
                           TEST SUMMARY                                
=======================================================================
Total Tests Executed: 15
Tests Passed:        15
Tests Failed:        0
Elapsed Time:        1236 ms
Status:              SUCCESS (ALL TESTS PASSED)
=======================================================================
```
