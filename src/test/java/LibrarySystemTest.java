import org.junit.jupiter.api.Test;
import utils.PasswordHasher;
import utils.ValidationUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class LibrarySystemTest {

    @Test
    public void testPasswordHashing() {
        String plainPassword = "student123";
        String hashedPassword = PasswordHasher.hashPassword(plainPassword);
        
        assertNotNull(hashedPassword);
        assertNotEquals(plainPassword, hashedPassword);
        assertTrue(PasswordHasher.verifyPassword(plainPassword, hashedPassword));
        assertFalse(PasswordHasher.verifyPassword("wrongpass", hashedPassword));
    }

    @Test
    public void testValidationUtils() {
        // Test Email Validation
        assertTrue(ValidationUtils.isValidEmail("test@college.edu"));
        assertTrue(ValidationUtils.isValidEmail("john.doe+1@domain.org"));
        assertFalse(ValidationUtils.isValidEmail("invalid-email"));
        assertFalse(ValidationUtils.isValidEmail("test@"));

        // Test Contact Validation (10 digits)
        assertTrue(ValidationUtils.isValidPhone("9876543210"));
        assertFalse(ValidationUtils.isValidPhone("12345"));
        assertFalse(ValidationUtils.isValidPhone("98765432109")); // 11 digits
        assertFalse(ValidationUtils.isValidPhone("abcde12345"));

        // Test ISBN Validation
        assertTrue(ValidationUtils.isValidISBN("9780132350884")); // Clean Code ISBN-13
        assertTrue(ValidationUtils.isValidISBN("978-0-13-468599-1")); // Effective Java with hyphens
        assertTrue(ValidationUtils.isValidISBN("0132350882")); // ISBN-10
        assertFalse(ValidationUtils.isValidISBN("123")); // Invalid length
    }

    @Test
    public void testFineCalculationLogic() {
        // Fine Rate: ₹10 per day overdue
        double fineRate = 10.0;
        
        Date now = new Date();
        // Assume due date was 5 days ago
        long fiveDaysInMillis = 5L * 24 * 60 * 60 * 1000;
        Date dueDate = new Date(now.getTime() - fiveDaysInMillis);
        
        // Simulating book return today (5 days late)
        Date returnDate = now;
        
        assertTrue(returnDate.after(dueDate));
        
        long diffMs = returnDate.getTime() - dueDate.getTime();
        long overdueDays = diffMs / (1000 * 60 * 60 * 24);
        if (overdueDays == 0 && diffMs > 0) {
            overdueDays = 1;
        }
        
        assertEquals(5, overdueDays);
        
        double calculatedFine = overdueDays * fineRate;
        assertEquals(50.0, calculatedFine);
    }
}
