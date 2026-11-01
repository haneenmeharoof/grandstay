package hotel.util;

public class DemoPasswordGenerator {
    public static void main(String[] args) {
        System.out.println("ADMIN=" + PasswordUtil.hashPassword("Admin@123"));
        System.out.println("RECEPTION=" + PasswordUtil.hashPassword("Reception@123"));
        System.out.println("HOUSEKEEPER=" + PasswordUtil.hashPassword("Housekeeping@123"));
        System.out.println("CUSTOMER=" + PasswordUtil.hashPassword("Customer@123"));
    }
}