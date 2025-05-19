package Ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

public class TradeListUi extends NavigationSuper {

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

        // Define columns
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

        // Add columns to the table
        transactionTable.getColumns().addAll(
                dateColumn, typeColumn, counterpartyColumn, itemColumn,
                incExpColumn, amountColumn, paymentMethodColumn, statusColumn,
                transactionIdColumn, merchantIdColumn, remarksColumn
        );

        // Load transaction data into the ObservableList
        List<Transaction> transactions = JsonUtils.readTransactionsFromClasspath("transactionData.json");
        transactionData.clear();  // Clear existing data to ensure it's reloaded
        transactionData.addAll(transactions);

        // Set table items
        transactionTable.setItems(transactionData);
        transactionTable.setMaxHeight(350);

        // Handle search functionality
        searchButton.setOnAction(e -> {
            String transactionId = searchField.getText().trim();
            if (!transactionId.isEmpty()) {
                // 查找与输入ID匹配的交易
                Optional<Transaction> transactionOpt = transactionData.stream()
                        .filter(t -> t.getTransactionId().equals(transactionId))
                        .findFirst();

                if (transactionOpt.isPresent()) {
                    // 如果找到匹配的交易，展示交易详情界面
                    TransactionUi transactionUi = new TransactionUi(transactionOpt.get());
                    Stage detailsStage = new Stage();
                    Scene transactionScene = new Scene(transactionUi.createTransactionDetailsPage(), 800, 600);
                    detailsStage.setScene(transactionScene);
                    detailsStage.setTitle("Transaction Details");
                    detailsStage.show();
                } else {
                    // 如果没有找到交易，弹出错误提示框
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Transaction Not Found");
                    alert.setHeaderText(null);
                    alert.setContentText("No transaction found with the ID: " + transactionId);
                    alert.showAndWait();
                }
            }
        });

        // Add search and table to the transaction list container
        transactionListContainer.getChildren().addAll(transactionListLabel, searchField, searchButton, transactionTable);

        // Return the final container
        mainContent.getChildren().add(transactionListContainer);

        return mainContent;
    }


    public static HBox createTradeButton() {
        // 创建用于水平排列按钮的主容器
        HBox buttonContainer = new HBox(20);
        buttonContainer.setPadding(new Insets(20));
        buttonContainer.setStyle("-fx-alignment: center;");  // 将按钮容器居中对齐

        // 创建“自动导入文件”按钮的容器
        VBox autoImportContainer = new VBox(10);
        autoImportContainer.setPadding(new Insets(10));
        autoImportContainer.setStyle("-fx-background-color: lightblue; -fx-border-color: blue; -fx-border-width: 2px; -fx-alignment: center;");

        // 创建“自动导入文件”按钮
        Button autoImportButton = new Button("Auto Import File");
        autoImportButton.setFont(new Font(14));
        autoImportButton.setMinWidth(200);  // 设置按钮的最小宽度
        autoImportButton.setStyle("-fx-background-color: blue; -fx-text-fill: white;");

        // 添加标题和按钮到容器
        Label autoImportTitle = new Label("Auto Import Part");
        autoImportTitle.setFont(new Font(16));
        autoImportTitle.setStyle("-fx-font-weight: bold;");
        autoImportContainer.getChildren().addAll(autoImportTitle, autoImportButton);

        // 创建“查看详情”按钮的容器
        VBox viewDetailsContainer = new VBox(10);
        viewDetailsContainer.setPadding(new Insets(10));
        viewDetailsContainer.setStyle("-fx-background-color: lightgreen; -fx-border-color: green; -fx-border-width: 2px; -fx-alignment: center;");

        // 创建“查看详情”按钮
        Button viewDetailsButton = new Button("Details");
        viewDetailsButton.setFont(new Font(14));
        viewDetailsButton.setMinWidth(200);  // 设置按钮的最小宽度
        viewDetailsButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");

        // 添加标题和按钮到容器
        Label viewDetailsTitle = new Label("Details Part");
        viewDetailsTitle.setFont(new Font(16));
        viewDetailsTitle.setStyle("-fx-font-weight: bold;");
        viewDetailsContainer.getChildren().addAll(viewDetailsTitle, viewDetailsButton);

        // 处理按钮操作
        autoImportButton.setOnAction(e -> handleAutoImportAction());
        viewDetailsButton.setOnAction(e -> handleViewDetailsAction());

        // 将两个容器添加到主容器（HBox）
        buttonContainer.getChildren().addAll(autoImportContainer, viewDetailsContainer);

        return buttonContainer;
    }

    private static void handleViewDetailsAction() {
        // 创建一个新窗口
        Stage detailsStage = new Stage();

        // 调用createTransactionListPage方法，获取交易列表页面
        VBox transactionListPage = createTransactionListPage();

        // 设置新窗口的场景
        Scene transactionListScene = new Scene(transactionListPage, 800, 600);
        detailsStage.setScene(transactionListScene);
        detailsStage.setTitle("Transaction List");
        detailsStage.show();
    }


    private static void handleAutoImportAction() {
        // 创建一个文本输入对话框让用户输入文件路径
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Auto");
        dialog.setHeaderText("Select CSV File Path");
        dialog.setContentText("File Path：");

        // 显示对话框并等待用户输入
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(filePath -> {
            // 验证文件路径
            File file = new File(filePath);
            if (file.exists() && file.isFile() && file.canRead()) {
                // 继续导入CSV文件
                try {
                    JsonUtils.parseCsv2Json(filePath);

                    // 导入成功后显示确认提示框
                    Alert infoAlert = new Alert(AlertType.INFORMATION);
                    infoAlert.setTitle("Import Successful");
                    infoAlert.setHeaderText(null);
                    infoAlert.setContentText("CSV File has been Imported");
                    infoAlert.showAndWait();

                } catch (Exception e) {
                    e.printStackTrace();
                    // 如果导入失败，显示错误提示框
                    Alert errorAlert = new Alert(AlertType.ERROR);
                    errorAlert.setTitle("Import Error");
                    errorAlert.setHeaderText("CSV File cannot be Imported");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.showAndWait();
                }
            } else {
                // 对于无效的文件路径，显示警告
                Alert warningAlert = new Alert(AlertType.WARNING);
                warningAlert.setTitle("Invalid File Path");
                warningAlert.setHeaderText("Invalid File Path");
                warningAlert.setContentText("Please Input Valid File Path");
                warningAlert.showAndWait();
            }
        });
    }



}