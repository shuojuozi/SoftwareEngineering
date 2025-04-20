package Ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
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
        root.setLeft(createSidebar());
        root.setCenter(createTradeManagementPage());
        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.setTitle("Transaction Management");
        stage.show();
    }

    public static VBox createTradeManagementPage() {
        VBox mainContent = new VBox();
        mainContent.setPadding(new Insets(20));
        mainContent.setSpacing(20);

        // Add Transaction Section
        VBox addTransactionContainer = new VBox(5);
        addTransactionContainer.setPadding(new Insets(10));
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
        addTransactionContainer.getChildren().addAll(
                addTransactionLabel, dateField, counterpartyField, itemField,
                amountField, paymentMethodField,
                statusField, transactionIdField, merchantIdField, remarksField, addButton
        );

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

        // Add columns to table
        transactionTable.getColumns().addAll(
                dateColumn, typeColumn, counterpartyColumn, itemColumn,
                incExpColumn, amountColumn, paymentMethodColumn, statusColumn,
                transactionIdColumn, merchantIdColumn, remarksColumn
        );

        List<Transaction> transactions = JsonUtils.readTransactionsFromClasspath("transactionData.json");
        transactionData.addAll(transactions);

        // Set table items
        transactionTable.setItems(transactionData);
        transactionTable.setMaxHeight(350);

        // Add search and table to the transaction list container
        transactionListContainer.getChildren().addAll(transactionListLabel, searchField, searchButton, transactionTable);

        // Add both sections to the main content
        mainContent.getChildren().addAll(addTransactionContainer, transactionListContainer);

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
                String counterpartyText = counterpartyField.getText();
                String amountText = amountField.getText();
                String paymentMethodText = paymentMethodField.getText();
                String statusText = statusField.getText();
                String transactionIdText = transactionIdField.getText();
                String merchantIdText = merchantIdField.getText();
                String remarksText = remarksField.getText();
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


//                transactionData.add(newTransaction); // Add new transaction to ObservableList
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
