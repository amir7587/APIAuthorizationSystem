package com.login.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


import javax.crypto.Cipher;
import java.security.Key;
import java.security.spec.KeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Utilities {

	private final static String SECRET_KEY = "MySecr3tK3y!f0rEncrypt10n"; 
	private final String SALT = "";
	private Properties properties;

	public Utilities() {
		loadProperties();
	}

	private void loadProperties() {
		properties = new Properties();
		try (InputStream input = Utilities.class.getClassLoader().getResourceAsStream("config.properties")) {
			properties.load(input);
		} catch (IOException ex) {
			System.err.println("Failed to load properties file: " + ex.getMessage());
		}
	}

	public String getPropertyValue(String property) {
		return properties.getProperty(property, "");
	}

	public Connection connect() throws SQLException {
		try {
			String url = "jdbc:mysql://" + getPropertyValue("server") + ":3306/" + getPropertyValue("database");
			boolean useSSL = getPropertyValue("usessl").equalsIgnoreCase("true");

			if (useSSL) {
				url += "?useSSL=true&requireSSL=true";
			}

			Class.forName("com.mysql.cj.jdbc.Driver");
			return DriverManager.getConnection(url, getPropertyValue("username"),
					decrypt(getPropertyValue("password")));
		} catch (ClassNotFoundException | SQLException ex) {
			throw new SQLException("Failed to connect to the database: " + ex.getMessage());
		}
	}

	public void disconnect(Connection conn) {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			System.err.println("Error while disconnecting: " + e.getMessage());
		}
	}

	private String decrypt(String password) {
		return password + SALT;
	}

	public ResultSet executeQuery(PreparedStatement statement, Connection conn) {
		ResultSet rs = null;
		try {
			rs = statement.executeQuery();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rs;

	}
	

	public int executeUpdate(PreparedStatement statement, Connection conn) {
		int key = 0;
		ResultSet rs = null;
		try {
			int row = statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
			rs.close();
			statement.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return key;
	}
	

	public PreparedStatement prepareStatement(String query, Connection conn, String... param) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			int count = 1;
			for (int i = 0; i < param.length; i++) {
				if (param[i] == null) {
					ps.setObject(count++, param[i]);
				} else {
					ps.setObject(count++, param[i].replaceAll("_COMMA_", ","));
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ps;

	}
	
	public PreparedStatement prepareStatement(String query, Connection conn, int... param) {
	    PreparedStatement ps = null;
	    try {
	        ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
	        int count = 1;
	        for (int i = 0; i < param.length; i++) {
	            ps.setInt(count++, param[i]);
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    return ps;
	}
	
	 public PreparedStatement prepareStatement(String query, Connection conn) throws SQLException {
	        PreparedStatement ps = null;
	        try {
	            ps = conn.prepareStatement(query);
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	            throw ex;
	        }
	        return ps;
	    }
	
	
	
	
	public String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		
		return dateFormat.format(date);
	}
	
	
	 public static String encryptPassword(String input) {
	        try {
	            Key key = generateKey();
	            Cipher cipher = Cipher.getInstance("AES");
	            cipher.init(Cipher.ENCRYPT_MODE, key);
	            byte[] encryptedBytes = cipher.doFinal(input.getBytes());
	            return Base64.getEncoder().encodeToString(encryptedBytes);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	    }

	    public static String decryptPassword(String encryptedInput) {
	        try {
	            Key key = generateKey();
	            Cipher cipher = Cipher.getInstance("AES");
	            cipher.init(Cipher.DECRYPT_MODE, key);
	            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedInput));
	            return new String(decryptedBytes);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	    }

	    private static Key generateKey() {
	        return new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
	    }
	
	
	
	
	public static void main(String[] args) {
		Utilities utilities = new Utilities();
		try (Connection conn = utilities.connect()) {
			System.out.println("Connected to the database!");
		} catch (SQLException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

}
