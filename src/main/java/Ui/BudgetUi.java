package Ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;


public class BudgetUi extends NavigationSuper {
    private BorderPane root;

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
        // Left Container
        VBox leftContainer = new VBox();
        leftContainer.setPadding(new Insets(20));
        leftContainer.setSpacing(10);

        // Monthly Transaction Settings
        Label monthlyTransactionLabel = new Label("设置月交易");
        HBox monthlyTransactionRow = new HBox();
        Label monthlyAmountLabel = new Label("$0");
        CheckBox monthlyTransactionCheckbox = new CheckBox("启用");
        monthlyTransactionRow.getChildren().addAll(monthlyAmountLabel, monthlyTransactionCheckbox);
        monthlyTransactionRow.setSpacing(10);

        // Add to left container
        leftContainer.getChildren().addAll(monthlyTransactionLabel, monthlyTransactionRow);

        // Right Container
        VBox rightContainer = new VBox();
        rightContainer.setPadding(new Insets(20));
        rightContainer.setSpacing(10);

        // Savings Goal Settings
        Label savingsGoalLabel = new Label("设置储蓄目标");
        HBox savingsGoalRow = new HBox();
        Label savingsAmountLabel = new Label("$0");
        CheckBox savingsGoalCheckbox = new CheckBox("启用");
        savingsGoalRow.getChildren().addAll(savingsAmountLabel, savingsGoalCheckbox);
        savingsGoalRow.setSpacing(10);

        // Add to right container
        rightContainer.getChildren().addAll(savingsGoalLabel, savingsGoalRow);

        // Progress Bar
        ProgressBar progressBar = new ProgressBar(0.56); // Example progress of 56%
        Label progressLabel = new Label("56%");
        HBox progressContainer = new HBox(progressBar, progressLabel);
        progressContainer.setAlignment(Pos.CENTER);
        progressContainer.setSpacing(10);

        // AI Icon
        ImageView aiIcon = new ImageView("https://tse2-mm.cn.bing.net/th/id/OIP-C.uTCSuJ7CQm_yA_WNnTqlhAHaHa?rs=1&pid=ImgDetMain"); // Use a valid path to your AI icon image
        aiIcon.setFitWidth(50);
        aiIcon.setFitHeight(50);
        aiIcon.setPreserveRatio(true);
        aiIcon.setSmooth(true);
        aiIcon.setCache(true);

        // Output Text Area
        TextArea outputTextArea = new TextArea();
        outputTextArea.setEditable(false);
        outputTextArea.setPromptText("AI返回的结果...");

        // Main Layout
        HBox mainContainer = new HBox(leftContainer, rightContainer);
        mainContainer.setSpacing(20);
        mainContainer.setAlignment(Pos.CENTER);

        VBox dashboardLayout = new VBox();
        dashboardLayout.getChildren().addAll(mainContainer, progressContainer, aiIcon, outputTextArea);
        dashboardLayout.setAlignment(Pos.CENTER);
        dashboardLayout.setSpacing(20);

        return dashboardLayout;
    }


}
