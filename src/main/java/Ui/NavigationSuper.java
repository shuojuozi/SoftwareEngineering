package Ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class NavigationSuper extends Application {
    public static BorderPane root;

    public NavigationSuper() {
        // Initialize the root layout
        root = new BorderPane();
        root.setLeft(createSidebar());

    }
    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.setLeft(createSidebar()); // Sidebar for navigation

        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.setTitle("Financial Dashboard");
        stage.show();
    }


    public static VBox createSidebar() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #f8f9fa;");

        // Labels for different pages
        box.getChildren().addAll(
                createSidebarLabel("📊 Dashboard", e -> root.setCenter(DashBoardUi.createDashboardPane())),
                createSidebarLabel("Trade management", e -> root.setCenter(TradeUi.createTradeManagementPage())),
                createSidebarLabel("Transaction details", e -> root.setCenter(TransactionUi.createTransactionDetailPage())),
                createSidebarLabel("Classified management of expenditure", e -> root.setCenter(ClassifiedUi.createDashboardPane())),
                createSidebarLabel("Budgeting and savings goals", e -> root.setCenter(BudgetUi.createDashboardPane())),
                createSidebarLabel("Analysis and report", e -> root.setCenter(AnalysisUi.createDashboardPane())),
                createSidebarLabel("Settings", e -> root.setCenter(Ui.SettingsUi.createSettingsPane()))

                );

        return box;
    }

    public static Label createSidebarLabel(String text, javafx.event.EventHandler<javafx.scene.input.MouseEvent> handler) {
        Label label = new Label(text);
        label.setOnMouseClicked(handler);
        return label;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
