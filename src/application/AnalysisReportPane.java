package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.application.Platform;


public class AnalysisReportPane {

    public Node getView() {
        // 主内容容器（可滚动区域内部）
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #f0f2f5;");

        // 页面各部分内容
        VBox chartCard = createChartCard();
        VBox aiReportCard = createAiReportCard();  // 会有多行内容
        VBox periodicCard = createPeriodicReportCard();

        mainContainer.getChildren().addAll(
                chartCard, aiReportCard, periodicCard
        );

        // 放入ScrollPane以实现滚动
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(mainContainer);
        scrollPane.setFitToWidth(true); // 横向适配窗口
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background: #f0f2f5;"); // 防止边框出现白边

        scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setVvalue(0);
        });

        return scrollPane;
    }

    private VBox createChartCard() {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.setStyle(getCardStyle());

        Label chartTitle = new Label("Spending and Income Trends");
        chartTitle.setFont(Font.font("Arial", 18));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setMinSize(600, 300);
        lineChart.setLegendVisible(true);

        XYChart.Series<String, Number> spendingSeries = new XYChart.Series<>();
        spendingSeries.setName("Spending");
        spendingSeries.getData().add(new XYChart.Data<>("Jan", 300));
        spendingSeries.getData().add(new XYChart.Data<>("Feb", 400));
        spendingSeries.getData().add(new XYChart.Data<>("Mar", 350));
        spendingSeries.getData().add(new XYChart.Data<>("Apr", 450));
        spendingSeries.getData().add(new XYChart.Data<>("May", 480));
        spendingSeries.getData().add(new XYChart.Data<>("Jun", 520));

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        incomeSeries.getData().add(new XYChart.Data<>("Jan", 500));
        incomeSeries.getData().add(new XYChart.Data<>("Feb", 530));
        incomeSeries.getData().add(new XYChart.Data<>("Mar", 550));
        incomeSeries.getData().add(new XYChart.Data<>("Apr", 580));
        incomeSeries.getData().add(new XYChart.Data<>("May", 600));
        incomeSeries.getData().add(new XYChart.Data<>("Jun", 650));

        lineChart.getData().addAll(spendingSeries, incomeSeries);

        card.getChildren().addAll(chartTitle, lineChart);
        return card;
    }

    private VBox createAiReportCard() {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle(getCardStyle());

        Label aiLabel = new Label("AI Prediction Report");
        aiLabel.setFont(Font.font("Arial", 18));

        Label aiContent = new Label(
                "AI model predicts a steady increase in savings over the next quarter due to:\n" +
                        "- Decreasing spend on non-essential items\n" +
                        "- Increase in income from potential side projects\n" +
                        "- Reduced cost in fixed subscriptions\n" +
                        "- Improved monthly budgeting behavior\n" +
                        "- AI-assisted spending categorization and smarter financial planning suggestions\n" +
                        "- External income sources such as freelancing\n" +
                        "- More conscious shopping behavior driven by alert thresholds"
        );
        aiContent.setWrapText(true);

        card.getChildren().addAll(aiLabel, aiContent);
        return card;
    }

    private VBox createPeriodicReportCard() {
        VBox card = new VBox(15);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle(getCardStyle());

        Label periodicLabel = new Label("Periodic Reports");
        periodicLabel.setFont(Font.font("Arial", 18));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button pdfButton = new Button("Export PDF");
        pdfButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");

        Button csvButton = new Button("Export CSV");
        csvButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

        buttonBox.getChildren().addAll(pdfButton, csvButton);

        card.getChildren().addAll(periodicLabel, buttonBox);
        return card;
    }

    private String getCardStyle() {
        return "-fx-background-color: #ffffff; "
                + "-fx-background-radius: 8; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0.5, 0, 0);";
    }
}
