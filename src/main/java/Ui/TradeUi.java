package Ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.concurrent.Task;
import pojo.Transaction;
import utils.JsonUtils;

import java.io.File;

public class TradeUi {
    private static ProgressBar csvProgress;
    private static final ObservableList<Transaction> transactionData = FXCollections.observableArrayList();

    public static VBox createTradeManagementPage() {
        VBox main = new VBox(20);
        main.setPadding(new Insets(20));

        // ==== Manual Add Card ====
        VBox addCard = new VBox(8);
        addCard.setPadding(new Insets(15));
        addCard.setStyle("-fx-background-color:#f0f0f0; -fx-border-radius:5; -fx-background-radius:5;");
        Label addLbl = new Label("Add Transaction Manually");
        addLbl.setFont(new Font(18));
        addLbl.setStyle("-fx-font-weight:bold;");

        TextField dateField = field("year-month-day hour:minute:second");
        TextField cpField = field("Counterparty");
        TextField itemField = field("Item");
        TextField amtField = field("Amount");
        TextField payField = field("Payment Method");
        TextField statusField = field("Status");
        TextField txIdField = field("Transaction ID");
        TextField mchIdField = field("Merchant ID");
        TextField noteField = field("Remarks");
        Button addBtn = btn("Add Transaction");

        addBtn.setOnAction(e -> handleManualAdd(dateField, cpField, itemField,
                amtField, payField, statusField, txIdField, mchIdField, noteField));

        addCard.getChildren().addAll(addLbl, dateField, cpField, itemField, amtField,
                payField, statusField, txIdField, mchIdField, noteField, addBtn);

        // ==== CSV Import Card ====
        VBox importCard = new VBox(10);
        importCard.setAlignment(Pos.CENTER);
        importCard.setPadding(new Insets(15));
        importCard.setStyle("-fx-background-color:#f0f0f0; -fx-border-radius:5; -fx-background-radius:5; -fx-border-color:#999; -fx-border-style:dashed;");

        Label importLbl = new Label("Drag CSV here or click to browse");
        importLbl.setFont(new Font(16));
        Button browseBtn = btn("Browse…");

        csvProgress = new ProgressBar(0);
        csvProgress.setPrefWidth(250);
        csvProgress.setVisible(false);

        importCard.getChildren().addAll(importLbl, browseBtn, csvProgress);
        importCard.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) e.acceptTransferModes(TransferMode.COPY);
            e.consume();
        });
        importCard.setOnDragDropped(TradeUi::handleFileDrop);
        browseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            File f = fc.showOpenDialog(null);
            if (f != null) processCsv(f);
        });

        // ==== Transaction List Buttons ====
        HBox listBtns = TradeListUi.createTradeButton();

        main.getChildren().addAll(addCard, importCard, new Separator(), listBtns);
        return main;
    }

    // ===== Utility Control Builder =====
    private static TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        return f;
    }

    private static Button btn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:#004085; -fx-text-fill:white;");
        return b;
    }

    // ===== Manual Add Handler =====
    private static void handleManualAdd(TextField date, TextField cp, TextField item, TextField amt, TextField pay, TextField status, TextField txId, TextField mchId, TextField note) {
        if (date.getText().isEmpty() || cp.getText().isEmpty() || item.getText().isEmpty() || amt.getText().isEmpty()) {
            alert("All required");
            return;
        }
        try {
            Double.parseDouble(amt.getText());
        } catch (NumberFormatException ex) {
            alert("Invalid amount");
            return;
        }
        Transaction t = new Transaction(date.getText(), null, cp.getText(), item.getText(), null,
                Double.parseDouble(amt.getText()), pay.getText(), status.getText(),
                txId.getText(), mchId.getText(), note.getText());
        try {
            JsonUtils.addManualTransaction(t);
            alert("Added ✔");
        } catch (Exception ex) {
            alert(ex.getMessage());
        }

        date.clear();
        cp.clear();
        item.clear();
        amt.clear();
        pay.clear();
        status.clear();
        txId.clear();
        mchId.clear();
        note.clear();
    }

    // ===== CSV Drag & Drop Import Handler =====
    private static void handleFileDrop(DragEvent e) {
        Dragboard db = e.getDragboard();
        boolean ok = false;
        if (db.hasFiles()) {
            for (File f : db.getFiles()) {
                if (f.getName().endsWith(".csv")) {
                    processCsv(f);
                    ok = true;
                }
            }
        }
        e.setDropCompleted(ok);
        e.consume();
    }

    private static void processCsv(File f) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                csvProgress.setVisible(true);
                JsonUtils.parseCsv2Json(f.getAbsolutePath(), p -> updateProgress(p, 1.0));
                return null;
            }
        };
        csvProgress.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            csvProgress.setVisible(false);
            alert("Imported: " + f.getName());
        });
        task.setOnFailed(e -> {
            csvProgress.setVisible(false);
            alert("Import failed: " + task.getException().getMessage());
        });
        new Thread(task).start();
    }

    private static void alert(String m) {
        new Alert(Alert.AlertType.INFORMATION, m).showAndWait();
    }
}
