package Ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TradeUi extends Ui.NavigationSuper {

    private BorderPane root;

    public TradeUi() {
        // 初始化其他组件
        root = new BorderPane();
        root.setCenter(createDashboardPane());
        root.setRight(createTradeManagementPage());
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


    public VBox createDashboardPane() {
        HBox summaryBox = new HBox(20);
        summaryBox.setPadding(new Insets(20));
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.getChildren().addAll(
                createInfoCard("Total Assets", "$120,500", "#cce5ff", "#004085"),
                createInfoCard("Monthly Expense", "$5,200", "#f8d7da", "#721c24"),
                createInfoCard("Monthly Income", "$7,300", "#d4edda", "#155724"),
                createInfoCard("Savings Goal Progress", "56%", "#fff3cd", "#856404")
        );

        // Bar Chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Recent Transactions");
        xAxis.setLabel("Category");
        yAxis.setLabel("Amount");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Recent Transactions");
        series.getData().add(new XYChart.Data<>("Rent", 1100));
        series.getData().add(new XYChart.Data<>("Groceries", 750));
        series.getData().add(new XYChart.Data<>("Utilities", 400));
        series.getData().add(new XYChart.Data<>("Transport", 300));
        series.getData().add(new XYChart.Data<>("Misc", 500));
        barChart.getData().add(series);

        // Pie Chart
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Spending by Category");
        pieChart.getData().addAll(
                new PieChart.Data("Rent", 35),
                new PieChart.Data("Groceries", 25),
                new PieChart.Data("Utilities", 10),
                new PieChart.Data("Transport", 10),
                new PieChart.Data("Misc", 20)
        );

        HBox chartsBox = new HBox(20, barChart, pieChart);
        chartsBox.setPadding(new Insets(20));
        chartsBox.setAlignment(Pos.CENTER);
        return new VBox(summaryBox, chartsBox);
    }

    public static VBox createTradeManagementPage() {
        VBox mainContent = new VBox();
        mainContent.setPadding(new Insets(20));
        mainContent.setSpacing(20);

        // 第一个容器：Add Transaction Manually
        VBox addTransactionContainer = new VBox(5); // 减小Spacing，压缩容器
        addTransactionContainer.setPadding(new Insets(10)); // 减小Padding，压缩容器
        addTransactionContainer.setStyle("-fx-background-color: #f0f0f0; -fx-border-radius: 5;");
        Label addTransactionLabel = new Label("Add Transaction Manually");
        addTransactionLabel.setFont(new Font(18));
        addTransactionLabel.setStyle("-fx-font-weight: bold;");
        TextField dateField = new TextField("year-month-day");
        dateField.setPromptText("Date");
        TextField amountField = new TextField("Amount");
        amountField.setPromptText("Amount");
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("¥", "$", "£");
        categoryCombo.setValue("Category");
        TextField remarksField = new TextField("Remarks");
        Button addButton = new Button("Add Transaction");
        addButton.setStyle("-fx-background-color: blue; -fx-text-fill: white;");
        addTransactionContainer.getChildren().addAll(addTransactionLabel, dateField, amountField, categoryCombo, remarksField, addButton);

        // 第二个容器：Import Transactions
        VBox importTransactionsContainer = new VBox(5); // 减小Spacing值使得容器更加紧凑
        importTransactionsContainer.setPadding(new Insets(10)); // 减小Padding
        Label importTransactionsLabel = new Label("Import Transactions");
        importTransactionsLabel.setFont(new Font(18));
        importTransactionsLabel.setStyle("-fx-font-weight: bold;");
        Button chooseFileButton = new Button("Choose File");
        chooseFileButton.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
        TextField chooseFileField = new TextField(); // 新增文本框
        chooseFileField.setPromptText("File path");
        Button importRecordsButton = new Button("Import Records");
        importRecordsButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        TextField importRecordsField = new TextField(); // 新增文本框
        importRecordsField.setPromptText("Additional info");
        importTransactionsContainer.getChildren().addAll(importTransactionsLabel, chooseFileButton, chooseFileField, importRecordsButton, importRecordsField);

        // 第三个容器：Transaction List (更改为使用 TableView)
        VBox transactionListContainer = new VBox(15); // 增加Spacing，使内容更舒适
        transactionListContainer.setPadding(new Insets(20)); // 增加Padding来调整容器大小
        Label transactionListLabel = new Label("Transaction List");
        transactionListLabel.setFont(new Font(18));
        transactionListLabel.setStyle("-fx-font-weight: bold;");
        TextField searchField = new TextField("Search transactions...");
        Button searchButton = new Button("Search");

        // 创建 TableView 用于显示交易详情
        TableView<Transaction> transactionTable = new TableView<>();

        // 创建并添加表格列
        TableColumn<Transaction, String> dateColumn = new TableColumn<>("Date");
        TableColumn<Transaction, String> amountColumn = new TableColumn<>("Amount");
        TableColumn<Transaction, String> categoryColumn = new TableColumn<>("Category");
        TableColumn<Transaction, String> remarksColumn = new TableColumn<>("Remarks");

        // 设置每一列的 cellValueFactory，指明如何从 Transaction 中获取数据
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        remarksColumn.setCellValueFactory(cellData -> cellData.getValue().remarksProperty());

        transactionTable.getColumns().addAll(dateColumn, amountColumn, categoryColumn, remarksColumn);

        // 添加一些示例数据
        ObservableList<Transaction> transactionData = FXCollections.observableArrayList(
                new Transaction("2025-04-16", "$100", "Food", "Lunch with friends"),
                new Transaction("2025-04-15", "£50", "Transportation", "Taxi fare"),
                new Transaction("2025-04-16", "¥500", "Food", "Dinner with friends")
        );

        transactionTable.setItems(transactionData);

        // 设置 TableView 的高度限制
        transactionTable.setMaxHeight(350);

        transactionListContainer.getChildren().addAll(transactionListLabel, searchField, searchButton, transactionTable);

        //```java
        // 将所有容器组合在一起
        mainContent.getChildren().addAll(addTransactionContainer, importTransactionsContainer, transactionListContainer);

        return mainContent;
    }

    // 处理交易数据的 Transaction 类
    public static class Transaction {
        private final SimpleStringProperty date;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty category;
        private final SimpleStringProperty remarks;

        public Transaction(String date, String amount, String category, String remarks) {
            this.date = new SimpleStringProperty(date);
            this.amount = new SimpleStringProperty(amount);
            this.category = new SimpleStringProperty(category);
            this.remarks = new SimpleStringProperty(remarks);
        }

        public String getDate() {
            return date.get();
        }

        public SimpleStringProperty dateProperty() {
            return date;
        }

        public String getAmount() {
            return amount.get();
        }

        public SimpleStringProperty amountProperty() {
            return amount;
        }

        public String getCategory() {
            return category.get();
        }

        public SimpleStringProperty categoryProperty() {
            return category;
        }

        public String getRemarks() {
            return remarks.get();
        }

        public SimpleStringProperty remarksProperty() {
            return remarks;
        }
    }

    public HBox createInfoCard(String title, String value, String bgColor, String textColor) {
        HBox infoCard = new HBox(20);
        infoCard.setStyle("-fx-background-color: " + bgColor + "; -fx-border-radius: 5;");
        infoCard.setPadding(new Insets(15));
        infoCard.setAlignment(Pos.CENTER);

        VBox textBox = new VBox(5);
        textBox.setStyle("-fx-text-fill: " + textColor + ";");
        Label titleLabel = new Label(title);
        titleLabel.setFont(new Font(16));
        Label valueLabel = new Label(value);
        valueLabel.setFont(new Font(18));
        valueLabel.setStyle("-fx-font-weight: bold;");

        textBox.getChildren().addAll(titleLabel, valueLabel);
        infoCard.getChildren().add(textBox);

        return infoCard;
    }


}
