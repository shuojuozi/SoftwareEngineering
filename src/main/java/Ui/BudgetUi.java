package Ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class BudgetUi extends NavigationSuper {
    private BorderPane root;
    private static double money ;

    public BudgetUi() {
        // Initialize the root layout
        root = new BorderPane();
        root.setLeft(createSidebar());
        root.setCenter(createDashboardPane());
    }
    @Override
    public void start(Stage primaryStage) {
        root = new BorderPane();
        root.setLeft(createSidebar()); // Sidebar for navigation
        root.setCenter(createDashboardPane()); // Default page (Dashboard)
        //VBox dashboardPane = createDashboardPane();
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static VBox createDashboardPane() {
        // Create four containers in a horizontal row
        HBox topRow = new HBox();
        topRow.setPadding(new Insets(20));
        topRow.setSpacing(200); // Add spacing between the containers
        topRow.setAlignment(Pos.CENTER);

        // Left Container - "总资产" & "设置月存储"
        VBox leftContainer = new VBox();
        leftContainer.setSpacing(20);
        leftContainer.setStyle("-fx-background-color: #cce5ff; -fx-background-radius: 10;");  // Light blue background
        leftContainer.getChildren().add(createInfoCard("Monthly Storage", "$" + money, "#cce5ff", "#004085"));
        Button leftButton = new Button("Setting");
        leftButton.setStyle("-fx-background-color: #004085; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px;");
        leftContainer.getChildren().add(leftButton);

        // Right Container - "设置储蓄目标" & "本月花费"
        VBox rightContainer = new VBox();
        rightContainer.setSpacing(20);
        rightContainer.setStyle("-fx-background-color: #f1f8ff; -fx-background-radius: 10;"); // Light gray-blue background
        rightContainer.getChildren().add(createInfoCard("Saving goals", "$" + money, "#f1f8ff", "#004085"));
        Button rightButton = new Button("Setting");
        rightButton.setStyle("-fx-background-color: #004085; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px;");
        rightContainer.getChildren().add(rightButton);

        // Set the HGrow property so both containers expand equally
        HBox.setHgrow(leftContainer, Priority.ALWAYS);
        HBox.setHgrow(rightContainer, Priority.ALWAYS);

        // Add the left and right containers to the top row
        topRow.getChildren().addAll(leftContainer, rightContainer);

        // Center Row - "AI" & "Output Text Area"
        HBox centerRow = new HBox();
        centerRow.setPadding(new Insets(20));
        centerRow.setSpacing(200); // Adjust spacing as needed
        centerRow.setAlignment(Pos.CENTER);

        // Progress Bar Section - Align with Left VBox (leftContainer)
        ProgressBar progressBar = new ProgressBar(0.69); // Example progress of 56%
        Label progressLabel = new Label("69%");
        HBox progressContainer = new HBox(progressBar, progressLabel);
        progressContainer.setAlignment(Pos.CENTER_LEFT);
        progressContainer.setSpacing(10);

        // Progress Bar Section1 - Align with Right VBox (rightContainer)
        ProgressBar progressBar1 = new ProgressBar(0.84); // Example progress of 56%
        Label progressLabel1 = new Label("84%");
        HBox progressContainer1 = new HBox(progressBar1, progressLabel1);
        progressContainer1.setAlignment(Pos.CENTER_RIGHT);
        progressContainer1.setSpacing(10);

        // Set the HGrow property for both progress bars to ensure they expand equally
        HBox.setHgrow(progressContainer, Priority.ALWAYS);
        HBox.setHgrow(progressContainer1, Priority.ALWAYS);

        // Add the progress containers to the center row
        centerRow.getChildren().addAll(progressContainer, progressContainer1);

        // AI Icon
        ImageView aiIcon = new ImageView("https://img1.baidu.com/it/u=1669554297,2393016886&fm=253&fmt=auto&app=120&f=JPEG?w=826&h=500");
        aiIcon.setFitWidth(50);
        aiIcon.setFitHeight(50);
        aiIcon.setPreserveRatio(true);
        aiIcon.setSmooth(true);
        aiIcon.setCache(true);

        // Output Text Area (smaller size to leave space for input area)
        TextArea outputTextArea = new TextArea();
        outputTextArea.setEditable(false);
        outputTextArea.setPromptText("AI output...");
        outputTextArea.setPrefHeight(100); // Reduce height for output area

        // New TextArea and Button at the bottom
        HBox inputRow = new HBox();
        inputRow.setPadding(new Insets(20));
        inputRow.setSpacing(10); // Set spacing between the text area and button
        inputRow.setAlignment(Pos.CENTER);

        // TextArea for user input (multi-line and scrollable)
        TextArea textArea = new TextArea();
        textArea.setPromptText("请输入内容..."); // Placeholder text
        textArea.setPrefWidth(800); // Make the text area take up all available width
        textArea.setPrefHeight(Double.MAX_VALUE);
        textArea.setStyle("-fx-font-size: 14px;");
        textArea.setWrapText(true); // Allow text to wrap

        // Button
        Button inputButton = new Button("submit");
        inputButton.setStyle("-fx-background-color: #004085; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px;");

        // Add TextArea and Button to the input row
        inputRow.getChildren().addAll(textArea, inputButton);

        // Main Layout
        VBox dashboardLayout = new VBox();
        dashboardLayout.setSpacing(20);
        dashboardLayout.getChildren().addAll(topRow, centerRow, aiIcon, outputTextArea, inputRow); // Add input row to the layout

        return dashboardLayout;
    }



    public static VBox createInfoCard(String title, String value, String backgroundColor, String textColor) {
        VBox infoCard = new VBox(20);
        infoCard.setStyle("-fx-background-color: " + backgroundColor + "; -fx-border-radius: 10px; -fx-padding: 10;");
        Label titleLabel = new Label(title);
        titleLabel.setFont(new Font(20));
        titleLabel.setStyle("-fx-text-fill: " + textColor + ";");
        Label valueLabel = new Label(value);
        valueLabel.setFont(new Font(20));
        valueLabel.setStyle("-fx-text-fill: " + textColor + ";");

        infoCard.getChildren().addAll(titleLabel, valueLabel);
        return infoCard;
    }


}
