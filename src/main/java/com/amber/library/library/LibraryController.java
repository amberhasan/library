/**
 * LibraryController for Library Application.
 *
 * This class controls the GUI for a library management system, allowing the user to manage books and publishers.
 * It provides functionalities to add, delete, and search for books, as well as to clear input fields and refresh the books list.
 * The GUI elements are defined in FXML and this controller binds the data to these elements and handles user interactions.
 *
 * Written by Amber Hasan (amh130430) for CS 6360.MS1, starting on 3/1/2024.
 * The purpose of this class is to fulfill the assignment requirements by implementing a JavaFX application
 * that interacts with a database to manage a library's book inventory.
 *
 * @version 1.0
 */

package com.amber.library.library;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class LibraryController {

    // GUI component bindings
    @FXML
    private TableColumn<String, Number> authorsColumn;

    @FXML
    private TextField authorsTextField;

    @FXML
    private TableView<Book> booksTableView;

    @FXML
    private TextField deweyTextField;

    @FXML
    private TableColumn<Book, Number> isbnColumn;

    @FXML
    private TextField isbnTextField;

    @FXML
    private ComboBox<Publisher> publisherComboBox;

    @FXML
    private TableColumn<Book, String> titleColumn;

    @FXML
    private TextField titleTextField;

    // Database manager instance for DB operations
    private final DBMgr dbMgr;
    private ObservableList<Book> books;
    private  String mode = "Insert";
    ObservableList<Publisher> publisherList;
    private Book bookToUpdate;


    /**
     * Constructor initializes the database manager.
     */
    public LibraryController() {
        dbMgr = DBMgr.getInstance();
    }

    /**
     * Initializes the publishers ComboBox and TableView on GUI start-up.
     */
    @FXML
    private void initialize() {
        initializePublishers();
        initializeTableView();
    }

    /**
     * Fetches and displays the list of publishers in the publisherComboBox.
     */
    public void initializePublishers() {
        try {

            publisherList = dbMgr.getPublishers();
            publisherComboBox.setItems(publisherList);
        } catch (Exception ex) {
            System.out.println("initializePublishers failed " + ex.getMessage());
        }

    }

    /**
     * Retrieves a Publisher object based on its ID.
     *
     * @param id The unique ID of the publisher.
     * @return The Publisher object if found, null otherwise.
     */
    public Publisher getPublisher(int id){
        for (Publisher publisher : publisherList) {
            if (publisher.getId() == id) {
                return publisher;
            }
        }
        return null;
    }

    /**
     * Clears all input fields and resets the form to its default state.
     */
    @FXML
    void onClear() {
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

    /**
     * Deletes the selected book from the database and updates the books list.
     */
    @FXML
    void onDelete() {
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
        onClear();
    }

    /**
     * Deletes the selected book from the database and updates the books list.
     */
    @FXML
    void onSave() {
        System.out.println("onSave");
        try {
            // Collect input values
            String title = titleTextField.getText().trim();
            String authorName = authorsTextField.getText().trim();
            String isbn = isbnTextField.getText().trim();
            String dewey = deweyTextField.getText().trim();
            Publisher publisher = publisherComboBox.getValue();

            StringBuilder errors = new StringBuilder();
            String validationResult = null;

            validationResult = Validator.validateNotEmpty(title, "Title");
            if (validationResult != null) errors.append(validationResult).append("\n");

            validationResult = Validator.validateTitleAlphanumeric(title);
            if (validationResult != null) errors.append(validationResult).append("\n");

            validationResult = Validator.validateNotEmpty(authorName, "Author Name");
            if (validationResult != null) errors.append(validationResult).append("\n");

            validationResult = Validator.validateISBN(isbn);
            if (validationResult != null) errors.append(validationResult).append("\n");

            validationResult = Validator.validateDewey(dewey);
            if (validationResult != null) errors.append(validationResult).append("\n");

            if (publisher == null) errors.append("Publisher must be selected.\n");

            // Check if there were any errors
            if (!errors.isEmpty()) {
                showAlert("Validation Error", errors.toString(), true);
                return; // Exit the method if validation fails
            }

            if(mode.equals("Update")){
                if (dbMgr.updateBook(title, bookToUpdate)) {
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
            onClear();
        } catch (Exception ex) {
            System.out.println("onSave failed " + ex.getMessage());
        }
        refreshBooks();
    }

    /**
     * Searches for books based on the title entered in the titleTextField.
     */
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

    /**
     * Displays an alert dialog to the user.
     *
     * @param title The title of the alert.
     * @param message The message to display in the alert.
     * @param isError Determines if the alert is an error message or information message.
     */
    private void showAlert(String title, String message, Boolean isError) {
        Alert alert = new Alert(isError ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Initializes and populates the TableView with books.
     */
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

    /**
     * Refreshes the books displayed in the TableView by fetching them again from the database.
     */
    public void refreshBooks() {
        ObservableList<Book> allBooks = getBooks(); // Fetch all books
        booksTableView.setItems(allBooks); // Update the TableView
    }

    /**
     * Fetches all books from the database.
     *
     * @return An ObservableList of Book objects.
     */
    private ObservableList<Book> getBooks() {
        return dbMgr.getBooks();
    }
}
