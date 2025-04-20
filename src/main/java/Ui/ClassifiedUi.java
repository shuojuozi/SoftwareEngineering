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


public class ClassifiedUi extends NavigationSuper {
    private BorderPane root;

    public ClassifiedUi() {
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

        // Create a container for the charts
        HBox chartsBox = new HBox(20, pieChart, barChart);
        chartsBox.setPadding(new Insets(20));
        chartsBox.setAlignment(Pos.CENTER);

        // Add Category button
        Button addCategoryButton = new Button("Add Category");
        addCategoryButton.setOnAction(event -> {
            // Add your event handling code here
            System.out.println("Add Category button clicked");
        });

        VBox dashboardLayout = new VBox(chartsBox, addCategoryButton);
        dashboardLayout.setAlignment(Pos.CENTER);
        dashboardLayout.setSpacing(20); // Add some spacing between elements

        return dashboardLayout;
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