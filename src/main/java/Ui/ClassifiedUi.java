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
import pojo.Transaction;
import utils.JsonUtils;

import java.util.List;


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

        // 1. 获取30天的消费数据
        List<Transaction> transactions = JsonUtils.getTransactionsByMonth(2025, 2); // 修改为2025年4月

        double housing = 0;
        double transport = 0;
        double dining = 0;
        double entertainment = 0;
        double shopping = 0;
        double health = 0;
        double communication = 0;
        double investment = 0;
        double transfer = 0;

        double sum = 0;
        for (Transaction transaction : transactions) {
            sum += transaction.getAmount();
            String category = transaction.getTransactionType();
            if (category.equalsIgnoreCase("\"housing\"")) {
                housing += transaction.getAmount();
            } else if (category.equalsIgnoreCase("\"food and dining\"")) {
                dining += transaction.getAmount();
            } else if (category.equalsIgnoreCase("\"communication\"")) {
                communication += transaction.getAmount();
            } else if (category.equalsIgnoreCase("\"transfer accounts\"")) {
                transfer += transaction.getAmount();
            } else if (category.equalsIgnoreCase("\"entertainment\"")) {
                entertainment += transaction.getAmount();
            } else if (category.equalsIgnoreCase("\"transportation\"")) {
                transport += transaction.getAmount();
            } else if (category.equalsIgnoreCase("\"shopping\"")) {
                shopping += transaction.getAmount();
            } else if (category.equalsIgnoreCase("\"finance and investment\"")) {
                investment += transaction.getAmount();
            } else if (category.equalsIgnoreCase("\"healthcareeducation and training\"")) {
                health += transaction.getAmount();
            }

        }

        // 5. 创建 Bar Chart（使用从 summarizeByBillingCycle 获取的数据）
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Recent Transactions");
        xAxis.setLabel("Category");
        yAxis.setLabel("Amount");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Recent Transactions");
        series.getData().addAll(
                new XYChart.Data<>("Housing", housing),
                new XYChart.Data<>("Dinging", dining),
                new XYChart.Data<>("Entertainment", entertainment),
                new XYChart.Data<>("Transport", transport),
                new XYChart.Data<>("Shopping", shopping),
                new XYChart.Data<>("Health", health),
                new XYChart.Data<>("Communication", communication),
                new XYChart.Data<>("Investment", investment),
                new XYChart.Data<>("Transfer", transfer)
        );
        barChart.getData().add(series);

// 6. 创建 Pie Chart
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Spending by Category");
        pieChart.getData().addAll(
                new PieChart.Data("Rent", housing),
                new PieChart.Data("Dinging", dining),
                new PieChart.Data("Entertainment", entertainment),
                new PieChart.Data("Transport", transport),
                new PieChart.Data("Shopping", shopping),
                new PieChart.Data("Health", health),
                new PieChart.Data("Communication", communication),
                new PieChart.Data("Investment", investment),
                new PieChart.Data("Transfer", transfer)
        );

// 7. 放置图表
        HBox chartsBox = new HBox(20, barChart, pieChart);
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



}