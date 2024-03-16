package com.amber.library.library;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;


public class HelloController {

    @FXML
    private TableColumn<?, ?> authorsColumn;

    @FXML
    private TextField authorsTextField;

    @FXML
    private TableView<?> booksTableView;

    @FXML
    private Button clearBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private TextField deweyTextField;

    @FXML
    private TableColumn<?, ?> isbnColumn;

    @FXML
    private TextField isbnTextField;

    @FXML
    private ComboBox<Publishers> publisherComboBox;

    @FXML
    private Button saveBtn;

    @FXML
    private Button searchBtn;

    @FXML
    private TableColumn<?, ?> titleColumn;

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
    }

    public void initializePublishers() {
        try {

            ObservableList<Publishers> publishersList = dbMgr.getPublishers();
            publisherComboBox.setItems(publishersList);
        } catch (Exception ex) {
            System.out.println("initializePublishers failed " + ex.getMessage());
        }

    }

}
