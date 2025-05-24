package com.example;

import Ui.NavigationSuper;
import Ui.DashBoardUi;
import utils.DateContext;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Initialize the main container
        BorderPane root = new BorderPane();

        // Mount the navigation bar and default homepage content
        NavigationSuper.root = root;  // Used by sidebar buttons
        root.setLeft(NavigationSuper.createSidebar());
        root.setCenter(DashBoardUi.createDashboardPane(DateContext.getYear(), DateContext.getMonth()));

        // Set the scene and display the main stage
        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Smart Budget Assistant");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
