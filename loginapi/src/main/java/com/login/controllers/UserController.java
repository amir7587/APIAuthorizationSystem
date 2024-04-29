package com.login.controllers;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.login.dto.UserDTO;
import com.login.service.UserService;
import com.login.utils.SessionValidator;
import com.login.utils.Validator;

@Controller
public class UserController {

    // Method to sign in
    @CrossOrigin(origins = "*")
    @ResponseBody
    @RequestMapping(value = "/signin", method = RequestMethod.POST)
    public ResponseEntity<String> signin(final @RequestBody String credentials, HttpServletRequest httpRequest) {
        try {
            // Parse incoming JSON object
            JSONObject inputJson = new JSONObject(credentials);
            Validator valObj = new Validator();
            // Validate credentials
            String checkData = valObj.validateCredentials(inputJson);
            if (checkData.isEmpty()) {
                // Create UserDTO object
                UserDTO dto = new UserDTO();
                dto.setUsername(inputJson.getString("username"));
                dto.setPassword(inputJson.getString("password"));

                // Call UserService to sign in
                UserService userService = new UserService();
                JSONObject outputJson = userService.signin(dto);

                return new ResponseEntity<String>(outputJson.toString(), HttpStatus.OK);
            } else {
                // Return error response if credentials are invalid
                return new ResponseEntity<String>(checkData, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Return internal server error if an exception occurs
        return new ResponseEntity<String>("", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // ListIngredient is a public API to list ingredients accessible by anyone
    // This method does not require authentication
    @CrossOrigin(origins = "*")
    @ResponseBody
    @RequestMapping(value = "/ingredients", method = RequestMethod.GET)
    public ResponseEntity<String> listIngredients(HttpServletRequest httpRequest) {
        JSONObject inputJson = null;
        JSONObject jsonResponse = new JSONObject();
        int tokenUserId = 0;
        try {
            // Check if token is provided in request header
            if (httpRequest.getHeader("TOKEN") != null) {
                // Validate token
                tokenUserId = SessionValidator.verify(httpRequest.getHeader("TOKEN"));
                if (tokenUserId <= 0) {
                    // Return error response if token is not valid
                    jsonResponse.put("error", "TOKEN_NOT_VALID");
                    return new ResponseEntity<String>(jsonResponse.toString(), HttpStatus.BAD_REQUEST);
                } else {
                    // Call UserService to get list of ingredients
                    UserService obj = new UserService();
                    JSONObject outputJson = obj.listIngredients();
                    return new ResponseEntity<String>(outputJson.toString(), HttpStatus.OK);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Return internal server error if an exception occurs
        return new ResponseEntity<String>("", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Activate or deactivate user. This is a private API accessible only to users with update permission
    @CrossOrigin(origins = "*")
    @ResponseBody
    @RequestMapping(value = "/userstatus/{userId}/{status}", method = RequestMethod.POST)
    public ResponseEntity<String> updateUserStatus(final @PathVariable int userId, final @PathVariable String status,
            HttpServletRequest httpRequest) {
        JSONObject inputJson = null;
        JSONObject jsonResponse = new JSONObject();
        int tokenUserId = 0;
        int stat = 0;
        try {
            if (httpRequest.getHeader("TOKEN") != null) {
                tokenUserId = SessionValidator.verify(httpRequest.getHeader("TOKEN"));
                if (tokenUserId <= 0) {
                    jsonResponse.put("error", "TOKEN_NOT_VALID");
                    return new ResponseEntity<String>(jsonResponse.toString(), HttpStatus.BAD_REQUEST);
                } else {
                    // Determine status
                    if (status.equalsIgnoreCase("active")) {
                        stat = 1;
                    } else {
                        stat = 0;
                    }
                    // Call UserService to update user status
                    UserService obj = new UserService();
                    JSONObject permit = obj.getUserRolesAndPermissions(tokenUserId);
                    String permission = permit.getString("permissions");
                    boolean hasUpdatePermission = permission.contains("Update");

                    if (hasUpdatePermission) {
                        String output = obj.updateUserStatus(userId, stat);
                        return new ResponseEntity<String>(output, HttpStatus.OK);
                    } else {
                        // Return error response if user does not have update permission
                        return new ResponseEntity<String>(
                                "You Don't have permission to Update User Status Please contact Admin", HttpStatus.OK);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Return internal server error if an exception occurs
        return new ResponseEntity<String>("", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Update Ingredients. This is a private API accessible only to users with update permission
    @CrossOrigin(origins = "*")
    @ResponseBody
    @RequestMapping(value = "/updateingredients", method = RequestMethod.POST)
    public ResponseEntity<String> updateIngredients(final @RequestBody String input, HttpServletRequest httpRequest) {
        JSONObject inputJson = null;
        JSONObject jsonResponse = new JSONObject();
        int tokenUserId = 0;
        try {
            if (httpRequest.getHeader("TOKEN") != null) {
                tokenUserId = SessionValidator.verify(httpRequest.getHeader("TOKEN"));
                if (tokenUserId <= 0) {
                    jsonResponse.put("error", "TOKEN_NOT_VALID");
                    return new ResponseEntity<String>(jsonResponse.toString(), HttpStatus.BAD_REQUEST);
                } else {
                    // Parse incoming JSON object
                    inputJson = new JSONObject(input);
                    // Call UserService to update ingredients
                    UserService obj = new UserService();
                    JSONObject permit = obj.getUserRolesAndPermissions(tokenUserId);
                    String permission = permit.getString("permissions");
                    boolean hasUpdatePermission = permission.contains("Update");
                    if (hasUpdatePermission) {
                        JSONObject outputJson = obj.updateIngredient(inputJson);
                        return new ResponseEntity<String>(outputJson.toString(), HttpStatus.OK);
                    } else {
                        // Return error response if user does not have update permission
                        return new ResponseEntity<String>(
                                "You Don't have permission to Update User Status Please contact Admin", HttpStatus.OK);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Return internal server error if an exception occurs
        return new ResponseEntity<String>("", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Register User. This is a private API accessible only to admins or users with create permission
    @CrossOrigin(origins = "*")
    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<String> createUser(final @RequestBody String userInput, HttpServletRequest httpRequest) {
        JSONObject inputJson = null;
        JSONObject jsonResponse = new JSONObject();
        int tokenUserId = 0;
        try {
            if (httpRequest.getHeader("TOKEN") != null) {
                tokenUserId = SessionValidator.verify(httpRequest.getHeader("TOKEN"));
                if (tokenUserId <= 0) {
                    jsonResponse.put("error", "TOKEN_NOT_VALID");
                    return new ResponseEntity<String>(jsonResponse.toString(), HttpStatus.BAD_REQUEST);
                } else {
                    // Parse incoming JSON object
                    inputJson = new JSONObject(userInput);
                    // Create UserDTO object
                    UserDTO dto = new UserDTO();
                    dto.setUsername(inputJson.getString("username"));
                    dto.setPassword(inputJson.getString("password"));
                    dto.setEmail(inputJson.getString("email"));
                    dto.setFirstName(inputJson.getString("firstName"));
                    dto.setLastName(inputJson.getString("lastName"));
                    dto.setContactNumber(inputJson.getString("contactNumber"));
                    dto.setStreetAddress(inputJson.getString("streetAddress"));
                    dto.setCity(inputJson.getString("city"));
                    dto.setState(inputJson.getString("state"));
                    dto.setCountry(inputJson.getString("country"));
                    dto.setPostalCode(inputJson.getString("postalCode"));
                    dto.setRoleName(inputJson.getString("userType"));

                    // Call UserService to add a new user
                    UserService obj = new UserService();
                    JSONObject permit = obj.getUserRolesAndPermissions(tokenUserId);
                    String permission = permit.getString("permissions");
                    boolean hasCreatePermission = permission.contains("Create");

                    if (hasCreatePermission) {
                        Validator valObj = new Validator();
                        String checkData = valObj.validate(inputJson);
                        if (checkData.isEmpty()) {
                            int userId = obj.add(dto);
                            if (userId > 0) {
                                return new ResponseEntity<String>("User Added Successfully", HttpStatus.OK);
                            } else if (userId == -1) {
                                return new ResponseEntity<String>("User Already Exist", HttpStatus.OK);
                            } else if (userId == 0) {
                                return new ResponseEntity<String>("Failed to add User", HttpStatus.OK);
                            }
                        } else {
                            // Return error response if input data is not valid
                            return new ResponseEntity<String>(checkData, HttpStatus.BAD_REQUEST);
                        }
                    } else {
                        // Return error response if user does not have create permission
                        return new ResponseEntity<String>(
                                "You Don't have permission to Create User Please contact Admin", HttpStatus.OK);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Return internal server error if an exception occurs
        return new ResponseEntity<String>("", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
