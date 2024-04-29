package com.login.utils;

import org.json.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

    // Validate user registration data
    public static String validate(JSONObject userData) {
        StringBuilder errorMessage = new StringBuilder();

        // Check if any mandatory field is missing
        if (!userData.has("username") || userData.getString("username").isEmpty()) {
            errorMessage.append("Username is required.\n");
        }
        if (!userData.has("password") || userData.getString("password").isEmpty()) {
            errorMessage.append("Password is required.\n");
        }
        if (!userData.has("email") || userData.getString("email").isEmpty()) {
            errorMessage.append("Email is required.\n");
        }
        if (!userData.has("firstName") || userData.getString("firstName").isEmpty()) {
            errorMessage.append("First name is required.\n");
        }
        if (!userData.has("lastName") || userData.getString("lastName").isEmpty()) {
            errorMessage.append("Last name is required.\n");
        }
        if (!userData.has("contactNumber") || userData.getString("contactNumber").isEmpty()) {
            errorMessage.append("Contact number is required.\n");
        } else {
            String phoneNumber = userData.getString("contactNumber");
            // Validate phone number format (10 digits)
            if (!isValidPhoneNumber(phoneNumber)) {
                errorMessage.append("Invalid phone number format.\n");
            }
        }
        if (!userData.has("streetAddress") || userData.getString("streetAddress").isEmpty()) {
            errorMessage.append("Address is required.\n");
        }
        if (!userData.has("city") || userData.getString("city").isEmpty()) {
            errorMessage.append("City is required.\n");
        }
        if (!userData.has("state") || userData.getString("state").isEmpty()) {
            errorMessage.append("State is required.\n");
        }
        if (!userData.has("country") || userData.getString("country").isEmpty()) {
            errorMessage.append("Country is required.\n");
        }
        if (!userData.has("userType") || userData.getString("userType").isEmpty()) {
            errorMessage.append("User type is required.\n");
        } else {
            String role = userData.getString("userType");
            // Validate role (should be either Admin, User, or Guest)
            if (!isValidRole(role)) {
                errorMessage.append("Invalid user role. Allowed roles are: Admin, User, Guest.\n");
            }
        }

        // Return accumulated error messages
        return errorMessage.toString();
    }

    // Validate user credentials during sign-in
    public static String validateCredentials(JSONObject userData) {
        StringBuilder errorMessage = new StringBuilder();

        // Check if any mandatory field is missing
        if (!userData.has("username") || userData.getString("username").isEmpty()) {
            errorMessage.append("Username is required.\n");
        }
        if (!userData.has("password") || userData.getString("password").isEmpty()) {
            errorMessage.append("Password is required.\n");
        }

        // Return accumulated error messages
        return errorMessage.toString();
    }

    // Validate phone number format
    private static boolean isValidPhoneNumber(String phoneNumber) {
        // Regular expression for 10-digit phone number
        String regex = "\\d{10}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    // Validate user role
    private static boolean isValidRole(String role) {
        // Valid roles: Admin, User, Guest
        return role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("User") || role.equalsIgnoreCase("Guest");
    }

}
