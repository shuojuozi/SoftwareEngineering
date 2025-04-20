package Ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.prefs.Preferences;

/** 设置中心：查看 / 保存 DeepSeek API Key */
public class SettingsUi {

    /** 提供给 NavigationSuper 的静态工厂 */
    public static VBox createSettingsPane() {
        // 与 DeepSeek 使用同一节点，保证读写一致
        Preferences prefs = Preferences.userNodeForPackage(utils.DeepSeek.class);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // ---------- DeepSeek API Key ----------
        HBox apiBox = new HBox(10);
        apiBox.setAlignment(Pos.CENTER_LEFT);

        Label apiLbl = new Label("DeepSeek API Key:");
        PasswordField apiField = new PasswordField();
        apiField.setPrefWidth(320);
        apiField.setText(prefs.get("deepseek_api_key", ""));   // 读取已保存值

        Label status = new Label();
        Button saveBtn = new Button("保存");
        saveBtn.setOnAction(e -> {
            String key = apiField.getText().trim();
            if (key.isEmpty()) { status.setText("Key 不能为空"); return; }
            prefs.put("deepseek_api_key", key);                 // 写入 Preferences
            status.setText("已保存 ✓");
        });

        apiBox.getChildren().addAll(apiLbl, apiField, saveBtn, status);
        root.getChildren().addAll(title, new Separator(), apiBox);
        return root;
    }
}
