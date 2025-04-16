package application;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class BudgetGoalsPane {

    private VBox chatContent;
    private ScrollPane chatScroll;
    private TextField messageInput;

    public VBox getView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.TOP_CENTER);

        // ========== 1. 月度预算设置 ==========
        VBox monthlyBox = new VBox(10);
        monthlyBox.setAlignment(Pos.CENTER);
        Label monthlyLabel = new Label("⚙ Set your monthly budget");
        monthlyLabel.setFont(Font.font(16));
        HBox monthlyInput = new HBox(10);
        monthlyInput.setAlignment(Pos.CENTER);
        TextField monthlyField = new TextField("5,000");
        ComboBox<String> currencyBox1 = new ComboBox<>();
        currencyBox1.getItems().addAll("Dollar(USD)", "Pound(GBP)", "CNY(¥)");
        currencyBox1.setValue("Dollar(USD)");
        monthlyInput.getChildren().addAll(monthlyField, currencyBox1);
        monthlyBox.getChildren().addAll(monthlyLabel, monthlyInput);

        // ========== 2. 储蓄目标设置 ==========
        VBox savingBox = new VBox(10);
        savingBox.setAlignment(Pos.CENTER);
        Label savingLabel = new Label("⚙ Set your savings goal");
        savingLabel.setFont(Font.font(16));
        HBox savingInput = new HBox(10);
        savingInput.setAlignment(Pos.CENTER);
        TextField savingField = new TextField("500,000");
        ComboBox<String> currencyBox2 = new ComboBox<>();
        currencyBox2.getItems().addAll("Dollar(USD)", "Pound(GBP)", "CNY(¥)");
        currencyBox2.setValue("Dollar(USD)");
        savingInput.getChildren().addAll(savingField, currencyBox2);
        savingBox.getChildren().addAll(savingLabel, savingInput);

        HBox topSettings = new HBox(100, monthlyBox, savingBox);
        topSettings.setAlignment(Pos.CENTER);

        // ========== 3. 进度条 ==========
        VBox progressBox = new VBox(10);
        progressBox.setAlignment(Pos.CENTER);
        Label progressLabel = new Label("Achieve progress");
        progressLabel.setFont(Font.font(16));
        ProgressBar progressBar = new ProgressBar(0.1);
        progressBar.setPrefWidth(200);
        Label percentLabel = new Label("10%");
        progressBox.getChildren().addAll(progressLabel, progressBar, percentLabel);

        // ========== 4. Smart Assistant 区域 ==========
        VBox assistantContainer = new VBox(10);
        assistantContainer.setPadding(new Insets(10));
        assistantContainer.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-border-color: #ccc; -fx-border-radius: 8;");
        assistantContainer.setAlignment(Pos.TOP_CENTER);
        assistantContainer.setMaxHeight(450); // 限制最大高度

        // 顶部头像与标题
        Image image = new Image("file:/C:/Users/XIOXIN 16/Desktop/ai.png", 100, 100, true, true);
        ImageView imageView = new ImageView(image);
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(50, 50, 40);
        imageView.setClip(clip);
        Label assistantTitle = new Label("Smart Budget Assistant");
        assistantTitle.setFont(Font.font("Arial", 18));

        // 聊天内容和滚动区
        chatContent = new VBox(10);
        chatContent.setPadding(new Insets(10));
        chatContent.setPrefWidth(400);
        chatContent.setFillWidth(true);

        chatScroll = new ScrollPane(chatContent);
        chatScroll.setFitToWidth(true);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScroll.setStyle("-fx-background: #f9f9f9; -fx-background-color: #f9f9f9;");
        VBox.setVgrow(chatScroll, Priority.ALWAYS);

        // 输入区
        HBox messageBox = new HBox(10);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(10, 0, 0, 0));

        messageInput = new TextField();
        messageInput.setPromptText("Ask something...");
        messageInput.setPrefHeight(35);
        messageInput.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        Button sendBtn = new Button("➤");
        sendBtn.setFont(Font.font(14));
        sendBtn.setPrefHeight(35);
        sendBtn.setPrefWidth(50);

        // ✅ 支持按钮与回车键发送
        sendBtn.setOnAction(e -> sendMessage());
        messageInput.setOnAction(e -> sendMessage());

        messageBox.getChildren().addAll(messageInput, sendBtn);

        assistantContainer.getChildren().addAll(imageView, assistantTitle, chatScroll, messageBox);

        // ========== 主布局整合 ==========
        container.getChildren().addAll(
                topSettings,
                progressBox,
                assistantContainer
        );
        VBox.setVgrow(assistantContainer, Priority.ALWAYS); // 让它拉到底部

        return container;
    }

    // ✅ 统一的发送逻辑（按钮或回车）
    private void sendMessage() {
        String userMsg = messageInput.getText().trim();
        if (!userMsg.isEmpty()) {
            Label userLabel = new Label("You: " + userMsg);
            userLabel.setStyle("-fx-background-color: #d4edda; -fx-padding: 8; -fx-background-radius: 5;");
            userLabel.setWrapText(true);
            chatContent.getChildren().add(userLabel);

            Label aiReply = new Label("AI: Simulated reply to \"" + userMsg + "\".");
            aiReply.setStyle("-fx-background-color: #f1f1f1; -fx-padding: 8; -fx-background-radius: 5;");
            aiReply.setWrapText(true);
            chatContent.getChildren().add(aiReply);

            messageInput.clear();

            // ✅ 自动滚动到底部（必须 Platform.runLater）
            chatContent.heightProperty().addListener((obs, oldVal, newVal) -> {
                chatScroll.setVvalue(1.0);
            });

        }
    }
}
