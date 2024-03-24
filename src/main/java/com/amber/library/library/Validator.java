package com.amber.library.library;

public class Validator {

    // Validates title for alphanumeric characters only
    public static String validateTitleAlphanumeric(String title) {
        if (!title.matches("[a-zA-Z0-9 ]+")) { // Regular expression for alphanumeric characters and spaces
            return "Title must contain only letters, numbers, and spaces.";
        }
        return null;
    }

    // Validates that the input is not empty
    public static String validateNotEmpty(String input, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            return fieldName + " cannot be empty.";
        }
        return null;
    }

    // Validates ISBN format
    public static String validateISBN(String isbn) {
        if (!isbn.matches("[0-9]{13}|[0-9]{10}")) {
            return "Invalid ISBN format. ISBN must be 10 or 13 digits.";
        }
        return null;
    }

    // Validates Dewey Decimal
    public static String validateDewey(String dewey) {
        if (!dewey.matches("\\d+(\\.\\d+)?")) {
            return "Invalid Dewey Decimal format.";
        }
        return null;
    }

    // Add more validation methods as needed
}
