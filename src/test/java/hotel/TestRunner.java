package hotel;

import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Direct Test Runner for executing JUnit 5 test classes.
 * Provides transparent, colorized terminal reporting.
 */
public class TestRunner {

    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("           GRANDSTAY HOTEL MANAGEMENT SYSTEM - UNIT TEST SUITE         ");
        System.out.println("=======================================================================");

        List<Class<?>> testClasses = List.of(
                hotel.util.DateUtilTest.class,
                hotel.util.PasswordUtilTest.class,
                hotel.util.CurrencyUtilTest.class,
                hotel.validation.InputValidatorTest.class,
                hotel.service.ReservationCalculationTest.class,
                hotel.model.ModelLogicTest.class
        );

        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        long startTime = System.currentTimeMillis();

        for (Class<?> testClass : testClasses) {
            System.out.println("\n[RUNNING SUITE] " + testClass.getSimpleName());
            Object instance;
            try {
                instance = testClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                System.err.println("  Failed to instantiate " + testClass.getName() + ": " + e.getMessage());
                continue;
            }

            for (Method method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    totalTests++;
                    String testName = method.getName();
                    if (method.isAnnotationPresent(DisplayName.class)) {
                        testName = method.getAnnotation(DisplayName.class).value();
                    }

                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                        passedTests++;
                        System.out.println("  [PASS] ✓ " + testName);
                    } catch (Exception ex) {
                        failedTests++;
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        System.err.println("  [FAIL] ✗ " + testName + " -> " + cause.getMessage());
                    }
                }
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("\n=======================================================================");
        System.out.println("                           TEST SUMMARY                                ");
        System.out.println("=======================================================================");
        System.out.println("Total Tests Executed: " + totalTests);
        System.out.println("Tests Passed:        " + passedTests);
        System.out.println("Tests Failed:        " + failedTests);
        System.out.println("Elapsed Time:        " + elapsed + " ms");
        System.out.println("Status:              " + (failedTests == 0 ? "SUCCESS (ALL TESTS PASSED)" : "FAILURE"));
        System.out.println("=======================================================================");

        if (failedTests > 0) {
            System.exit(1);
        }
    }
}
