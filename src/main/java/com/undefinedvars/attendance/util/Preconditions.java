package com.undefinedvars.attendance.util;

import java.util.Objects;

/*
    Preconditions is a utility class that provides common validation methods
    to ensure that method arguments meet certain criteria before they are processed.
 */
public final class Preconditions {
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
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1 || atIndex != email.lastIndexOf('@')) {
            throw new IllegalArgumentException(fieldName + " must contain exactly one '@' with characters on both sides");
        }
        return email;
    }
}
