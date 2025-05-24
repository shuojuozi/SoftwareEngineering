package Ui;

import utils.DateContext;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class NavigationSuper {
    public static BorderPane root;

    public static VBox createSidebar() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #f8f9fa;");

        // Labels for different pages
        box.getChildren().addAll(
                createSidebarLabel("📊 Dashboard", e -> root.setCenter(
                        DashBoardUi.createDashboardPane(DateContext.getYear(), DateContext.getMonth()))),
                createSidebarLabel("💼 Trade management", e -> root.setCenter(
                        TradeUi.createTradeManagementPage())),
//              createSidebarLabel("Classified management of expenditure", e -> root.setCenter(
//                      ClassifiedUi.createDashboardPane())),
                createSidebarLabel("💰 Budgeting and savings goals", e -> root.setCenter(
                        BudgetUi.createDashboardPane())),
                createSidebarLabel("📈 Analysis and report", e -> root.setCenter(
                        AnalysisUi.createDashboardPane())),
                createSidebarLabel("🛠 Settings", e -> root.setCenter(
                        SettingsUi.createSettingsPane()))
        );

        return box;
    }

    public static Label createSidebarLabel(String text, javafx.event.EventHandler<MouseEvent> handler) {
        Label label = new Label(text);
        label.setOnMouseClicked(handler);
        return label;
    }
}
