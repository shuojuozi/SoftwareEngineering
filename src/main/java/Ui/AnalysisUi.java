package Ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import utils.DeepSeek;

import javax.imageio.ImageIO;

import static utils.JsonUtils.getDailyExpensesForMonth;

public class AnalysisUi extends NavigationSuper {

    private BorderPane root;

    public AnalysisUi() {
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
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses");
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        xAxis.setTickLabelRotation(45);
        xAxis.setAnimated(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (in hundred)");
        yAxis.setTickUnit(25);
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(275);
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Financial Trend Over Time");
        lineChart.setPrefSize(1500, 400);

        // 2. 获取数据并按日期排序
        Map<Integer, Double> expensesMap = getDailyExpensesForMonth(UserUi.year, UserUi.month);
        if (expensesMap.isEmpty()) {
            System.out.println("无数据，跳过图表渲染");
        }

        // 按日期排序（确保为升序）
        List<Integer> sortedDates = expensesMap.keySet().stream()
                .sorted()
                .toList();

        // 生成简化的日期字符串（MM-dd 格式）
        List<String> dateStrings = sortedDates.stream()
                .map(dateInt -> {
                    int year = dateInt / 10000;
                    int month = (dateInt % 10000) / 100;
                    int day = dateInt % 100;
                    return LocalDate.of(year, month, day)
                            .format(DateTimeFormatter.ofPattern("MM-dd"));
                })
                .toList();

        // 显式设置 X 轴类别
        Platform.runLater(() -> {
            xAxis.getCategories().clear();
            xAxis.getCategories().addAll(dateStrings);
        });

        // 添加数据点
        Platform.runLater(() -> {
            sortedDates.forEach(dateInt -> {
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

        // 配置横向滚动容器
        ScrollPane scrollPane = new ScrollPane(lineChart);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setStyle("-fx-padding: 10;");
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
        Button aiAnalysisButton = new Button("AI Analysis");

        // 导出PDF按钮事件
        exportPDFButton.setOnAction(event -> exportPDF(lineChart));
        // 导出CSV按钮事件
        exportCSVButton.setOnAction(event -> exportCSV(expensesMap));

        // AI分析按钮事件
        aiAnalysisButton.setOnAction(event -> {
            // 这里我们将触发AI分析
            String aiSuggestion = DeepSeek.analyzeSpendingBehavior(UserUi.year, UserUi.month);
            aiPredictionTextArea.setText(aiSuggestion); // 显示AI建议
        });

        buttonBox.getChildren().addAll(exportPDFButton, exportCSVButton, aiAnalysisButton);
        dashboardLayout.getChildren().add(buttonBox);

        return dashboardLayout;
    }

    private static void exportPDF(LineChart<String, Number> lineChart) {
        try {
            // 创建PDF文档
            Document document = new Document();

            // 使用用户文档目录作为基础路径
            String userHome = System.getProperty("user.home");
            String pdfDirPath = userHome + "/Documents/FinancialReports/pdf/";
            File pdfDir = new File(pdfDirPath);

            // 确保目录存在
            if (!pdfDir.exists() && !pdfDir.mkdirs()) {
                throw new IOException("无法创建目录: " + pdfDir.getAbsolutePath());
            }

            // 生成唯一文件名
            String fileName = "Report_" + System.currentTimeMillis() + ".pdf";
            String filePath = pdfDirPath + fileName;

            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // 添加报告标题
            Paragraph title = new Paragraph("Financial Trend Report\n\n",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // 生成图表图片
            WritableImage image = lineChart.snapshot(new SnapshotParameters(), null);
            File chartImageFile = new File(pdfDirPath + "chart_temp.png");
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", chartImageFile);

            // 添加图片到PDF（自动调整大小）
            Image pdfImage = Image.getInstance(chartImageFile.getAbsolutePath());
            pdfImage.scaleToFit(500, 300); // 限制图片尺寸
            pdfImage.setAlignment(Image.ALIGN_CENTER);
            document.add(pdfImage);

            document.close();
            chartImageFile.delete(); // 删除临时图片

            // 显示完整路径供用户验证
            System.out.println("PDF导出成功，路径: " + new File(filePath).getAbsolutePath());
        } catch (Exception e) {
            System.err.println("PDF导出失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void exportCSV(Map<Integer, Double> expensesMap) {
        try {
            // 使用用户文档目录作为基础路径
            String userHome = System.getProperty("user.home");
            String csvDirPath = userHome + "/Documents/FinancialReports/csv/";
            File csvDir = new File(csvDirPath);

            // 确保目录存在
            if (!csvDir.exists() && !csvDir.mkdirs()) {
                throw new IOException("无法创建目录: " + csvDir.getAbsolutePath());
            }

            // 生成唯一文件名
            String fileName = "Expenses_" + System.currentTimeMillis() + ".csv";
            String filePath = csvDirPath + fileName;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                // 写入CSV头
                writer.write("Date,Amount (USD)\n");

                // 按日期排序写入数据
                expensesMap.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            try {
                                int dateInt = entry.getKey();
                                LocalDate date = LocalDate.of(
                                        dateInt / 10000,
                                        (dateInt % 10000) / 100,
                                        dateInt % 100
                                );
                                writer.write(String.format("%s,%.2f\n",
                                        date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                        entry.getValue()));
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });

                System.out.println("CSV导出成功，路径: " + new File(filePath).getAbsolutePath());
            }
        } catch (IOException | UncheckedIOException e) {
            System.err.println("CSV导出失败: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
