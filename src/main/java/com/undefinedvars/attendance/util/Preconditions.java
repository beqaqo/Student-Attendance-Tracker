package com.undefinedvars.attendance.util;

import java.util.Objects;
import java.util.regex.Pattern;

/*
    Preconditions is a utility class that provides common validation methods
    to ensure that method arguments meet certain criteria before they are processed.
 */
public final class Preconditions {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private Preconditions() {} // not instantiable

    public static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value;
    }

    public static String requireValidEmail(String email, String fieldName) {
        requireNonBlank(email, fieldName);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(fieldName + " must be a valid email address");
        }
        return email;
    }
}
