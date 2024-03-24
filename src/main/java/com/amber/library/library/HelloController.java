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

    private  String mode = "Insert";

    ObservableList<Publisher> publisherList;

    private Book bookToUpdate;


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

            publisherList = dbMgr.getPublishers();
            publisherComboBox.setItems(publisherList);
        } catch (Exception ex) {
            System.out.println("initializePublishers failed " + ex.getMessage());
        }

    }

    public Publisher getPublisher(int id){
        for (Publisher publisher : publisherList) {
            if (publisher.getId() == id) { // Assuming getId() method exists
                return publisher;
            }
        }
        return null; // Return null or throw an exception if not found
    }

    @FXML
    void onClear(ActionEvent event) {
        System.out.println("onClear");
        // Clear text fields
        titleTextField.setText("");
        authorsTextField.setText("");
        isbnTextField.setText("");
        deweyTextField.setText("");
        mode = "Insert";
        bookToUpdate = null;
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
        refreshBooks();
        onClear(null);
    }



    @FXML
    void onSave(ActionEvent event) {
        System.out.println("onSave");
        try {
            // Collect input values
            String title = titleTextField.getText().trim();
            String authorName = authorsTextField.getText().trim();
            String isbn = isbnTextField.getText().trim();
            String dewey = deweyTextField.getText().trim();
            Publisher publisher = publisherComboBox.getValue();

            // Validate inputs
            if (title.isEmpty() || authorName.isEmpty() || isbn.isEmpty() || dewey.isEmpty() || publisher == null) {
                showAlert("Validation Error", "All fields are required.", true);
                return; // Exit the method if validation fails
            }

            if(mode.equals("Update")){
                if (dbMgr.updateBook(title, publisher.getId(), bookToUpdate)) {
                    showAlert("Success", "Data updated successfully.", false);
                    booksTableView.setItems(getBooks()); // Refresh the TableView
                } else {
                    showAlert("Failed", "Data not saved successfully.", true);
                }

            }else{
                if (dbMgr.insertBook(title, authorName, isbn, dewey, publisher.getId(), 100, "English", "Genre")) {
                    showAlert("Success", "Data saved successfully.", false);
                    booksTableView.setItems(getBooks()); // Refresh the TableView
                } else {
                    showAlert("Failed", "Data not saved successfully.", true);
                }
            }


            onClear(null);

        } catch (Exception ex) {
            System.out.println("onSave failed " + ex.getMessage());
        }
        refreshBooks();
    }

    @FXML
    void onSearch() {
        String searchQuery = titleTextField.getText().trim();
        if(searchQuery.isEmpty()){
            initializeTableView();
        }else{
            ObservableList<Book> searchResults = dbMgr.searchBooks(searchQuery);
            booksTableView.setItems(searchResults);
        }

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

        booksTableView.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    Book bookData = row.getItem();
                    bookToUpdate = bookData;
                    System.out.println("Single click on: " + bookData);
                    mode = "Update";
                    titleTextField.setText(bookData.getTitle());
                    authorsTextField.setText(bookData.getAuthors());
                    isbnTextField.setText(bookData.getIsbn());
                    deweyTextField.setText(""+bookData.getDewey());
                    publisherComboBox.setValue(getPublisher(bookData.getPublisherId().intValue()));
                }
            });
            return row;
        });
    }

    public void refreshBooks() {
        ObservableList<Book> allBooks = getBooks(); // Fetch all books
        booksTableView.setItems(allBooks); // Update the TableView
    }
    private ObservableList<Book> getBooks() {
        ObservableList<Book> books = dbMgr.getBooks();

//        DBMgr dbManager = DBMgr.getInstance();
//        Connection conn = dbManager.getConnection();
//
//        String query = """
//    SELECT b.BookID, b.ISBN, b.DeweyDecimalSystemNumber, p.Title, CONCAT(a.FirstName, ' ', COALESCE(a.MiddleName, ''), ' ', a.LastName) AS AuthorName, p.PublicationDate, pb.NumberOfPages, pb.Language, pb.Genre
//    FROM Book b
//    JOIN Publication p ON b.PublicationID = p.PublicationID
//    JOIN BookAuthor ba ON b.BookID = ba.BookID
//    JOIN Author a ON ba.AuthorID = a.AuthorID
//    JOIN PhysicalBook pb ON b.BookID = pb.BookID
//    """;
//
//        try (Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(query)) {
//
//            while (rs.next()) {
//                int id = rs.getInt("BookID");
//                String title = rs.getString("Title");
//                String authorName = rs.getString("AuthorName");
//                String isbn = rs.getString("ISBN");
//                String dewey = rs.getString("DeweyDecimalSystemNumber");
//                // Handle potential null values explicitly
//                authorName = authorName != null ? authorName : "null";
//                int publicationYear = rs.getDate("PublicationDate") != null ? rs.getDate("PublicationDate").toLocalDate().getYear() : 0; // Use 0 or some default for null publicationYear
//                int numberOfPages = rs.getInt("NumberOfPages");
//                String genre = rs.getString("Genre");
//                genre = genre != null ? genre : "null";
//
//                // Assuming your Book class has a constructor that matches these fields and handles nulls
//                books.add(new Book(id, title, authorName, isbn, dewey, 1)); // Adjust constructor call as necessary
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        return books;
    }



}
