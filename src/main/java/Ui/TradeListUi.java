package Ui;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import pojo.Transaction;
import javafx.collections.FXCollections;
import utils.JsonUtils;

public class TradeListUi {

    private static ObservableList<Transaction> transactionData = FXCollections.observableArrayList();

    public TradeListUi() {
        super();
    }

    public static VBox createTransactionListPage() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));

        // Transaction List Section
        VBox transactionListContainer = new VBox(15);
        transactionListContainer.setPadding(new Insets(20));

        Label transactionListLabel = new Label("Transaction List");
        transactionListLabel.setFont(new Font(18));
        transactionListLabel.setStyle("-fx-font-weight: bold;");

        TextField searchField = new TextField("Search transactions...");
        Button searchButton = new Button("Search");

        TableView<Transaction> transactionTable = new TableView<>();

        // Search logic
        searchButton.setOnAction(e -> {
            String keyword = searchField.getText().trim().toLowerCase();
            ObservableList<Transaction> filtered = FXCollections.observableArrayList();

            for (Transaction t : transactionData) {
                if ((t.getTransactionTime() != null && t.getTransactionTime().toLowerCase().contains(keyword)) ||
                        (t.getTransactionType() != null && t.getTransactionType().toLowerCase().contains(keyword)) ||
                        (t.getCounterparty() != null && t.getCounterparty().toLowerCase().contains(keyword)) ||
                        (t.getItem() != null && t.getItem().toLowerCase().contains(keyword)) ||
                        (t.getIncExp() != null && t.getIncExp().toLowerCase().contains(keyword)) ||
                        (String.valueOf(t.getAmount()).contains(keyword)) ||
                        (t.getPaymentMethod() != null && t.getPaymentMethod().toLowerCase().contains(keyword)) ||
                        (t.getStatus() != null && t.getStatus().toLowerCase().contains(keyword)) ||
                        (t.getTransactionId() != null && t.getTransactionId().toLowerCase().contains(keyword)) ||
                        (t.getMerchantId() != null && t.getMerchantId().toLowerCase().contains(keyword)) ||
                        (t.getNote() != null && t.getNote().toLowerCase().contains(keyword))) {
                    filtered.add(t);
                }
            }

            transactionTable.setItems(filtered);
        });

        // Define table columns
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

        // Set column values
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

        transactionTable.getColumns().addAll(
                dateColumn, typeColumn, counterpartyColumn, itemColumn,
                incExpColumn, amountColumn, paymentMethodColumn, statusColumn,
                transactionIdColumn, merchantIdColumn, remarksColumn
        );

        // Load data from JSON file
        List<Transaction> transactions = JsonUtils.readTransactionsFromClasspath("transactionData.json");
        transactionData.clear();
        transactionData.addAll(transactions);
        transactionTable.setItems(transactionData);
        transactionTable.setMaxHeight(350);

        transactionListContainer.getChildren().addAll(transactionListLabel, searchField, searchButton, transactionTable);
        mainContent.getChildren().add(transactionListContainer);
        return mainContent;
    }

    public static HBox createTradeButton() {
        // Container for horizontal button layout
        HBox buttonContainer = new HBox(20);
        buttonContainer.setPadding(new Insets(20));
        buttonContainer.setStyle("-fx-alignment: center;");

        // Container for "Manual Import File" button
        VBox autoImportContainer = new VBox(10);
        autoImportContainer.setPadding(new Insets(10));
        autoImportContainer.setStyle("-fx-background-color: lightblue; -fx-border-color: blue; -fx-border-width: 2px; -fx-alignment: center;");

        Button autoImportButton = new Button("Manual Import File");
        autoImportButton.setFont(new Font(14));
        autoImportButton.setMinWidth(200);
        autoImportButton.setStyle("-fx-background-color: blue; -fx-text-fill: white;");

        Label autoImportTitle = new Label("Manual Import Part");
        autoImportTitle.setFont(new Font(16));
        autoImportTitle.setStyle("-fx-font-weight: bold;");
        autoImportContainer.getChildren().addAll(autoImportTitle, autoImportButton);

        // Container for "Details" button
        VBox viewDetailsContainer = new VBox(10);
        viewDetailsContainer.setPadding(new Insets(10));
        viewDetailsContainer.setStyle("-fx-background-color: lightgreen; -fx-border-color: green; -fx-border-width: 2px; -fx-alignment: center;");

        Button viewDetailsButton = new Button("Details");
        viewDetailsButton.setFont(new Font(14));
        viewDetailsButton.setMinWidth(200);
        viewDetailsButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");

        Label viewDetailsTitle = new Label("Details Part");
        viewDetailsTitle.setFont(new Font(16));
        viewDetailsTitle.setStyle("-fx-font-weight: bold;");
        viewDetailsContainer.getChildren().addAll(viewDetailsTitle, viewDetailsButton);

        // Button actions
        autoImportButton.setOnAction(e -> handleAutoImportAction());
        viewDetailsButton.setOnAction(e -> handleViewDetailsAction());

        buttonContainer.getChildren().addAll(autoImportContainer, viewDetailsContainer);
        return buttonContainer;
    }

    private static void handleViewDetailsAction() {
        Stage detailsStage = new Stage();
        VBox transactionListPage = TradeListUi.createTransactionListPage();
        Scene scene = new Scene(transactionListPage, 800, 600);
        detailsStage.setScene(scene);
        detailsStage.setTitle("Transaction Details");
        detailsStage.show();
    }

    private static void handleAutoImportAction() {
        // Prompt for file path
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Manual");
        dialog.setHeaderText("Select CSV File Path");
        dialog.setContentText("File Path:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(filePath -> {
            File file = new File(filePath);
            if (file.exists() && file.isFile() && file.canRead()) {
                try {
                    JsonUtils.parseCsv2Json(filePath);

                    Alert infoAlert = new Alert(AlertType.INFORMATION);
                    infoAlert.setTitle("Import Successful");
                    infoAlert.setHeaderText(null);
                    infoAlert.setContentText("CSV File has been Imported");
                    infoAlert.showAndWait();

                } catch (Exception e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(AlertType.ERROR);
                    errorAlert.setTitle("Import Error");
                    errorAlert.setHeaderText("CSV File cannot be Imported");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.showAndWait();
                }
            } else {
                Alert warningAlert = new Alert(AlertType.WARNING);
                warningAlert.setTitle("Invalid File Path");
                warningAlert.setHeaderText("Invalid File Path");
                warningAlert.setContentText("Please Input Valid File Path");
                warningAlert.showAndWait();
            }
        });
    }
}
