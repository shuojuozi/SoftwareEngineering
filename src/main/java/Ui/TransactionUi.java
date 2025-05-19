package Ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

import pojo.Transaction;

public class TransactionUi {
    private Transaction transaction;

    public TransactionUi(Transaction transaction) {
        this.transaction = transaction;
    }

    // 创建交易详情页面
    public VBox createTransactionDetailsPage() {
        VBox detailsPage = new VBox(15);
        detailsPage.setPadding(new Insets(20));
        detailsPage.setStyle("-fx-background-color: #f0f0f0; -fx-border-radius: 10;");

        // 设置标题
        Label titleLabel = new Label("Transaction Details");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4b8df8;");

        // 创建显示交易详细信息的标签
        HBox transactionIdBox = createInfoRow("Transaction ID: ", transaction.getTransactionId());
        HBox typeBox = createInfoRow("Transaction Type: ", transaction.getTransactionType());
        HBox counterpartyBox = createInfoRow("Counterparty: ", transaction.getCounterparty());
        HBox itemBox = createInfoRow("Item: ", transaction.getItem());
        HBox incExpBox = createInfoRow("Income/Expense: ", transaction.getIncExp());
        HBox amountBox = createInfoRow("Amount: ", String.valueOf(transaction.getAmount()));
        HBox paymentMethodBox = createInfoRow("Payment Method: ", transaction.getPaymentMethod());
        HBox statusBox = createInfoRow("Status: ", transaction.getStatus());
        HBox merchantIdBox = createInfoRow("Merchant ID: ", transaction.getMerchantId());
        HBox remarksBox = createInfoRow("Remarks: ", transaction.getNote());

        // 将所有标签添加到页面中
        detailsPage.getChildren().addAll(titleLabel, transactionIdBox, typeBox, counterpartyBox, itemBox,
                incExpBox, amountBox, paymentMethodBox, statusBox, merchantIdBox, remarksBox);

        return detailsPage;
    }

    // 创建一行显示信息的布局
    private HBox createInfoRow(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.setFont(new Font("Arial", 14));
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");

        Label valueLabel = new Label(valueText);
        valueLabel.setFont(new Font("Arial", 14));
        valueLabel.setStyle("-fx-text-fill: #555555;");

        HBox row = new HBox(10, label, valueLabel);
        row.setPadding(new Insets(5, 0, 5, 0));
        row.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 5; -fx-padding: 5;");
        return row;
    }
}
