package com.amber.library.library;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;

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


    public HelloController() {
        dbMgr = DBMgr.getInstance();
    }

    @FXML
    private void initialize() {
        // This method is automatically called after FXML fields are injected.
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
    }

    @FXML
    void onDelete(ActionEvent event) {
        System.out.println("onDelete");
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

            // Simple ISBN validation (adjust regex as needed for your use case)
//            if (!isbn.matches("\\d{13}")) {
//                showAlert("Validation Error", "ISBN must be 13 digits.");
//                return; // Exit the method if validation fails
//            }
            if (dbMgr.insertBook(title, isbn, dewey, publisher.getId())) {
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
        // Add some sample books
        books.add(new Book(1, "The Great Gatsby", "F. Scott Fitzgerald", 1925, 0, 1));
        books.add(new Book(2, "Moby Dick", "Herman Melville", 1851, 0, 1));
        // Add more books as needed

        return books;
    }


}
