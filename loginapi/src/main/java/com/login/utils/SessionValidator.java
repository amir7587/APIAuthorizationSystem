package com.login.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionValidator {

    Utilities util = new Utilities();

    // Verify the session token and retrieve the associated user ID
    public static int verify(String token) {
        Utilities util = new Utilities();
        Connection conn = null;
        int userId = 0;
        // If token is null or empty, return 0 (indicating invalid token)
        if (token == null || token.isEmpty())
            return userId;
        try {
            conn = util.connect();
            // Query to retrieve the user ID associated with the given session token
            String query = "SELECT user_id FROM token WHERE session_token=?";
            ResultSet rs = util.executeQuery(util.prepareStatement(query, conn, token), conn);
            if (rs.next()) {
                userId = rs.getInt("user_id");
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            util.disconnect(conn);
        }
        // Return the user ID (0 if token is invalid)
        return userId;
    }

}
