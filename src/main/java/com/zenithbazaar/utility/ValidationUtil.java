package com.zenithbazaar.utility;

import com.zenithbazaar.exception.ValidationException;
import java.math.BigDecimal;
import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private ValidationUtil() {}

    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
    }

    public static void validateEmail(String email) {
        requireNotBlank(email, "Email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    public static void validateMinLength(String value, int minLen, String fieldName) {
        requireNotBlank(value, fieldName);
        if (value.trim().length() < minLen) {
            throw new ValidationException(fieldName + " must be at least " + minLen + " characters");
        }
    }

    public static void validatePositivePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Price must be greater than or equal to 0.00");
        }
    }

    public static void validateNonNegativeQuantity(Integer qty) {
        if (qty == null || qty < 0) {
            throw new ValidationException("Quantity cannot be negative");
        }
    }

    public static void validatePositiveQuantity(Integer qty) {
        if (qty == null || qty <= 0) {
            throw new ValidationException("Quantity must be greater than zero");
        }
    }

    public static void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5");
        }
    }
}
