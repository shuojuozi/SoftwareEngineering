package Ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

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
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setTickUnit(3); // Each unit represents 3 days
        yAxis.setLabel("Amount (in hundred)");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Financial Trend Over Time");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Expenses");
        // Example data, replace with actual data as needed
        series.getData().add(new XYChart.Data<>(1, 200)); // Day 1
        series.getData().add(new XYChart.Data<>(4, 500)); // Day 4
        series.getData().add(new XYChart.Data<>(7, 300)); // Day 7
        series.getData().add(new XYChart.Data<>(10, 400)); // Day 10
        series.getData().add(new XYChart.Data<>(13, 600)); // Day 13
        lineChart.getData().add(series);
        dashboardLayout.getChildren().add(lineChart);

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
