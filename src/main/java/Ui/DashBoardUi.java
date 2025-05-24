package Ui;

import javafx.beans.property.IntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import pojo.Transaction;
import utils.DateContext;
import utils.FinanceContext;
import utils.JsonUtils;

import java.util.List;
import java.util.Map;

public class DashBoardUi {

    // Category color mapping
    private static final Map<String, String> COLOR = Map.of(
            "housing", "#d32f2f",
            "dining", "#f9a825",
            "entertainment", "#4caf50",
            "transport", "#03a9f4",
            "shopping", "#3f51b5",
            "health", "#bb86fc",
            "education", "#e91e63",
            "communication", "#9e9e9e",
            "investment", "#ff9800",
            "transfer", "#795548"
    );

    public static VBox createDashboardPane(int year, int month) {

        // Top control area: year/month selectors + load button
        HBox topCtrl = new HBox(10);
        topCtrl.setAlignment(Pos.CENTER_LEFT);
        IntegerProperty yProp = DateContext.yearProperty();
        IntegerProperty mProp = DateContext.monthProperty();

        ComboBox<Integer> yearBox = new ComboBox<>();
        ComboBox<Integer> monthBox = new ComboBox<>();
        for (int y = 2020; y <= 2030; y++) yearBox.getItems().add(y);
        for (int m = 1; m <= 12; m++) monthBox.getItems().add(m);
        yearBox.setValue(year);
        monthBox.setValue(month);

        Button loadBtn = new Button("Load");
        loadBtn.setOnAction(e -> {
            DateContext.set(yearBox.getValue(), monthBox.getValue());
            NavigationSuper.root.setCenter(
                    createDashboardPane(yearBox.getValue(), monthBox.getValue()));
        });
        topCtrl.getChildren().addAll(new Label("Year:"), yearBox,
                new Label("Month:"), monthBox, loadBtn);

        // Load transaction data and classify expenses by type
        List<Transaction> txs = JsonUtils.getTransactionsByMonth(year, month);
        double housing = 0, transport = 0, dining = 0, entertainment = 0, shopping = 0,
                health = 0, education = 0, communication = 0, investment = 0, transfer = 0;
        double sum = 0;
        for (Transaction t : txs) {
            double amt = t.getAmount();
            sum += amt;
            switch (t.getTransactionType().replace("\"", "").toLowerCase()) {
                case "housing" -> housing += amt;
                case "transportation" -> transport += amt;
                case "food and dining" -> dining += amt;
                case "entertainment" -> entertainment += amt;
                case "shopping" -> shopping += amt;
                case "healthcare" -> health += amt;
                case "education and training" -> education += amt;
                case "communication" -> communication += amt;
                case "finance and investment" -> investment += amt;
                case "transfer accounts" -> transfer += amt;
            }
        }

        // Financial summary
        double totalAssets = FinanceContext.getTotalAssets();
        double monthlyIncome = FinanceContext.getMonthlyIncome();
        double savingsGoal = FinanceContext.getSavingsGoal();
        double goalProgress = (totalAssets + monthlyIncome - sum) / savingsGoal;

        HBox summary = new HBox(20);
        summary.setPadding(new Insets(20));
        summary.setAlignment(Pos.CENTER);
        summary.getChildren().addAll(
                card("Total Assets", String.format("¥%,.2f", totalAssets), "#cce5ff", "#004085"),
                card("Monthly Expense", String.format("¥%,.2f", sum), "#f8d7da", "#721c24"),
                card("Monthly Income", String.format("¥%,.2f", monthlyIncome), "#d4edda", "#155724"),
                card("Savings Goal", String.format("¥%,.2f", savingsGoal), "#f8d7da", "#721c24"),
                card("Goal Progress", String.format("%.1f%%", goalProgress * 100), "#fff3cd", "#856404")
        );

        // Bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bar = new BarChart<>(xAxis, yAxis);
        bar.setLegendVisible(false);
        bar.setTitle("Spending by Category");
        xAxis.setLabel("Category");
        yAxis.setLabel("Amount");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        List<XYChart.Data<String, Number>> bars = List.of(
                bar("Housing", housing),
                bar("Dining", dining),
                bar("Entertainment", entertainment),
                bar("Transport", transport),
                bar("Shopping", shopping),
                bar("Health", health),
                bar("Education", education),
                bar("Communication", communication),
                bar("Investment", investment),
                bar("Transfer", transfer)
        );
        series.getData().addAll(bars);
        bar.getData().add(series);

        // Pie chart
        PieChart.Data[] slices = {
                slice("Housing", housing),
                slice("Dining", dining),
                slice("Entertainment", entertainment),
                slice("Transport", transport),
                slice("Shopping", shopping),
                slice("Health", health),
                slice("Education", education),
                slice("Communication", communication),
                slice("Investment", investment),
                slice("Transfer", transfer)
        };
        PieChart pie = new PieChart();
        pie.setTitle("Spending by Category");
        pie.getData().addAll(slices);

        // Combine charts
        HBox charts = new HBox(20, bar, pie);
        charts.setPadding(new Insets(20));
        charts.setAlignment(Pos.CENTER);

        // Root layout
        VBox root = new VBox(10, topCtrl, summary, charts);
        root.setAlignment(Pos.TOP_CENTER);

        // Add listeners for dynamic financial data update
        FinanceContext.totalAssetsProperty().addListener((o, ov, nv) -> refresh());
        FinanceContext.savingsGoalProperty().addListener((o, ov, nv) -> refresh());
        FinanceContext.monthlyIncomeProperty().addListener((o, ov, nv) -> refresh());

        return root;
    }

    // Refresh the dashboard view when data changes
    private static void refresh() {
        NavigationSuper.root.setCenter(
                createDashboardPane(DateContext.getYear(), DateContext.getMonth()));
    }

    // Create a summary card
    private static VBox card(String title, String value, String bg, String fg) {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:10;-fx-padding:10;");
        Label t = new Label(title);
        t.setFont(new Font(14));
        t.setStyle("-fx-text-fill:" + fg + ";");
        Label v = new Label(value);
        v.setFont(new Font(20));
        v.setStyle("-fx-text-fill:" + fg + ";");
        box.getChildren().addAll(t, v);
        return box;
    }

    // Create a bar chart data entry with color
    private static XYChart.Data<String, Number> bar(String name, double val) {
        XYChart.Data<String, Number> d = new XYChart.Data<>(name, val);
        colorBar(d);
        return d;
    }

    // Apply color to bar chart based on category
    private static void colorBar(XYChart.Data<String, Number> d) {
        d.nodeProperty().addListener((o, oldV, node) -> {
            if (node != null) {
                String key = d.getXValue().toLowerCase();
                node.setStyle("-fx-bar-fill:" + COLOR.getOrDefault(key, "#666") + ";");
            }
        });
    }

    // Create pie chart slice
    private static PieChart.Data slice(String name, double val) {
        PieChart.Data d = new PieChart.Data(name, val);
        colorSlice(d);
        return d;
    }

    // Apply color to pie chart slice
    private static void colorSlice(PieChart.Data s) {
        s.nodeProperty().addListener((o, oldV, node) -> {
            if (node != null) {
                String key = s.getName().toLowerCase();
                node.setStyle("-fx-pie-color:" + COLOR.getOrDefault(key, "#666") + ";");
            }
        });
    }
}
