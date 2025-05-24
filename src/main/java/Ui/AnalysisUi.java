package Ui;

import utils.ReportUtils;
import utils.DateContext;
import utils.JsonUtils;
import pojo.Transaction;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.io.File;
import java.io.IOException;

public class AnalysisUi {

    public static VBox createDashboardPane() {
        VBox dashboardLayout = new VBox(20);
        dashboardLayout.setPadding(new Insets(20));
        dashboardLayout.setAlignment(Pos.TOP_CENTER);

        // 1. Analysis & Report Section
        Label analysisLabel = new Label("Analysis & Report");
        analysisLabel.setFont(new Font(24));
        dashboardLayout.getChildren().add(analysisLabel);

        // 2. Line Chart Section
        NumberAxis xAxis = new NumberAxis(1, 31, 1);
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day of Month");
        yAxis.setLabel("Amount");
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        int y = DateContext.getYear();
        int m = DateContext.getMonth();
        lineChart.setTitle(String.format("%d-%02d Daily Spending Trend", y, m));

        List<Transaction> transactions = JsonUtils.getTransactionsByMonth(y, m);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Daily Expenses");

        // Initialize daily sum map
        Map<Integer, Double> dailySum = new TreeMap<>();
        for (int day = 1; day <= 31; day++) {
            dailySum.put(day, 0.0);
        }

        // Aggregate transaction amounts by day
        for (Transaction t : transactions) {
            try {
                LocalDateTime date = LocalDateTime.parse(t.getTransactionTime().replace("\"", ""),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                int day = date.getDayOfMonth();
                dailySum.merge(day, t.getAmount(), Double::sum);
            } catch (Exception e) {
                System.err.println("Error parsing date: " + t.getTransactionTime());
            }
        }

        for (Map.Entry<Integer, Double> entry : dailySum.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        lineChart.getData().add(series);
        dashboardLayout.getChildren().add(lineChart);

        // 3. Text summary section
        TextArea aiPredictionTextArea = new TextArea();
        aiPredictionTextArea.setEditable(false);
        aiPredictionTextArea.setWrapText(true);
        aiPredictionTextArea.setPrefRowCount(5);

        double totalSpent = dailySum.values().stream().mapToDouble(Double::doubleValue).sum();
        double avgDaily = totalSpent / dailySum.size();

        String analysis = String.format("""
                Financial Analysis Summary:
                • Total Transactions: %d
                • Total Spent: ¥%.2f
                • Average Daily Spending: ¥%.2f
                • Number of Active Days: %d
                """,
                transactions.size(),
                totalSpent,
                avgDaily,
                dailySum.size()
        );

        aiPredictionTextArea.setText(analysis);
        dashboardLayout.getChildren().add(aiPredictionTextArea);

        // 4. Export buttons section
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        Button exportPDFButton = new Button("Export PDF");
        Button exportCSVButton = new Button("Export CSV");

        // PDF export logic
        exportPDFButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF Report");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName(
                    String.format("financial_report_%d_%02d.pdf", y, m)
            );
            File file = fileChooser.showSaveDialog(exportPDFButton.getScene().getWindow());
            if (file != null) {
                try {
                    ReportUtils.askAndExportPdf("analysis", file);
                } catch (Exception e) {
                    System.err.println("Failed to export PDF: " + e.getMessage());
                }
            }
        });

        // CSV export logic
        exportCSVButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save CSV File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName(
                    String.format("transactions_%d_%02d.csv", y, m)
            );
            File file = fileChooser.showSaveDialog(exportCSVButton.getScene().getWindow());
            if (file != null) {
                try {
                    List<Transaction> monthlyData = JsonUtils.getTransactionsByMonth(y, m);
                    JsonUtils.exportTransactions(monthlyData, file.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("Failed to export CSV: " + e.getMessage());
                }
            }
        });

        buttonBox.getChildren().addAll(exportPDFButton, exportCSVButton);
        dashboardLayout.getChildren().add(buttonBox);

        return dashboardLayout;
    }
}
