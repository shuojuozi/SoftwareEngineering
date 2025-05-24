package Ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import pojo.Transaction;
import utils.JsonUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Updated ClassifiedUi — Removed hardcoded month & supports unquoted category fields.
 */
public class ClassifiedUi extends NavigationSuper {
    public static VBox createDashboardPane() {
        LocalDate now = LocalDate.now();

        // Retrieve transaction data from transactionData.json
        List<Transaction> txs = JsonUtils.readTransactionsFromClasspath("transactionData.json");

        // Classify and accumulate category amounts
        Map<String, Double> catSum = new HashMap<>();
        double total = 0;
        for (Transaction t : txs) {
            String c = normalize(t.getTransactionType());
            double amount = t.getAmount();
            catSum.merge(c, amount, Double::sum);
            total += amount;
        }

        // Get spending for each category
        double housing = catSum.getOrDefault("housing", 0.0);
        double dining = catSum.getOrDefault("food and dining", 0.0);
        double entertainment = catSum.getOrDefault("entertainment", 0.0);
        double transport = catSum.getOrDefault("transportation", 0.0);
        double shopping = catSum.getOrDefault("shopping", 0.0);
        double health = catSum.getOrDefault("healthcare", 0.0);
        double education = catSum.getOrDefault("education and training", 0.0);
        double communication = catSum.getOrDefault("communication", 0.0);
        double investment = catSum.getOrDefault("finance and investment", 0.0);
        double transfer = catSum.getOrDefault("transfer accounts", 0.0);

        // Bar chart for category-wise spending
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> bar = new BarChart<>(x, y);
        bar.setTitle("Spending – " + now.getMonth());
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.getData().addAll(
                new XYChart.Data<>("Housing", housing),
                new XYChart.Data<>("Dining", dining),
                new XYChart.Data<>("Entertainment", entertainment),
                new XYChart.Data<>("Transport", transport),
                new XYChart.Data<>("Shopping", shopping),
                new XYChart.Data<>("Health", health),
                new XYChart.Data<>("Education", education),
                new XYChart.Data<>("Communication", communication),
                new XYChart.Data<>("Investment", investment),
                new XYChart.Data<>("Transfer", transfer));
        bar.getData().add(s);

        // Pie chart for category-wise spending
        PieChart pie = new PieChart();
        pie.setTitle("Spending by Category");
        pie.getData().addAll(
                new PieChart.Data("Housing", housing),
                new PieChart.Data("Dining", dining),
                new PieChart.Data("Entertainment", entertainment),
                new PieChart.Data("Transport", transport),
                new PieChart.Data("Shopping", shopping),
                new PieChart.Data("Health", health),
                new PieChart.Data("Communication", communication),
                new PieChart.Data("Investment", investment),
                new PieChart.Data("Transfer", transfer));

        // Layout
        HBox charts = new HBox(20, bar, pie);
        charts.setPadding(new Insets(20));
        charts.setAlignment(Pos.CENTER);

        Button addCat = new Button("Add Category");
        VBox box = new VBox(charts, addCat);
        box.setAlignment(Pos.CENTER);
        box.setSpacing(20);

        return box;
    }

    // Normalize category strings (remove quotes, trim, lowercase)
    private static String normalize(String s) {
        return s == null ? "" : s.replace("\"", "").toLowerCase().trim();
    }
}
