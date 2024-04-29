package com.login.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionValidator {

	Utilities util = new Utilities();

	public static int verify(String token) {
		Utilities util = new Utilities();
		Connection conn = null;
		int userId = 0;
		if (token == null || token.isEmpty())
			return userId;
		try {
			conn = util.connect();
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
		return userId;
	}

}
