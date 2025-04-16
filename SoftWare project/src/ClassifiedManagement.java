import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.chart.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;

public class ClassifiedManagement {

    private List<String> categories = new ArrayList<>();
    private List<Double> amounts = new ArrayList<>();
    private List<String> times = new ArrayList<>();
    private String selectedTimePeriod = "月度";

    public ClassifiedManagement() {
        readDataFromFile("E:\\IdeaProject\\SoftWare project\\src\\data.csv");
    }

    public VBox getClassifiedManagementContent() {
        Label title = new Label("Expense Category Management");

        // 时间选择
        ToggleGroup timeGroup = new ToggleGroup();
        ToggleButton monthBtn = new ToggleButton("月度");
        ToggleButton quarterBtn = new ToggleButton("季度");
        ToggleButton yearBtn = new ToggleButton("年度");
        monthBtn.setToggleGroup(timeGroup);
        quarterBtn.setToggleGroup(timeGroup);
        yearBtn.setToggleGroup(timeGroup);
        monthBtn.setSelected(true);

        monthBtn.setOnAction(event -> updateChartData("月度"));
        quarterBtn.setOnAction(event -> updateChartData("季度"));
        yearBtn.setOnAction(event -> updateChartData("年度"));

        HBox timeBox = new HBox(10, monthBtn, quarterBtn, yearBtn);
        timeBox.setAlignment(Pos.CENTER);

        // 图表控件初始化
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Expense Distribution by Category");

        CategoryAxis lineXAxis = new CategoryAxis();
        NumberAxis lineYAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(lineXAxis, lineYAxis);
        lineChart.setTitle("Spending Trend Over Time");
        lineXAxis.setLabel("Time");
        lineYAxis.setLabel("Total Amount");

        // 图表容器
        VBox chartBox = new VBox(pieChart); // 默认饼图
        chartBox.setSpacing(10);

        // 视图选择器
        ComboBox<String> viewSelector = new ComboBox<>();
        viewSelector.getItems().addAll("种类占比", "变化趋势");
        viewSelector.setValue("种类占比");

        viewSelector.setOnAction(event -> {
            String selected = viewSelector.getValue();
            chartBox.getChildren().clear();  // 清空原图表

            if (selected.equals("种类占比")) {
                PieChart newPieChart = new PieChart();
                newPieChart.setTitle("Expense Distribution by Category");
                updatePieChart(newPieChart);
                chartBox.getChildren().add(newPieChart);
            } else {
                updateLineChart(lineChart);
                chartBox.getChildren().add(lineChart);
            }
        });


        // 添加支出按钮
        Button addCategoryButton = new Button("Add Category");
        addCategoryButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-padding: 10;");
        addCategoryButton.setOnAction(event -> handleAddCategory(pieChart, lineChart, chartBox, viewSelector.getValue()));

        // 编辑删除按钮（可后续补充逻辑）
        Button editCategoryButton = new Button("Edit Category");
        Button deleteCategoryButton = new Button("Delete Category");

        HBox bottomBox = new HBox(20, editCategoryButton, deleteCategoryButton);
        bottomBox.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(20,
                title,
                timeBox,
                new Label("查看方式："),
                viewSelector,
                chartBox,
                addCategoryButton,
                bottomBox
        );
        content.setStyle("-fx-background-color: #f0f8ff;");
        content.setPadding(new Insets(20));

        // 初始化图表
        updatePieChart(pieChart);

        return content;
    }

    private void updateChartData(String selectedTimePeriod) {
        this.selectedTimePeriod = selectedTimePeriod;
        System.out.println("Selected Time Period: " + selectedTimePeriod);
        // 可添加时间维度筛选逻辑
    }

    private void updatePieChart(PieChart pieChart) {
        pieChart.getData().clear();
        Map<String, Double> categoryMap = new LinkedHashMap<>();
        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            double amount = amounts.get(i);
            categoryMap.put(category, categoryMap.getOrDefault(category, 0.0) + amount);
        }
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            pieChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
    }

    private void updateLineChart(LineChart<String, Number> lineChart) {
        lineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Expense");

        Map<String, Double> timeAmountMap = new TreeMap<>();
        for (int i = 0; i < times.size(); i++) {
            String time = times.get(i);
            double amount = amounts.get(i);
            timeAmountMap.put(time, timeAmountMap.getOrDefault(time, 0.0) + amount);
        }

        for (String time : timeAmountMap.keySet()) {
            series.getData().add(new XYChart.Data<>(time, timeAmountMap.get(time)));
        }

        lineChart.getData().add(series);
    }

    private void handleAddCategory(PieChart pieChart, LineChart<String, Number> lineChart, VBox chartBox, String currentView) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Category");

        TextField categoryField = new TextField();
        categoryField.setPromptText("Enter Category Name");
        TextField amountField = new TextField();
        amountField.setPromptText("Enter Amount");
        TextField timeField = new TextField();
        timeField.setPromptText("Enter Time (e.g., 2024-04)");

        Button addButton = new Button("Add");
        addButton.setOnAction(event -> {
            String categoryName = categoryField.getText();
            String time = timeField.getText();
            try {
                double amount = Double.parseDouble(amountField.getText());

                categories.add(categoryName);
                amounts.add(amount);
                times.add(time);
                appendDataToFile("E:\\IdeaProject\\SoftWare project\\src\\data.csv", categoryName, amount, time);
                System.out.println("Added: " + categoryName + ", " + amount + ", " + time);

                if (currentView.equals("种类占比")) {
                    updatePieChart(pieChart);
                    chartBox.getChildren().setAll(pieChart);
                } else {
                    updateLineChart(lineChart);
                    chartBox.getChildren().setAll(lineChart);
                }

                dialog.close();
            } catch (NumberFormatException e) {
                System.err.println("Invalid amount entered.");
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> dialog.close());

        VBox vbox = new VBox(10, categoryField, amountField, timeField, addButton, cancelButton);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f0f8ff;");
        dialog.setScene(new Scene(vbox, 300, 250));
        dialog.showAndWait();
    }

    private void appendDataToFile(String filePath, String category, double amount, String time) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(category + "," + amount + "," + time + "\n");
            System.out.println("Written to file: " + category + "," + amount + "," + time);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readDataFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Category")) continue;
                String[] data = line.split(",");
                if (data.length >= 3) {
                    String category = data[0].trim();
                    String moneyStr = data[1].trim();
                    String time = data[2].trim();
                    try {
                        double amount = moneyStr.isEmpty() ? 0.0 : Double.parseDouble(moneyStr);
                        categories.add(category);
                        amounts.add(amount);
                        times.add(time);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid amount in line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
