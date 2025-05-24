package Ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TransactionUi {

    // Create Transaction Detail Page (Unified module entry)
    public static VBox createTransactionDetailPage() {
        VBox mainContent = new VBox();
        mainContent.setPadding(new Insets(20));

        // ========== Header and Action Buttons ==========
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #007bff; -fx-padding: 10;");

        Label titleLabel = new Label("Transaction Details");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(new Font(24));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: yellow; -fx-text-fill: black;");

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        header.getChildren().addAll(titleLabel, spacer, editButton, deleteButton);

        // ========== Transaction Detail Section ==========
        GridPane transactionDetails = new GridPane();
        transactionDetails.setPadding(new Insets(20));
        transactionDetails.setVgap(10);
        transactionDetails.setHgap(10);

        transactionDetails.add(new Label("Date:"), 0, 0);
        transactionDetails.add(new Label("2025-04-15"), 1, 0);
        transactionDetails.add(new Label("Amount:"), 0, 1);
        transactionDetails.add(new Label("100.00"), 1, 1);
        transactionDetails.add(new Label("Currency:"), 0, 2);
        transactionDetails.add(new Label("USD"), 1, 2);
        transactionDetails.add(new Label("Notes:"), 0, 3);
        transactionDetails.add(new Label("Sample transaction note."), 1, 3);

        // ========== Modification History ==========
        VBox modificationHistory = new VBox(5);
        modificationHistory.setPadding(new Insets(20));
        modificationHistory.getChildren().addAll(
                new Label("Modification History"),
                new Label("2025-04-14: Transaction created."),
                new Label("2025-04-15: Amount changed from 50.00 to 100.00.")
        );

        // ========== Final Layout ==========
        mainContent.getChildren().addAll(header, transactionDetails, modificationHistory);
        return mainContent;
    }
}
