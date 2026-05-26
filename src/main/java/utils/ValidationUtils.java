package utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern ISBN_PATTERN = Pattern.compile("^(?:97[89])?\\d{9}[\\dX]$");

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidISBN(String isbn) {
        if (isbn == null) return false;
        // Strip hyphens and spaces
        String cleanIsbn = isbn.replace("-", "").replace(" ", "");
        return ISBN_PATTERN.matcher(cleanIsbn).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        // Password should be at least 6 characters
        return password.trim().length() >= 6;
    }
}
