package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    private BorderPane root;

    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.setLeft(createSidebar());
        root.setCenter(createDashboardPane()); // 默认首页

        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.setTitle("Financial Dashboard");
        stage.show();
    }

    private VBox createSidebar() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #f8f9fa;");

        Label dashboardLabel  = new Label("📊 Dashboard");
        Label tradeLabel      = new Label("Trade management");
        Label transactionLabel= new Label("Transaction details");
        Label classifiedLabel = new Label("Classified management of expenditure");
        Label budgetLabel     = new Label("Budgeting and savings goals");
        Label analysisLabel   = new Label("Analysis and report");
        Label testerLabel     = new Label("👤 Tester");

        dashboardLabel.setOnMouseClicked(e -> root.setCenter(createDashboardPane()));
        tradeLabel.setOnMouseClicked(e -> root.setCenter(new Label("Trade management page...")));
        transactionLabel.setOnMouseClicked(e -> root.setCenter(new Label("Transaction details page...")));
        classifiedLabel.setOnMouseClicked(e -> root.setCenter(new Label("Classified management page...")));
        budgetLabel.setOnMouseClicked(e -> root.setCenter(new BudgetGoalsPane().getView()));  // 调用拆分出的类
        analysisLabel.setOnMouseClicked(e -> root.setCenter(new Label("Analysis and report page...")));
        testerLabel.setOnMouseClicked(e -> root.setCenter(new Label("Tester page...")));

        box.getChildren().addAll(
                dashboardLabel, tradeLabel, transactionLabel,
                classifiedLabel, budgetLabel, analysisLabel, testerLabel
        );
        return box;
    }

    private VBox createDashboardPane() {
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
        series.getData().add(new XYChart.Data<>("Rent", 1100));
        series.getData().add(new XYChart.Data<>("Groceries", 750));
        series.getData().add(new XYChart.Data<>("Utilities", 400));
        series.getData().add(new XYChart.Data<>("Transport", 300));
        series.getData().add(new XYChart.Data<>("Misc", 500));
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

    private VBox createInfoCard(String title, String value, String bgColor, String textColor) {
        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web(textColor));
        titleLabel.setFont(Font.font(14));

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.web(textColor));
        valueLabel.setFont(Font.font(20));

        VBox card = new VBox(titleLabel, valueLabel);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: " + bgColor + ";"
                + "-fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefSize(200, 60);

        return card;
    }

    public static void main(String[] args) {
        launch();
    }
}
