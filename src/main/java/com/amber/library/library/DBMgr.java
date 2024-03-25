package com.amber.library.library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.lang.ClassNotFoundException;

/**
 * Manages database operations for the library application.
 * This class handles all interactions with the database, including connecting to the database,
 * and performing CRUD operations for books, authors, publishers, and related entities.
 * It follows the Singleton pattern to ensure only one instance of this class manages the database connections and operations.
 * Written by Amber Hasan (amh130430) for CS 6360.MS1, starting on 3/1/2024.
 */
public class DBMgr {

    private static DBMgr instance;
    private Connection connection;
    private final String url = "jdbc:mysql://localhost:3306/library_db"; // Adjust this to your database
    private final String user = "root2"; // Your database username
    private final String password = "admin"; // Your database password

/**
 * Private constructor to prevent direct instantiation.
 * Initializes the connection to the database.
 */
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

/**
 * Returns the single instance of DBMgr, creating it if it does not already exist.
 * This method is thread-safe.
 *
 * @return The single instance of DBMgr.
 */
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

/**
 * Gets the current database connection.
 *
 * @return The current Connection object.
 */
    public Connection getConnection() {
        return connection;
    }

/**
 * Gets the current database connection.
 *
 * @return The current Connection object.
 */
    public ObservableList<Publisher> getPublishers() {
        ObservableList<Publisher> publisherList = FXCollections.observableArrayList();
        try (Statement stmt = connection.createStatement()) {

            ResultSet result = stmt.executeQuery("Select * from publisher");
            while (result.next()) {
                int id = result.getInt("PublisherID"); // You can also use column indices, e.g., getInt(1)
                String name = result.getString("Name");
                String web = result.getString("Website");
                publisherList.add(new Publisher(
                        id, name, web
                ));
                System.out.printf("ID: %d, Name: %s, web: %s%n", id, name, web);
            }
            return publisherList;
        } catch (SQLException e) {
            e.printStackTrace();
            return publisherList;
        }
    }

/**
 * Links a book to an author in the BookAuthor table.
 * This is a utility method used internally during book insertion to associate books with their authors.
 *
 * @param conn The database connection.
 * @param bookId The ID of the book.
 * @param authorId The ID of the author.
 * @throws SQLException If any SQL errors occur during the operation.
 */
    private void linkBookToAuthor(Connection conn, int bookId, int authorId) throws SQLException {
        String sql = "INSERT INTO BookAuthor (BookID, AuthorID) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, authorId);
            pstmt.executeUpdate();
        }
    }

/**
 * Inserts a new book into the database.
 * This method orchestrates the insertion of a book by managing transactions across multiple tables,
 * including Publication, Author, Book, PhysicalBook, and the BookAuthor link table. This ensures that
 * all related entities are consistently updated in a single transaction.
 *
 * @param title The title of the book.
 * @param authorName The name of the author.
 * @param isbn The ISBN of the book.
 * @param deweyDecimal The Dewey Decimal classification for the book.
 * @param publisherId The ID of the publisher.
 * @param numberOfPages The number of pages in the book.
 * @param language The language of the book.
 * @param genre The genre of the book.
 * @return true if the book and its related data were successfully inserted; false otherwise.
 * This includes rolling back the transaction in case of any failures to ensure data integrity.
 */
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

/**
 * Updates the title of an existing book in the database.
 * This method is part of managing the book inventory where book details might change over time.
 * It demonstrates handling a simple transaction to ensure the title is updated atomically.
 *
 * @param title The new title for the book.
 * @param bookToUpdate The book object containing the existing details of the book, including its current title.
 * @return true if the book's title was successfully updated; false otherwise.
 * Similar to insertBook, this method manages transactions to ensure consistency and rolls back changes if the update fails.
 */
    public boolean updateBook(String title, Book bookToUpdate ) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction

            String sql = "UPDATE Publication SET Title = ?, Type = 'Book' WHERE title = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, bookToUpdate.getTitle());
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

/**
 * Inserts a new publication record into the Publication table.
 * This helper method is called during the book insertion process to create a new publication entity.
 * It returns the generated ID for the new publication to be used in subsequent insert operations.
 *
 * @param conn The database connection.
 * @param title The title of the publication.
 * @param publisherId The ID of the publisher.
 * @return The generated ID of the new publication record.
 * @throws SQLException If the insertion fails, indicating no rows affected or unable to obtain the generated ID.
 */
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

/**
 * Inserts a new author into the database.
 * This method supports the insertion of books by ensuring that authors are uniquely stored.
 * It creates a new author record and returns the generated ID for linking with the book.
 *
 * @param conn The database connection.
 * @param authorName The full name of the author.
 * @return The generated ID for the new author record.
 * @throws SQLException If the insertion fails, such as when no rows are affected or the generated ID cannot be obtained.
 */
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

