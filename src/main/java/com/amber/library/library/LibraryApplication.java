package com.amber.library.library;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The main class for the Library Application.
 * This class extends javafx.application.Application and serves as the entry point for the JavaFX application.
 * It is responsible for loading the initial user interface from an FXML file and displaying it within a primary stage.
 * Written by Amber Hasan (amh130430) for CS 6360.MS1, starting on 3/1/2024.
 */
public class LibraryApplication extends Application {
    /**
     * Starts the JavaFX application by setting up the primary stage.
     * This method loads the FXML layout for the initial view of the application, sets the scene on the primary stage,
     * and then displays the stage. The FXML file defines the layout and controls for the application's user interface.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Load the FXML file specifying the layout of the application's user interface
        FXMLLoader fxmlLoader = new FXMLLoader(LibraryApplication.class.getResource("hello-view.fxml"));

        // Create a new scene with the loaded FXML layout and a specified width and height
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);

        // Set the title of the primary stage
        stage.setTitle("Library");

        // Set the scene on the primary stage to display the loaded FXML layout
        stage.setScene(scene);

        // Display the primary stage
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}