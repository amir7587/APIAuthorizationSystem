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

public class Utilities {

    private final static String SECRET_KEY = "MySecr3tK3y!f0rEncrypt10n"; // Secret key for encryption
    private final String SALT = ""; // Salt for password decryption
    private Properties properties;

    public Utilities() {
        loadProperties();
    }

    // Load properties from config.properties file
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = Utilities.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            System.err.println("Failed to load properties file: " + ex.getMessage());
        }
    }

    // Get property value from loaded properties
    public String getPropertyValue(String property) {
        return properties.getProperty(property, "");
    }

    // Establish database connection
    public Connection connect() throws SQLException {
        try {
            // Construct database URL
            String url = "jdbc:mysql://" + getPropertyValue("server") + ":3306/" + getPropertyValue("database");
            boolean useSSL = getPropertyValue("usessl").equalsIgnoreCase("true");

            if (useSSL) {
                url += "?useSSL=true&requireSSL=true";
            }

            // Load MySQL JDBC driver and connect to database
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, getPropertyValue("username"),
                    decrypt(getPropertyValue("password")));
        } catch (ClassNotFoundException | SQLException ex) {
            throw new SQLException("Failed to connect to the database: " + ex.getMessage());
        }
    }

    // Disconnect from the database
    public void disconnect(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error while disconnecting: " + e.getMessage());
        }
    }

    // Decrypt password (not recommended to store passwords in plain text)
    private String decrypt(String password) {
        return password + SALT;
    }

    // Execute SQL query and return result set
    public ResultSet executeQuery(PreparedStatement statement, Connection conn) {
        ResultSet rs = null;
        try {
            rs = statement.executeQuery();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rs;
    }

    // Execute SQL update (insert, update, delete) and return auto-generated key
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

    // Prepare statement with String parameters
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

    // Prepare statement with int parameters
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

    // Prepare statement without parameters
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

    // Get current date and time in "yyyy-MM-dd HH:mm:ss" format
    public String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

}
