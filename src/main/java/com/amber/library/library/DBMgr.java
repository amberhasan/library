package com.amber.library.library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.lang.ClassNotFoundException;

public class DBMgr {

    private static DBMgr instance;
    private Connection connection;

    private final String url = "jdbc:mysql://localhost:3306/library_db"; // Adjust this to your database
    private final String user = "root2"; // Your database username
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

    public ObservableList<Publisher> getPublishers() {
        ObservableList<Publisher> publisherList = FXCollections.observableArrayList();
        try (Statement stmt = connection.createStatement()) {

            ResultSet result = stmt.executeQuery("Select * from publisher");
            while (result.next()) {
                // Assuming the table has columns 'id', 'name', and 'age'
                int id = result.getInt("PublisherID"); // You can also use column indices, e.g., getInt(1)
                String name = result.getString("Name");
                String web = result.getString("Website");
                publisherList.add(new Publisher(
                        id, name, web
                ));

                // Do something with the data
                System.out.printf("ID: %d, Name: %s, web: %s%n", id, name, web);
            }
            return publisherList;
        } catch (SQLException e) {
            e.printStackTrace();
            return publisherList;
        }
    }

    public boolean insertBook(String title, String isbn, String deweyDecimal, int publisherId, int numberOfPages, String language, String genre) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert into Publication table
            int publicationId = insertPublication(conn, title, publisherId);
            System.out.println("Publication inserted successfully with ID: " + publicationId);

            // Insert into Book table
            int bookId = insertBookEntry(conn, publicationId, isbn, deweyDecimal);
            System.out.println("Book inserted successfully with ID: " + bookId);

            // Insert into PhysicalBook table
            insertPhysicalBook(conn, bookId, numberOfPages, language, genre);
            System.out.println("PhysicalBook details inserted successfully for BookID: " + bookId);

            conn.commit(); // Commit transaction
            System.out.println("Transaction committed successfully.");
            return true;
        } catch (SQLException e) {
            System.out.println("Inserting book failed: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                    System.out.println("Transaction rolled back.");
                } catch (SQLException ex) {
                    System.out.println("Rollback failed: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset default commit behavior
                } catch (SQLException e) {
                    System.out.println("Error resetting auto-commit: " + e.getMessage());
                }
            }
        }
    }

    private int insertPublication(Connection conn, String title, int publisherId) throws SQLException {
        String sql = "INSERT INTO Publication (Title, PublisherID, Type) VALUES (?, ?, 'Book')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            pstmt.setInt(2, publisherId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating publication failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating publication failed, no ID obtained.");
                }
            }
        }
    }

    private int insertBookEntry(Connection conn, int publicationId, String isbn, String deweyDecimal) throws SQLException {
        String sql = "INSERT INTO Book (PublicationID, ISBN, DeweyDecimalSystemNumber) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, publicationId);
            pstmt.setString(2, isbn);
            pstmt.setString(3, deweyDecimal);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating book entry failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating book entry failed, no ID obtained.");
                }
            }
        }
    }

    private void insertPhysicalBook(Connection conn, int bookId, int numberOfPages, String language, String genre) throws SQLException {
        String sql = "INSERT INTO PhysicalBook (BookID, NumberOfPages, Language, Genre) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, numberOfPages);
            pstmt.setString(3, language);
            pstmt.setString(4, genre);
            pstmt.executeUpdate();
        }
    }


    public Boolean deleteBookAndReferences(int bookId) {
        try {
            connection.setAutoCommit(false); // Start transaction

            // 1. Delete references in dependent tables
            String[] dependentTables = new String[]{"PhysicalBook", "AudioBook", "EBook", "BookAuthor"};
            for (String table : dependentTables) {
                String sqlDeleteReferences = String.format("DELETE FROM %s WHERE BookID = ?", table);
                try (PreparedStatement pstmtDeleteReferences = connection.prepareStatement(sqlDeleteReferences)) {
                    pstmtDeleteReferences.setInt(1, bookId);
                    pstmtDeleteReferences.executeUpdate();
                }
            }

            // 2. Delete the book
            String sqlDeleteBook = "DELETE FROM Book WHERE BookID = ?";
            try (PreparedStatement pstmtDeleteBook = connection.prepareStatement(sqlDeleteBook)) {
                pstmtDeleteBook.setInt(1, bookId);
                int affectedRows = pstmtDeleteBook.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback(); // Rollback if no book was deleted
                    System.out.println("No book found with the specified ID, rolling back.");
                    return false;
                }
            }

            connection.commit(); // Commit transaction
            System.out.println("Book and its references deleted successfully.");
            return true;
        } catch (SQLException e) {
            System.out.println("Deleting book failed: " + e.getMessage());
            try {
                if (connection != null) {
                    connection.rollback(); // Attempt to roll back on error
                    System.out.println("Transaction is rolled back.");
                }
            } catch (SQLException ex) {
                System.out.println("Error rolling back transaction: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true); // Reset auto-commit behavior
            } catch (SQLException e) {
                System.out.println("Error resetting auto-commit: " + e.getMessage());
            }
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
