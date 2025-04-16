package Ui;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TradeUi extends Application {
    private BorderPane root;

    @Override
    public void start(Stage primaryStage) {
        // 创建主布局
        root = new BorderPane();
        // 添加侧边栏
        root.setLeft(createSidebar());
        // 初始化默认显示交易页
        root.setCenter(createTradeManagementPage());
        // 创建场景并设置舞台
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Transaction Details");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // 创建侧边栏
    private VBox createSidebar() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #f8f9fa;");

        Label dashboardLabel = new Label("📊 Dashboard");
        Label tradeLabel = new Label("Trade management");
        Label transactionLabel = new Label("Transaction details");
        Label classifiedLabel = new Label("Classified management of expenditure");
        Label budgetLabel = new Label("Budgeting and savings goals");
        Label analysisLabel = new Label("Analysis and report");
        Label testerLabel = new Label("👤 Tester");

        dashboardLabel.setOnMouseClicked(e -> root.setCenter(new Label("Dashboard page...")));
        tradeLabel.setOnMouseClicked(e -> root.setCenter(createTradeManagementPage())); // 点击 Trade Management
        classifiedLabel.setOnMouseClicked(e -> root.setCenter(new Label("Classified management page...")));
        budgetLabel.setOnMouseClicked(e -> root.setCenter(new Label("Budgeting and savings goals page...")));
        analysisLabel.setOnMouseClicked(e -> root.setCenter(new Label("Analysis and report page...")));
        testerLabel.setOnMouseClicked(e -> root.setCenter(new Label("Tester page...")));

        box.getChildren().addAll(
                dashboardLabel, tradeLabel, transactionLabel,
                classifiedLabel, budgetLabel, analysisLabel, testerLabel
        );
        return box;
    }

    // 创建 Trade Management 页面
    private VBox createTradeManagementPage() {
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
        TableColumn<Transaction, String> actionsColumn = new TableColumn<>("Actions");

        // 设置每一列的 cellValueFactory，指明如何从 Transaction 中获取数据
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        remarksColumn.setCellValueFactory(cellData -> cellData.getValue().remarksProperty());

        transactionTable.getColumns().addAll(dateColumn, amountColumn, categoryColumn, remarksColumn, actionsColumn);

        // 添加一些示例数据
        ObservableList<Transaction> transactionData = FXCollections.observableArrayList(
                new Transaction("2025-04-16", "$100", "Food", "Lunch with friends"),
                new Transaction("2025-04-15", "£50", "Transportation", "Taxi fare"),
                new Transaction("2025-04-16", "¥500", "Food", "Dinner with friends")
        );

        transactionTable.setItems(transactionData);

        // 设置 TableView 的高度限制
        transactionTable.setMaxHeight(350);

        // 将 TableView 添加到容器中
        transactionListContainer.getChildren().addAll(transactionListLabel, searchField, searchButton, transactionTable);

        // 将所有部分添加到主内容区域
        mainContent.getChildren().addAll(addTransactionContainer, importTransactionsContainer, transactionListContainer);

        return mainContent;
    }

    // 创建 Transaction 类表示交易数据
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

        public String getAmount() {
            return amount.get();
        }

        public String getCategory() {
            return category.get();
        }

        public String getRemarks() {
            return remarks.get();
        }

        // 为每个字段提供 getValue 方法
        public SimpleStringProperty dateProperty() {
            return date;
        }

        public SimpleStringProperty amountProperty() {
            return amount;
        }

        public SimpleStringProperty categoryProperty() {
            return category;
        }

        public SimpleStringProperty remarksProperty() {
            return remarks;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
