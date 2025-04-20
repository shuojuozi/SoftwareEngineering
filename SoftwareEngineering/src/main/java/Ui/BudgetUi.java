package Ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import utils.DeepSeek;

import java.util.UUID;

/**
 * Budget 页面 —— 保留状态（聊天记录、sessionId）
 * 采用『单例组件』方案：侧边栏每次导航时返回同一个 Pane，避免内容丢失。
 */
public class BudgetUi {
    /* ---------------- 单例实现 ---------------- */
    private static final BudgetUi INSTANCE = new BudgetUi();
    public static VBox createDashboardPane() {    // NavigationSuper 调用
        return INSTANCE.layout;
    }

    /* ---------------- 字段 ---------------- */
    private final String sessionId = UUID.randomUUID().toString();  // DeepSeek 会话保持
    private final TextArea chatArea = new TextArea();
    private final TextField chatInput = new TextField();
    final VBox layout;   // 对外暴露的根节点

    /* ---------------- 构造 ---------------- */
    private BudgetUi() {
        // 左侧
        VBox left = new VBox(10);
        left.setPadding(new Insets(20));
        left.getChildren().addAll(new Label("设置月交易"), new HBox(new Label("$0"), new CheckBox("启用")));

        // 右侧
        VBox right = new VBox(10);
        right.setPadding(new Insets(20));
        right.getChildren().addAll(new Label("设置储蓄目标"), new HBox(new Label("$0"), new CheckBox("启用")));

        // 进度条
        ProgressBar bar = new ProgressBar(0.56);
        HBox barBox = new HBox(bar, new Label("56%"));
        barBox.setAlignment(Pos.CENTER);
        barBox.setSpacing(10);

        // AI Icon
        ImageView icon = new ImageView("https://tse2-mm.cn.bing.net/th/id/OIP-C.uTCSuJ7CQm_yA_WNnTqlhAHaHa?rs=1&pid=ImgDetMain");
        icon.setFitWidth(50); icon.setFitHeight(50); icon.setPreserveRatio(true);

        // Chat
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPromptText("AI 对话内容...");
        chatArea.setPrefHeight(250);

        chatInput.setPromptText("输入并回车或点击发送...");
        chatInput.setOnAction(e -> sendMsg());
        Button sendBtn = new Button("发送");
        sendBtn.setOnAction(e -> sendMsg());
        HBox inputBox = new HBox(chatInput, sendBtn);
        inputBox.setSpacing(10);
        inputBox.setAlignment(Pos.CENTER);

        VBox chatBox = new VBox(chatArea, inputBox);
        chatBox.setSpacing(10);
        chatBox.setPadding(new Insets(10));
        chatBox.setMaxWidth(600);

        // Assemble main containers
        HBox main = new HBox(left, right);
        main.setSpacing(20);
        main.setAlignment(Pos.CENTER);

        layout = new VBox(main, barBox, icon, chatBox);
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(20);
    }

    /* ---------------- 发送消息 ---------------- */
    private void sendMsg() {
        String userText = chatInput.getText().trim();
        if (userText.isEmpty()) return;
        chatArea.appendText("你: " + userText + "\n");
        chatInput.clear();
        new Thread(() -> {
            String reply = DeepSeek.chat(sessionId, userText);
            Platform.runLater(() -> chatArea.appendText("AI: " + reply + "\n"));
        }).start();
    }
}
