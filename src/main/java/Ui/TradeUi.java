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
import pojo.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.JsonUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static Ui.DashBoardUi.createInfoCard;

public class TradeUi extends NavigationSuper {
    private BorderPane root;
    private static ObservableList<Transaction> transactionData = FXCollections.observableArrayList();
    ;

    public TradeUi() {
        // Initialize the ObservableList for transactions
        root = new BorderPane();
        root.setCenter(createSidebar());
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

        // Text Fields for all transaction attributes
        TextField dateField = new TextField();
        dateField.setPromptText("year-month-day hour:minute:second");
        TextField counterpartyField = new TextField();
        counterpartyField.setPromptText("Counterparty");
        TextField itemField = new TextField();
        itemField.setPromptText("Item");
//        ComboBox<String> transactionTypeCombo = new ComboBox<>();
//        transactionTypeCombo.getItems().addAll("Expense", "Income", "Transfer");
//        transactionTypeCombo.setValue("Expense"); // Default value
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        TextField paymentMethodField = new TextField();
        paymentMethodField.setPromptText("Payment Method");
        TextField statusField = new TextField();
        statusField.setPromptText("Status");
        TextField transactionIdField = new TextField();
        transactionIdField.setPromptText("Transaction ID");
        TextField merchantIdField = new TextField();
        merchantIdField.setPromptText("Merchant ID");
        TextField remarksField = new TextField();
        remarksField.setPromptText("Remarks");
        Button addButton = new Button("Add Transaction");
        addButton.setStyle("-fx-background-color: blue; -fx-text-fill: white;");

        // Add transaction form fields to container
//        addTransactionContainer.getChildren().addAll(
//                addTransactionLabel, dateField, counterpartyField, itemField,
//                amountField, paymentMethodField,
//                statusField, transactionIdField, merchantIdField, remarksField, addButton
//        );

        // Transaction List Section
        VBox transactionListContainer = new VBox(15);
        transactionListContainer.setPadding(new Insets(20));
        Label transactionListLabel = new Label("Transaction List");
        transactionListLabel.setFont(new Font(18));
        transactionListLabel.setStyle("-fx-font-weight: bold;");
        TextField searchField = new TextField("Search transactions...");
        Button searchButton = new Button("Search");
        TableView<Transaction> transactionTable = new TableView<>();

        // Columns for all properties in Transaction class
        TableColumn<Transaction, String> dateColumn = new TableColumn<>("Transaction Date");
        TableColumn<Transaction, String> typeColumn = new TableColumn<>("Transaction Type");
        TableColumn<Transaction, String> counterpartyColumn = new TableColumn<>("Counterparty");
        TableColumn<Transaction, String> itemColumn = new TableColumn<>("Item");
        TableColumn<Transaction, String> incExpColumn = new TableColumn<>("Income/Expense");
        TableColumn<Transaction, String> amountColumn = new TableColumn<>("Amount");
        TableColumn<Transaction, String> paymentMethodColumn = new TableColumn<>("Payment Method");
        TableColumn<Transaction, String> statusColumn = new TableColumn<>("Status");
        TableColumn<Transaction, String> transactionIdColumn = new TableColumn<>("Transaction ID");
        TableColumn<Transaction, String> merchantIdColumn = new TableColumn<>("Merchant ID");
        TableColumn<Transaction, String> remarksColumn = new TableColumn<>("Remarks");

        // Set up cell value factories for all columns
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionTime()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionType()));
        counterpartyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCounterparty()));
        itemColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem()));
        incExpColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIncExp()));
        amountColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getAmount())));
        paymentMethodColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentMethod()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        transactionIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionId()));
        merchantIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMerchantId()));
        remarksColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNote()));

        // Add transaction form fields to container
        addTransactionContainer.getChildren().addAll(
                addTransactionLabel, dateField, counterpartyField, itemField,
                amountField, paymentMethodField,
                statusField, transactionIdField, merchantIdField, remarksField, addButton
        );
        HBox transactionListPage = TradeListUi.createTradeButton();
        mainContent.getChildren().addAll(addTransactionContainer, transactionListPage);

        // Add functionality for the "Add Transaction" button
        addButton.setOnAction(e -> {
            if (isValidInput(dateField, counterpartyField, itemField, amountField, paymentMethodField, statusField, transactionIdField, merchantIdField, remarksField)) {
                try {
                    Double.parseDouble(amountField.getText());
                } catch (Exception exception) {
                    exception.printStackTrace();
                    showAlert("Please use valid amount!");
                    return;
                }
                // 确保转换所有需要的字段为 UTF-8 编码
                String counterpartyText = null;
                String amountText = null;
                String paymentMethodText = null;
                String statusText = null;
                String transactionIdText = null;
                String merchantIdText = null;
                String remarksText = null;
                try {
                    counterpartyText = new String(counterpartyField.getText().getBytes("GBK"), "UTF-8");
                    amountText = new String(amountField.getText().getBytes("GBK"), "UTF-8");
                    paymentMethodText = new String(paymentMethodField.getText().getBytes("GBK"), "UTF-8");
                    statusText = new String(statusField.getText().getBytes("GBK"), "UTF-8");
                    transactionIdText = new String(transactionIdField.getText().getBytes("GBK"), "UTF-8");
                    merchantIdText = new String(merchantIdField.getText().getBytes("GBK"), "UTF-8");
                    remarksText = new String(remarksField.getText().getBytes("GBK"), "UTF-8");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                // 使用转换后的文本来构建 Transaction 对象
                Transaction newTransaction = new Transaction(
                        dateField.getText(), // 假设日期字段是 UTF-8 编码的
                        null,
                        counterpartyText, // 使用转换后的文本
                        itemField.getText(), // 假设 item 也是已经是正确编码的
                        amountText, // 使用转换后的文本
                        Double.parseDouble(amountText), // amountText 现在是 UTF-8 编码的
                        paymentMethodText, // 使用转换后的文本
                        statusText, // 使用转换后的文本
                        transactionIdText, // 使用转换后的文本
                        merchantIdText, // 使用转换后的文本
                        remarksText // 使用转换后的文本
                );

                //transactionData.add(newTransaction);
                // Add new transaction to ObservableList
                try {
                    storeTransactionsInMemory(newTransaction);
                    transactionData.clear();
                    List<Transaction> transactionsTemp = JsonUtils.readTransactionsFromClasspath("transactionData.json");
                    transactionData.addAll(transactionsTemp);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                clearFields(dateField, counterpartyField, itemField, amountField, paymentMethodField, statusField, transactionIdField, merchantIdField, remarksField);
            } else {
                showAlert("Please fill in all fields.");
            }
        });


        return mainContent;
    }

    private static boolean isValidInput(TextField... fields) {
        for (TextField field : fields) {
            if (field.getText().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void storeTransactionsInMemory(Transaction transaction) throws IOException, InterruptedException {
        // Iterate over the transactionData and create Transaction objects in memory
        // Create a new Transaction object
        Transaction newTransaction = new Transaction(
                transaction.getTransactionTime(),
                "\"" + transaction.getTransactionType() + "\"",
                "\"" + transaction.getCounterparty() + "\"",
                "\"" + transaction.getItem() + "\"",
                "\"" + transaction.getIncExp() + "\"",
                transaction.getAmount(),
                "\"" + transaction.getPaymentMethod() + "\"",
                "\"" + transaction.getStatus() + "\"",
                "\"" + transaction.getTransactionId() + "\"",
                "\"" + transaction.getMerchantId() + "\"",
                "\"" + transaction.getNote() + "\""
        );
        System.out.println(newTransaction);
        JsonUtils.addManualTransaction(newTransaction);
    }


    private static void loadTransactionsFromFile(ObservableList<Transaction> transactionData) {
        File file = new File("transactions.json");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                StringBuilder jsonString = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
                JSONArray jsonArray = new JSONArray(jsonString.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Transaction transaction = new Transaction(
                            jsonObject.getString("transactionTime"),
                            jsonObject.getString("transactionType"),
                            jsonObject.getString("counterparty"),
                            jsonObject.getString("item"),
                            jsonObject.getString("incExp"),
                            jsonObject.getDouble("amount"),
                            jsonObject.getString("paymentMethod"),
                            jsonObject.getString("status"),
                            jsonObject.getString("transactionId"),
                            jsonObject.getString("merchantId"),
                            jsonObject.getString("note")
                    );
                    transactionData.add(transaction);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    private static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}