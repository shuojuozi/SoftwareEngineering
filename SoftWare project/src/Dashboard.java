import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Dashboard {

    public VBox getDashboardContent() {
        List<String> categories = new ArrayList<>();
        List<Double> amounts = readDataFromFile("E:\\IdeaProject\\SoftWare project\\src\\data.csv", categories);

        // ====== 四个 Card 样式的统计数据 ======
        HBox statsCards = new HBox(20);
        statsCards.setAlignment(Pos.CENTER);

        statsCards.getChildren().addAll(
                createStatCard("Total Assets", "¥25,000"),
                createStatCard("Monthly Expense", "¥2,300"),
                createStatCard("Monthly Income", "¥6,500"),
                createStatCard("Savings Goal", "45%")
        );

        // ====== 饼图 ======
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Expense Distribution by Category");
        for (int i = 0; i < categories.size(); i++) {
            pieChart.getData().add(new PieChart.Data(categories.get(i), amounts.get(i)));
        }
        pieChart.setPrefWidth(400);

        // ====== 柱状图 ======
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Expenses by Category");
        xAxis.setLabel("Category");
        yAxis.setLabel("Amount");
        barChart.setPrefWidth(400);

        XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Expenses");
        for (int i = 0; i < categories.size(); i++) {
            dataSeries.getData().add(new XYChart.Data<>(categories.get(i), amounts.get(i)));
        }
        barChart.getData().add(dataSeries);

        // ====== 图表并排显示 ======
        HBox chartBox = new HBox(20);
        chartBox.setAlignment(Pos.CENTER);
        chartBox.getChildren().addAll(pieChart, barChart);

        // ====== 总体布局 ======
        VBox content = new VBox(25, new Label("Expense Dashboard"), statsCards, chartBox);
        content.setStyle("-fx-background-color: #eaeef3;");
        content.setPadding(new javafx.geometry.Insets(20));

        return content;
    }

    private VBox createStatCard(String title, String value) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox card = new VBox(10, titleLabel, valueLabel);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(180);
        card.setAlignment(Pos.CENTER);
        return card;
    }

    private List<Double> readDataFromFile(String filePath, List<String> categories) {
        List<Double> amounts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Category")) continue;

                String[] data = line.split(",");
                if (data.length >= 2) {
                    String category = data[0].trim();
                    String moneyStr = data[1].trim();
                    try {
                        double amount = moneyStr.isEmpty() ? 0.0 : Double.parseDouble(moneyStr);
                        categories.add(category);
                        amounts.add(amount);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid money format in line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return amounts;
    }
}
