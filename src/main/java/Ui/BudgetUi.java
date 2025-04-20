package Ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import utils.DeepSeek;

import java.util.UUID;

/**
 * Budget UI —— 合并旧版视觉元素（资产卡片、双进度条、输入区）与新版单例聊天记忆功能。
 */
public class BudgetUi {
    /* ---------------- 单例 ---------------- */
    private static final BudgetUi INSTANCE = new BudgetUi();
    public static VBox createDashboardPane() { return INSTANCE.layout; }

    /* ---------------- 数据 ---------------- */
    private static double money = 0.0;   // 可由其他模块动态修改

    private final String sessionId = UUID.randomUUID().toString();
    private final TextArea chatArea = new TextArea();
    private final TextField chatInput = new TextField();

    private final VBox layout; // 供外部引用

    /* ---------------- 构造 ---------------- */
    private BudgetUi() {
        /* ---------- 顶部信息卡片 row ---------- */
        VBox leftCardBox  = new VBox(20);
        leftCardBox.setStyle("-fx-background-color: #cce5ff; -fx-background-radius: 10; -fx-padding: 15;");
        leftCardBox.getChildren().addAll(createInfoCard("Monthly Storage", "$" + money, "#cce5ff", "#004085"),
                createStyledButton("Setting"));

        VBox rightCardBox = new VBox(20);
        rightCardBox.setStyle("-fx-background-color: #f1f8ff; -fx-background-radius: 10; -fx-padding: 15;");
        rightCardBox.getChildren().addAll(createInfoCard("Saving goals", "$" + money, "#f1f8ff", "#004085"),
                createStyledButton("Setting"));

        HBox topRow = new HBox(leftCardBox, rightCardBox);
        topRow.setSpacing(200);
        topRow.setPadding(new Insets(20));
        topRow.setAlignment(Pos.CENTER);

        /* ---------- 中间双进度条 row ---------- */
        ProgressBar barL = new ProgressBar(0.69);
        Label lblL = new Label("69%");
        HBox progL = new HBox(barL, lblL);
        progL.setAlignment(Pos.CENTER_LEFT); progL.setSpacing(10);

        ProgressBar barR = new ProgressBar(0.84);
        Label lblR = new Label("84%");
        HBox progR = new HBox(barR, lblR);
        progR.setAlignment(Pos.CENTER_RIGHT); progR.setSpacing(10);

        HBox centerRow = new HBox(progL, progR);
        centerRow.setPadding(new Insets(20));
        centerRow.setSpacing(200);
        centerRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(progL, Priority.ALWAYS);
        HBox.setHgrow(progR, Priority.ALWAYS);

        /* ---------- AI Icon ---------- */
        ImageView icon = new ImageView("https://tse2-mm.cn.bing.net/th/id/OIP-C.uTCSuJ7CQm_yA_WNnTqlhAHaHa?rs=1&pid=ImgDetMain");
        icon.setFitWidth(50); icon.setFitHeight(50); icon.setPreserveRatio(true);

        /* ---------- 聊天区域 ---------- */
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPromptText("AI 对话内容...");
        chatArea.setPrefHeight(220);

        chatInput.setPromptText("输入并回车或点击发送...");
        chatInput.setOnAction(e -> sendMsg());
        Button sendBtn = createStyledButton("发送");
        sendBtn.setOnAction(e -> sendMsg());
        HBox chatInputRow = new HBox(chatInput, sendBtn);
        chatInputRow.setSpacing(10);
        chatInputRow.setAlignment(Pos.CENTER);

        VBox chatBox = new VBox(chatArea, chatInputRow);
        chatBox.setPadding(new Insets(10));
        chatBox.setSpacing(10);
        chatBox.setMaxWidth(800);



        /* ---------- Assemble ---------- */
        layout = new VBox(topRow, centerRow, icon, chatBox);
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(20);
    }

    /* ---------------- 工具方法 ---------------- */
    private void sendMsg() {
        String txt = chatInput.getText().trim();
        if (txt.isEmpty()) return;
        chatArea.appendText("你: " + txt + "\n");
        chatInput.clear();
        new Thread(() -> {
            String reply = DeepSeek.chat(sessionId, txt);
            Platform.runLater(() -> chatArea.appendText("AI: " + reply + "\n"));
        }).start();
    }

    private static Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #004085; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20;");
        return btn;
    }

    public static VBox createInfoCard(String title, String value, String bg, String fg) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10; -fx-padding: 15;");
        Label t = new Label(title); t.setFont(new Font(18)); t.setStyle("-fx-text-fill: " + fg);
        Label v = new Label(value); v.setFont(new Font(20)); v.setStyle("-fx-text-fill: " + fg);
        box.getChildren().addAll(t, v);
        return box;
    }
}
