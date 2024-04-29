package com.login.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.log.dto.UserDTO;
import com.login.utils.SessionValidator;
import com.login.utils.Utilities;

public class UserService {
    Utilities util = new Utilities();

    // Get the role ID based on the role name
    private int getRole(String roleName, Connection conn) {
        int roleId = 0;
        String query = "SELECT id, name FROM Role WHERE name = ?";
        try {
            ResultSet rs = util.executeQuery(util.prepareStatement(query, conn, roleName), conn);
            if (rs.next()) {
                roleId = rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return roleId;
    }

    // Check if a user with the given username already exists
    private boolean isUserExist(String userName, Connection conn) {
        int userId = 0;
        boolean isExist = false;
        String query = "SELECT Id FROM user WHERE username=?";
        try {
            ResultSet rs = util.executeQuery(util.prepareStatement(query, conn, userName), conn);
            if (rs.next()) {
                userId = rs.getInt("Id");
                isExist = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isExist;
    }

    // Add a new user
    public int add(UserDTO user) {
        int userId = 0;
        int userRoleId = 0;
        Connection conn = null;
        int tokenId = 0;
        try {
            conn = util.connect();
            String query = "INSERT INTO User (username, password, email, first_name, last_name, contact_number, street_address, city, state, country, postal_code, created_date_time, created_by,status)"
                    + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            if (!isUserExist(user.getUsername(), conn)) {
                userId = util.executeUpdate(
                        util.prepareStatement(query, conn, user.getUsername(), user.getPassword(), user.getEmail(),
                                user.getFirstName(), user.getLastName(), user.getContactNumber(),
                                user.getStreetAddress(), user.getCity(), user.getState(), user.getCountry(),
                                user.getPostalCode(), util.getDateTime(), Integer.toString(1), Integer.toString(1)),
                        conn);

                // Assign a role to the user
                query = "INSERT INTO UserRole (user_id, role_id) VALUES(?,?)";
                userRoleId = util.executeUpdate(
                        util.prepareStatement(query, conn, userId, getRole(user.getRoleName(), conn)), conn);

                // Insert into user Token table
                final String token = UUID.randomUUID().toString().replace("-", "");
                query = "INSERT INTO Token (user_id,session_token) VALUES (?,?)";
                tokenId = util.executeUpdate(util.prepareStatement(query, conn, Integer.toString(userId), token),
                        conn);

            } else {
                userId = -1;
                return userId;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            util.disconnect(conn);
        }
        return userId;
    }

    // Update user status
    public String updateUserStatus(int userId, int newStatus) {
        Connection conn = null;
        try {
            conn = util.connect();
            String query = "UPDATE User SET status = ? WHERE id = ?";
            int rowsUpdated = util.executeUpdate(util.prepareStatement(query, conn, newStatus, userId), conn);
            if (rowsUpdated >= 0) {
                return "User status updated successfully.";
            } else {
                return "Failed to update user status.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } finally {
            util.disconnect(conn);
        }
    }

    // List all ingredients
    public JSONObject listIngredients() {
        Connection conn = null;
        JSONObject jsonResponse = new JSONObject();
        JSONArray ingredientArray = new JSONArray();
        try {
            conn = util.connect();
            String query = "SELECT id, name, total_unit, left_unit FROM Ingredient";
            ResultSet rs = util.executeQuery(util.prepareStatement(query, conn), conn);
            while (rs.next()) {
                JSONObject ingredientObject = new JSONObject();
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double totalUnit = rs.getInt("total_unit");
                double leftUnit = rs.getInt("left_unit");
                ingredientObject.put("id", id);
                ingredientObject.put("name", name);
                ingredientObject.put("total_unit", totalUnit);
                ingredientObject.put("left_unit", leftUnit);
                ingredientArray.put(ingredientObject);
            }
            jsonResponse.put("ingredients", ingredientArray);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            util.disconnect(conn);
        }
        return jsonResponse;
    }

    // Update ingredient details
    public JSONObject updateIngredient(JSONObject ingredientData) {
        Connection conn = null;
        JSONObject jsonResponse = new JSONObject();
        try {
            conn = util.connect();
            int id = ingredientData.getInt("id");
            int totalUnit = ingredientData.getInt("totalUnit");
            int leftUnit = ingredientData.getInt("leftUnit");
            String query = "UPDATE Ingredient SET total_unit = ?, left_unit = ? WHERE id = ?";
            util.executeUpdate(util.prepareStatement(query, conn, totalUnit, leftUnit, id), conn);
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Ingredient with ID " + id + " updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Failed to update ingredient. Please check the input data.");
        } finally {
            util.disconnect(conn);
        }
        return jsonResponse;
    }

    // Sign in user
    public JSONObject signin(UserDTO user) {
        JSONObject jsonResponse = new JSONObject();
        String query = "SELECT T.session_token AS token, U.username, U.first_name AS firstName, U.last_name AS lastName, "
                + "    U.email, U.contact_number AS contactNumber, U.street_address AS streetAddress, U.city, U.state, "
                + "    U.country, U.postal_code AS postalCode, R.name AS userRole, "
                + "    GROUP_CONCAT(P.name) AS permissions "
                + "FROM User U INNER JOIN Token T ON U.id = T.user_id INNER JOIN UserRole UR ON U.id = UR.user_id "
                + "INNER JOIN Role R ON UR.role_id = R.id " + "INNER JOIN RolePermission RP ON R.id = RP.role_id "
                + "INNER JOIN Permission P ON RP.permission_id = P.id " + "WHERE U.username = ? AND U.password = ? "
                + "GROUP BY T.session_token, U.username, U.first_name, U.last_name, U.email, U.contact_number, "
                + "U.street_address, U.city, U.state, U.country, U.postal_code, R.name;";
        try {
            Connection conn = util.connect();
            ResultSet rs = util.executeQuery(util.prepareStatement(query, conn, user.getUsername(), user.getPassword()),
                    conn);
            if (rs.next()) {
                jsonResponse.put("token", rs.getString("token"));
                jsonResponse.put("username", rs.getString("username"));
                jsonResponse.put("firstName", rs.getString("firstName"));
                jsonResponse.put("lastName", rs.getString("lastName"));
                jsonResponse.put("email", rs.getString("email"));
                jsonResponse.put("contactNumber", rs.getString("contactNumber"));
                jsonResponse.put("streetAddress", rs.getString("streetAddress"));
                jsonResponse.put("city", rs.getString("city"));
                jsonResponse.put("state", rs.getString("state"));
                jsonResponse.put("country", rs.getString("country"));
                jsonResponse.put("postalCode", rs.getString("postalCode"));
                jsonResponse.put("userRole", rs.getString("userRole"));
                jsonResponse.put("permissions", rs.getString("permissions"));
            } else {
                // Credentials are incorrect, return an error message
                jsonResponse.put("error", "Invalid username or password");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonResponse;
    }

    // Get user roles and permissions
    public JSONObject getUserRolesAndPermissions(int userId) {
        Connection conn = null;
        JSONObject jsonResponse = new JSONObject();
        try {
            conn = util.connect();
            String query = "SELECT r.name AS role_name, GROUP_CONCAT(p.name) AS permissions " + "FROM UserRole ur "
                    + "INNER JOIN Role r ON ur.role_id = r.id " + "INNER JOIN RolePermission rp ON r.id = rp.role_id "
                    + "INNER JOIN Permission p ON rp.permission_id = p.id " + "WHERE ur.user_id = ? "
                    + "GROUP BY r.name";
            ResultSet rs = util.executeQuery(util.prepareStatement(query, conn, userId), conn);
            if (rs.next()) {
                String roleName = rs.getString("role_name");
                String permissions = rs.getString("permissions");
                jsonResponse.put("role_name", roleName);
                jsonResponse.put("permissions", permissions);
            } else {
                jsonResponse.put("Error", "Invalid UserId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            util.disconnect(conn);
        }
        return jsonResponse;
    }
}
