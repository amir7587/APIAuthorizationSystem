package com.login.tests;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;

import com.log.dto.UserDTO;
import com.login.service.UserService;

public class UserTest {

    @Test
    public void testAdd() {
        // Create a sample user object
        UserDTO user = new UserDTO("Admin", "testuser", "testpassword", "test@example.com", "Test", "User",
                                    "1234567890", "123 Main St", "Anytown", "AnyState", "AnyCountry", "12345");
        
        // Call the add method of UserService
        UserService userService = new UserService();
        int userId = userService.add(user);
        
        // Print the userId
        System.out.println("User ID after adding: " + userId);
    }

    @Test
    public void testSignin() {
        // Create a sample user object
        UserDTO user = new UserDTO("Admin", "testuser", "testpassword", "test@example.com", "Test", "User",
                                    "1234567890", "123 Main St", "Anytown", "AnyState", "AnyCountry", "12345");
        
        // Call the signin method of UserService
        UserService userService = new UserService();
        JSONObject jsonResponse = userService.signin(user);
        
        // Print the JSON response
        System.out.println("JSON Response after signing in: " + jsonResponse);
    }

}
