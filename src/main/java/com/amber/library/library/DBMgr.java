package com.amber.library.library;
import java.sql.*;
import java.lang.ClassNotFoundException;
public class DBMgr {

    private static DBMgr instance;
    private Connection connection;

    private final String url = "jdbc:mysql://localhost:3306/library_db"; // Adjust this to your database
    private final String user = "root"; // Your database username
    private final String password = "admin"; // Your database password

    private DBMgr() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            System.out.println("Database Driver not found");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection failed");
            e.printStackTrace();
        }
    }

    public static DBMgr getInstance() {
        if (instance == null) {
            synchronized (DBMgr.class) {
                if (instance == null) {
                    instance = new DBMgr();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public ResultSet executeQuery(String query) {
        try (Statement stmt = connection.createStatement()) {
            ResultSet result =  stmt.executeQuery(query);
            while (result.next()) {
                // Assuming the table has columns 'id', 'name', and 'age'
                int id = result.getInt("PublisherID"); // You can also use column indices, e.g., getInt(1)
                String name = result.getString("Name");
                String web = result.getString("Website");

                // Do something with the data
                System.out.printf("ID: %d, Name: %s, web: %s%n", id, name, web);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Optional: Add a method to close the connection when the application terminates
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                instance = null; // Allow for reconnection later if needed
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
