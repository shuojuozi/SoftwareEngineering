package Ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static utils.JsonUtils.*;

public class AnalysisUi extends NavigationSuper {
    private BorderPane root;

    public AnalysisUi() {
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
        VBox dashboardLayout = new VBox(20);
        dashboardLayout.setPadding(new Insets(20));
        dashboardLayout.setAlignment(Pos.TOP_CENTER);

        // 1. Analysis & Report Section
        Label analysisLabel = new Label("Analysis & Report");
        analysisLabel.setFont(new Font(24));
        analysisLabel.setAlignment(Pos.CENTER);
        dashboardLayout.getChildren().add(analysisLabel);

        // 2. Line Chart Section
// 1. 初始化图表组件
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        xAxis.setTickLabelRotation(45); // 标签旋转 45 度
        xAxis.setAnimated(false); // 禁用动画，确保标签立即渲染

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (in hundred)");
        yAxis.setTickUnit(25); // 根据你的截图 Y 轴刻度单位为 25
        yAxis.setAutoRanging(false); // 固定 Y 轴范围
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(275); // 截图显示最大值为 275

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Financial Trend Over Time");
        lineChart.setPrefSize(1500, 400); // 增加宽度，高度减少以适配截图比例

// 2. 获取数据并按日期排序
        Map<Integer, Double> expensesMap = getDailyExpensesForMonth(2025, 3);
        if (expensesMap.isEmpty()) {
            System.out.println("无数据，跳过图表渲染");
            //return;
        }

// 按日期排序（确保为升序）
        List<Integer> sortedDates = expensesMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

// 3. 生成简化的日期字符串（MM-dd 格式）
        List<String> dateStrings = sortedDates.stream()
                .map(dateInt -> {
                    int year = dateInt / 10000;
                    int month = (dateInt % 10000) / 100;
                    int day = dateInt % 100;
                    return LocalDate.of(year, month, day)
                            .format(DateTimeFormatter.ofPattern("MM-dd")); // 格式化为 "10-05"
                })
                .collect(Collectors.toList());

// 显式设置 X 轴类别（必须在 JavaFX 主线程操作）
        Platform.runLater(() -> {
            xAxis.getCategories().clear();
            xAxis.getCategories().addAll(dateStrings);
        });

// 4. 添加数据点（确保日期与类别严格匹配）
        Platform.runLater(() -> {
            sortedDates.forEach(dateInt -> {
                // 生成与 dateStrings 完全一致的日期格式
                String dateStr = LocalDate.of(
                        dateInt / 10000,
                        (dateInt % 10000) / 100,
                        dateInt % 100
                ).format(DateTimeFormatter.ofPattern("MM-dd"));

                double amount = expensesMap.get(dateInt);
                series.getData().add(new XYChart.Data<>(dateStr, amount));
            });

            lineChart.getData().add(series);
        });

// 5. 配置横向滚动容器
        ScrollPane scrollPane = new ScrollPane(lineChart);
        scrollPane.setFitToWidth(false); // 关键：禁用宽度自适应
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // 强制显示横向滚动条
        scrollPane.setStyle("-fx-padding: 10;"); // 增加内边距

// 将图表添加到布局
        VBox chartContainer = new VBox(scrollPane);
        chartContainer.setPadding(new Insets(10));
        dashboardLayout.getChildren().add(chartContainer);
        // 3. AI Prediction Report Section
        TextArea aiPredictionTextArea = new TextArea();
        aiPredictionTextArea.setEditable(false);
        aiPredictionTextArea.setPromptText("AI Prediction Report will be displayed here...");
        aiPredictionTextArea.setWrapText(true);
        dashboardLayout.getChildren().add(aiPredictionTextArea);

        // 4. Export Buttons Section
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        Button exportPDFButton = new Button("Export PDF");
        Button exportCSVButton = new Button("Export CSV");

        // Add action listeners for buttons (currently just print to console)
        exportPDFButton.setOnAction(event -> {
            System.out.println("Export PDF button clicked");
        });
        exportCSVButton.setOnAction(event -> {
            System.out.println("Export CSV button clicked");
        });

        buttonBox.getChildren().addAll(exportPDFButton, exportCSVButton);
        dashboardLayout.getChildren().add(buttonBox);

        return dashboardLayout;
    }


}
