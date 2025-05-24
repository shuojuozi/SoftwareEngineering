package Ui;

// Imports
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import pojo.Transaction;
import utils.JsonUtils;
import utils.DateContext;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import utils.DeepSeek;
import utils.FinanceContext;

import java.util.UUID;

public class BudgetUi {

    private static final BudgetUi INSTANCE = new BudgetUi();
    public static VBox createDashboardPane() { return INSTANCE.layout; }

    /* -------------- Fields -------------- */
    private String sessionId = UUID.randomUUID().toString(); // Changed from final to non-final
    private final WebView chatView = new WebView();
    private final WebEngine webEngine = chatView.getEngine();
    private final StringBuilder htmlBuffer = new StringBuilder();
    private final Parser mdParser = Parser.builder().build();
    private final HtmlRenderer mdRenderer = HtmlRenderer.builder().build();
    private final TextField chatInput = new TextField();
    private final VBox layout;
    private final HBox cards; // Cards section

    /* -------------- Constructor -------------- */
    private BudgetUi() {
        /* ===== Cards Area ===== */
        cards = new HBox(40);
        cards.setAlignment(Pos.CENTER);
        cards.setMaxWidth(1000);

        // Initialize cards
        updateCards();

        // Add listener for date change
        DateContext.yearProperty().addListener((obs, oldVal, newVal) -> updateCards());
        DateContext.monthProperty().addListener((obs, oldVal, newVal) -> updateCards());

        /* ===== Chat Area ===== */
        Button newChatBtn = new Button("New Chat");
        newChatBtn.setStyle("-fx-background-color:#555; -fx-text-fill:white;");
        newChatBtn.setOnAction(e -> {
            sessionId = UUID.randomUUID().toString();
            htmlBuffer.setLength(0);
            htmlBuffer.append("<html><head><style>body{font-family:sans-serif;padding:10px;} "
                    + ".user{color:#0d47a1;} .ai{color:#004d40;} </style></head><body>");
            webEngine.loadContent(htmlBuffer.toString());
        });

        ImageView icon = new ImageView("https://brandlogos.net/wp-content/uploads/2025/02/deepseek_logo_icon-logo_brandlogos.net_s5bgc.png");
        icon.setFitWidth(40); icon.setPreserveRatio(true);

        HBox topRow = new HBox(10, newChatBtn, icon);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Initialize WebView
        chatView.setPrefHeight(400);
        htmlBuffer.append("<html><head><style>body{font-family:sans-serif;padding:10px;} "
                + ".user{color:#0d47a1;} .ai{color:#004d40;} </style></head><body>");
        webEngine.loadContent(htmlBuffer.toString());

        chatInput.setPromptText("Enter your message...");
        chatInput.setOnAction(e -> sendMsg());
        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-color:#114d9d; -fx-text-fill:white;");
        sendBtn.setOnAction(e -> sendMsg());

        HBox inputRow = new HBox(10, chatInput, sendBtn);
        HBox.setHgrow(chatInput, Priority.ALWAYS);

        VBox chatBox = new VBox(10, topRow, chatView, inputRow);
        chatBox.setPadding(new Insets(20)); chatBox.setMaxWidth(1000);

        /* ===== Page Layout ===== */
        layout = new VBox(30, cards, chatBox);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color:#fafafa;");
    }

    /* -------------- Update Card Section -------------- */
    private void updateCards() {
        // Get current month's transactions
        List<Transaction> txs = JsonUtils.getTransactionsByMonth(
                DateContext.getYear(),
                DateContext.getMonth()
        );

        double monthlyExpense = 0;
        for (Transaction t : txs) {
            monthlyExpense += t.getAmount();
        }

        // Clear old cards
        cards.getChildren().clear();

        // Create new cards
        Card storageCard = new Card("Monthly Expense", monthlyExpense);
        Card goalCard = new Card("Monthly Income", FinanceContext.getMonthlyIncome());

        // Add new cards to layout
        cards.getChildren().addAll(storageCard.box, goalCard.box);
    }

    /* -------------- Send Message to AI -------------- */
    private void sendMsg() {
        String txt = chatInput.getText().trim();
        if(txt.isEmpty()) return;

        appendMarkdown("**You:** " + txt, "user");
        chatInput.clear();

        new Thread(() -> {
            String reply = DeepSeek.chat(sessionId, txt);
            Platform.runLater(() -> appendMarkdown("**AI:** " + reply, "ai"));
        }).start();
    }

    /** Render a markdown message and append to WebView */
    private void appendMarkdown(String md, String cssClass) {
        Node document = mdParser.parse(md);
        String html = mdRenderer.render(document);
        htmlBuffer.append("<div class=\"")
                .append(cssClass)
                .append("\">")
                .append(html)
                .append("</div>");
        webEngine.loadContent(htmlBuffer.toString() + "</body></html>");
    }

    /* ==================================================================== */
    /*                            Internal Card Class                      */
    /* ==================================================================== */
    private static class Card {
        private final VBox box = new VBox(8);

        Card(String title, double value){
            box.setPadding(new Insets(20));
            box.setAlignment(Pos.TOP_LEFT);
            box.setPrefSize(280, 140);
            box.setStyle("-fx-background-color:#d8e9ff; -fx-background-radius:8;");

            Label lbl = new Label(title);
            lbl.setStyle("-fx-font-size:18; -fx-text-fill:#0d47a1; -fx-font-weight:bold;");

            Label money = new Label(String.format("$%,.2f", value));
            money.setStyle("-fx-font-size:26; -fx-text-fill:#0d47a1; -fx-font-weight:bold;");

            /* --- Progress Bar --- */
            double monthlyExpense = value;
            double monthlyIncome = FinanceContext.getMonthlyIncome();
            double progress = monthlyIncome > 0 ? monthlyExpense / monthlyIncome : 0;

            // Progress bar color changes depending on thresholds
            ProgressBar bar = new ProgressBar(1.0);
            bar.setPrefWidth(200);
            String barColor;
            if (progress > 1.2) {       // 20% over budget - dark red
                barColor = "#d32f2f";
            } else if (progress > 1.0) { // Slightly over budget - red
                barColor = "#e53935";
            } else if (progress > 0.8) { // Near budget - orange
                barColor = "#ff9800";
            } else {                     // Within budget - blue
                barColor = "#1976d2";
            }
            bar.setProgress(Math.min(progress, 1.0));
            bar.setStyle(String.format("-fx-accent:%s;", barColor));

            Label percent = new Label(String.format("%.0f%%", progress * 100));
            percent.setTextFill(Color.web(barColor));

            HBox barRow = new HBox(10, bar, percent);
            barRow.setAlignment(Pos.CENTER_LEFT);

            box.getChildren().addAll(lbl, money, barRow);
        }
    }
}
