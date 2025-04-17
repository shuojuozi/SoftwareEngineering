package Ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class DashBoardUi extends NavigationSuper {
    private BorderPane root;

    public DashBoardUi() {
        // Initialize the root layout
        root = new BorderPane();
        root.setLeft(createSidebar());
        root.setCenter(createDashboardPane());
    }
    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.setLeft(createSidebar()); // Sidebar for navigation
        root.setCenter(createDashboardPane()); // Default page (Dashboard)
        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.setTitle("Financial Dashboard");
        stage.show();
    }




    public static VBox createDashboardPane() {
        HBox summaryBox = new HBox(20);
        summaryBox.setPadding(new Insets(20));
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.getChildren().addAll(
                createInfoCard("Total Assets", "$120,500", "#cce5ff", "#004085"),
                createInfoCard("Monthly Expense", "$5,200", "#f8d7da", "#721c24"),
                createInfoCard("Monthly Income", "$7,300", "#d4edda", "#155724"),
                createInfoCard("Savings Goal Progress", "56%", "#fff3cd", "#856404")
        );

        // Bar Chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Recent Transactions");
        xAxis.setLabel("Category");
        yAxis.setLabel("Amount");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Recent Transactions");
        series.getData().addAll(
                new XYChart.Data<>("Rent", 1100),
                new XYChart.Data<>("Groceries", 750),
                new XYChart.Data<>("Utilities", 400),
                new XYChart.Data<>("Transport", 300),
                new XYChart.Data<>("Misc", 500)
        );
        barChart.getData().add(series);

        // Pie Chart
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Spending by Category");
        pieChart.getData().addAll(
                new PieChart.Data("Rent", 35),
                new PieChart.Data("Groceries", 25),
                new PieChart.Data("Utilities", 10),
                new PieChart.Data("Transport", 10),
                new PieChart.Data("Misc", 20)
        );

        HBox chartsBox = new HBox(20, barChart, pieChart);
        chartsBox.setPadding(new Insets(20));
        chartsBox.setAlignment(Pos.CENTER);
        return new VBox(summaryBox, chartsBox);
    }

    public VBox createTradeManagementPage() {
        TradeUi tradeUi = new TradeUi();
        return tradeUi.createTradeManagementPage();
    }

    public VBox createTransactionDetailPage() {
        TransactionUi transactionUi = new TransactionUi();
        return transactionUi.createTransactionDetailPage();
    }

    /*private VBox createClassifiedPage() {
        ClassifiedUi classifiedUi = new ClassifiedUi();
        return classifiedUi.getClassifiedManagementContent();
    }*/

    private VBox createBudgetingPane() {
        return new VBox(new Label("Budgeting and savings goals page..."));
    }

    private VBox createAnalysisPane() {
        return new VBox(new Label("Analysis and Report page..."));
    }

    private VBox createTesterPane() {
        return new VBox(new Label("Tester page..."));
    }
    public static VBox createInfoCard(String title, String value, String backgroundColor, String textColor) {
        VBox infoCard = new VBox(10);
        infoCard.setStyle("-fx-background-color: " + backgroundColor + "; -fx-border-radius: 10px; -fx-padding: 10;");
        Label titleLabel = new Label(title);
        titleLabel.setFont(new Font(16));
        titleLabel.setStyle("-fx-text-fill: " + textColor + ";");
        Label valueLabel = new Label(value);
        valueLabel.setFont(new Font(20));
        valueLabel.setStyle("-fx-text-fill: " + textColor + ";");

        infoCard.getChildren().addAll(titleLabel, valueLabel);
        return infoCard;
    }


}
