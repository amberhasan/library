package com.amber.library.library;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HelloController {

    @FXML
    private TableColumn<String, Number> authorsColumn;

    @FXML
    private TextField authorsTextField;

    @FXML
    private TableView<Book> booksTableView;

    @FXML
    private Button clearBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private TextField deweyTextField;

    @FXML
    private TableColumn<Book, Number> isbnColumn;

    @FXML
    private TextField isbnTextField;

    @FXML
    private ComboBox<Publisher> publisherComboBox;

    @FXML
    private Button saveBtn;

    @FXML
    private Button searchBtn;

    @FXML
    private TableColumn<Book, String> titleColumn;

    @FXML
    private TextField titleTextField;

    private final DBMgr dbMgr;

    private ObservableList<Book> books;


    public HelloController() {
        dbMgr = DBMgr.getInstance();
    }

    @FXML
    private void initialize() {
        initializePublishers();
        initializeTableView();
    }

    public void initializePublishers() {
        try {

            ObservableList<Publisher> publisherList = dbMgr.getPublishers();
            publisherComboBox.setItems(publisherList);
        } catch (Exception ex) {
            System.out.println("initializePublishers failed " + ex.getMessage());
        }

    }

    @FXML
    void onClear(ActionEvent event) {
        System.out.println("onClear");
        // Clear text fields
        titleTextField.setText("");
        authorsTextField.setText("");
        isbnTextField.setText("");
        deweyTextField.setText("");

        // Reset ComboBox selection
        publisherComboBox.setValue(null);
    }

    @FXML
    void onDelete(ActionEvent event) {
        Book selectedBook = booksTableView.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            boolean isDeleted = DBMgr.getInstance().deleteBookAndReferences((Integer) selectedBook.getId());
            if (isDeleted) {
                // Make sure books is not null before attempting to remove from it
                if (books != null) {
                    books.remove(selectedBook);
                }
                showAlert("Success", "Book deleted successfully.", false);
            } else {
                showAlert("Error", "Failed to delete the book from the database.", true);
            }
        } else {
            showAlert("Error", "Please select a book to delete.", true);
        }
    }



    @FXML
    void onSave(ActionEvent event) {
        System.out.println("onSave");
        try {
            // Collect input values
            String title = titleTextField.getText().trim();
            String authors = authorsTextField.getText().trim();
            String isbn = isbnTextField.getText().trim();
            String dewey = deweyTextField.getText().trim();
            Publisher publisher = publisherComboBox.getValue();

            // Validate inputs
            if (title.isEmpty() || authors.isEmpty() || isbn.isEmpty() || dewey.isEmpty() || publisher == null) {
                showAlert("Validation Error", "All fields are required.", true);
                return; // Exit the method if validation fails
            }

            if (dbMgr.insertBook(title, isbn, dewey, publisher.getId(), 100, "English", "Fiction")) {
                showAlert("Success", "Data saved successfully.", false);
            } else {
                showAlert("Failed", "Data not saved successfully.", true);
            }


        } catch (Exception ex) {
            System.out.println("onSave failed " + ex.getMessage());
        }
    }

    @FXML
    void onSearch(ActionEvent event) {
        System.out.println("onSearch");
    }

    private void showAlert(String title, String message, Boolean isError) {
        Alert alert = new Alert(isError ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void initializeTableView() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        authorsColumn.setCellValueFactory(new PropertyValueFactory<>("authors"));

        // Add books to the table view
        booksTableView.setItems(getBooks());
    }

    private ObservableList<Book> getBooks() {
        ObservableList<Book> books = FXCollections.observableArrayList();

        DBMgr dbManager = DBMgr.getInstance();
        Connection conn = dbManager.getConnection();

        String query = """
        SELECT b.BookID, p.Title, CONCAT(a.FirstName, ' ', COALESCE(a.MiddleName, ''), ' ', a.LastName) AS AuthorName, p.PublicationDate, pb.NumberOfPages, pb.Language, pb.Genre
        FROM Book b
        JOIN Publication p ON b.PublicationID = p.PublicationID
        JOIN BookAuthor ba ON b.BookID = ba.BookID
        JOIN Author a ON ba.AuthorID = a.AuthorID
        JOIN PhysicalBook pb ON b.BookID = pb.BookID
        """;

        try (Statement stmt = ((Connection) conn).createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("BookID");
                String title = rs.getString("Title");
                String authorName = rs.getString("AuthorName");
                int publicationYear = rs.getDate("PublicationDate").toLocalDate().getYear();
                int numberOfPages = rs.getInt("NumberOfPages");
                // Assuming the 'genre' is represented as an integer in your Book class, otherwise adjust accordingly.
                String genre = rs.getString("Genre");

                // Assuming your Book class has a constructor that matches these fields
                books.add(new Book(id, title, authorName, publicationYear, numberOfPages, 1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }


}