/**
 * Inserts a book entry linking it to its publication record.
 * After creating a publication record, this method inserts the book-specific details into the Book table.
 *
 * @param conn The database connection.
 * @param publicationId The ID of the publication associated with this book.
 * @param isbn The ISBN of the book.
 * @param deweyDecimal The Dewey Decimal classification of the book.
 * @return The generated ID for the new book record, used for further book-related insertions.
 * @throws SQLException If the book entry cannot be created due to SQL errors.
 */
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

/**
 * Inserts details of a physical book into the PhysicalBook table.
 * This complements the book entry with physical attributes such as number of pages, language, and genre.
 *
 * @param conn The database connection.
 * @param bookId The ID of the book, linking it to the book entry.
 * @param numberOfPages The number of pages in the book.
 * @param language The language of the book.
 * @param genre The genre of the book.
 * @throws SQLException If inserting the physical book details fails.
 */
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

/**
 * Deletes a book and its references from the database.
 * This method ensures that all records related to a book, such as author links and physical book details,
 * are removed in a single transaction to maintain database integrity.
 *
 * @param bookId The ID of the book to be deleted.
 * @return true if the book and all its references were successfully deleted; false if the book does not exist
 * or the deletion failed, in which case the transaction is rolled back.
 */
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

/**
 * Searches for books by title.
 * This method allows for partial matches and returns a list of books that match the search criteria.
 * It demonstrates using prepared statements to prevent SQL injection and
 * ensure secure query execution.
 *
 * @param title The title (or partial title) to search for.
 * @return An ObservableList of Book objects that match the search criteria.
 * Each book object contains details such as ID, title, ISBN, and author names,
 * making it possible to display comprehensive search results in the UI.
 *  */
    public ObservableList<Book> searchBooks(String title) {
        ObservableList<Book> books = FXCollections.observableArrayList();
        Connection conn = getConnection();
        String sql = """
        SELECT b.BookID, b.ISBN,b.DeweyDecimalSystemNumber, p.Title, CONCAT(a.FirstName, ' ', COALESCE(a.MiddleName, ''), ' ', a.LastName) AS AuthorName, 
        p.PublicationDate, pb.NumberOfPages, pb.Language, pb.Genre
        FROM Book b
        JOIN Publication p ON b.PublicationID = p.PublicationID
        LEFT JOIN BookAuthor ba ON b.BookID = ba.BookID
        LEFT JOIN Author a ON ba.AuthorID = a.AuthorID
        LEFT JOIN PhysicalBook pb ON b.BookID = pb.BookID
        WHERE p.Title LIKE ?
        """;
        try (
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + title + "%"); // Use the title in the query
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("BookID");
                String bookTitle = rs.getString("Title");
                String isbn = rs.getString("ISBN");
                String dewey = rs.getString("DeweyDecimalSystemNumber");
                String authorName = rs.getString("AuthorName");
                books.add(new Book(id, bookTitle, authorName, isbn, dewey, 123));
            }
        } catch (SQLException e) {
            System.out.println("Search failed: " + e.getMessage());
        }
        return books;
    }

/**
 * Retrieves all books from the database.
 * This method fetches detailed information about each book, including its publication details,
 * author(s), and physical book attributes like number of pages and genre. It is used to populate
 * the initial list of books in the UI or refresh the list after updates to the database.
 *
 * @return An ObservableList containing Book objects for all books in the database.
 * The list may be empty if no books are found or in case of a database access error.
 */
    public ObservableList<Book> getBooks() {
        ObservableList<Book> books = FXCollections.observableArrayList();
        Connection conn = getConnection();
        String query = """
    SELECT b.BookID, b.ISBN, b.DeweyDecimalSystemNumber, p.Title, CONCAT(a.FirstName, ' ', COALESCE(a.MiddleName, ''), ' ', a.LastName) AS AuthorName, p.PublicationDate, pb.NumberOfPages, pb.Language, pb.Genre
    FROM Book b
    JOIN Publication p ON b.PublicationID = p.PublicationID
    JOIN BookAuthor ba ON b.BookID = ba.BookID
    JOIN Author a ON ba.AuthorID = a.AuthorID
    JOIN PhysicalBook pb ON b.BookID = pb.BookID
    """;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("BookID");
                String title = rs.getString("Title");
                String authorName = rs.getString("AuthorName");
                String isbn = rs.getString("ISBN");
                String dewey = rs.getString("DeweyDecimalSystemNumber");
                authorName = authorName != null ? authorName : "null";
                books.add(new Book(id, title, authorName, isbn, dewey, 1)); // Adjust constructor call as necessary
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }
}
