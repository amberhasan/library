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

    private void linkBookToAuthor(Connection conn, int bookId, int authorId) throws SQLException {
        String sql = "INSERT INTO BookAuthor (BookID, AuthorID) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, authorId);
            pstmt.executeUpdate();
        }
    }

    public boolean insertBook(String title, String authorName, String isbn, String deweyDecimal, int publisherId, int numberOfPages, String language, String genre) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert into Publication table
            int publicationId = insertPublication(conn, title, publisherId);
            System.out.println("Publication inserted successfully with ID: " + publicationId);

            // Insert into Author table
            int authorId = insertAuthor(conn, authorName);
            System.out.println("Author inserted successfully with ID: " + authorId);

            // Insert into Book table
            int bookId = insertBookEntry(conn, publicationId, isbn, deweyDecimal);
            System.out.println("Book inserted successfully with ID: " + bookId);

            // Insert into PhysicalBook table
            insertPhysicalBook(conn, bookId, numberOfPages, language, genre);
            System.out.println("PhysicalBook details inserted successfully for BookID: " + bookId);

            // Link the book to an author (e.g., AuthorID = 1 for simplicity)
            linkBookToAuthor(conn, bookId, authorId); // Assuming AuthorID = 1 exists
            System.out.println("Book linked to Author successfully.");

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

    public boolean updateBook(String title, int publisherId, Book bookToUpdate ) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction

            String sql = "UPDATE Publication SET Title = ?, Type = 'Book' WHERE title = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title); // Assuming 'title' holds the new title value
                pstmt.setString(2, bookToUpdate.getTitle()); // Assuming 'publicationId' is the ID of the publication to update
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Updating publication failed, no rows affected.");
                }
            }

            conn.commit(); // Commit transaction
            System.out.println("Transaction committed successfully.");
            return true;
        } catch (SQLException e) {
            System.out.println("Updating book failed: " + e.getMessage());
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

    private int insertAuthor(Connection conn, String authorName) throws SQLException {
        String sql = "INSERT INTO Author (FirstName, MiddleName, LastName) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, authorName);
            pstmt.setString(2, "");
            pstmt.setString(3, "");
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating author failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating author failed, no ID obtained.");
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

    public ObservableList<Book> searchBooks(String title) {
        ObservableList<Book> books = FXCollections.observableArrayList();
        String sql = """
        SELECT b.BookID, p.Title, CONCAT(a.FirstName, ' ', COALESCE(a.MiddleName, ''), ' ', a.LastName) AS AuthorName, 
        p.PublicationDate, pb.NumberOfPages, pb.Language, pb.Genre
        FROM Book b
        JOIN Publication p ON b.PublicationID = p.PublicationID
        LEFT JOIN BookAuthor ba ON b.BookID = ba.BookID
        LEFT JOIN Author a ON ba.AuthorID = a.AuthorID
        LEFT JOIN PhysicalBook pb ON b.BookID = pb.BookID
        WHERE p.Title LIKE ?
        """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + title + "%"); // Use the title in the query
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("BookID");
                String bookTitle = rs.getString("Title");
                String isbn = rs.getString("ISBN");
                String dewey = rs.getString("DeweyDecimalSystemNumber");
                String authorName = rs.getString("AuthorName"); // Handle potential nulls accordingly
                String genre = rs.getString("Genre"); // Same as above
                // Assuming your Book class has a constructor that matches these fields
                books.add(new Book(id, bookTitle, authorName, isbn, dewey, 123));
            }
        } catch (SQLException e) {
            System.out.println("Search failed: " + e.getMessage());
        }
        return books;
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
