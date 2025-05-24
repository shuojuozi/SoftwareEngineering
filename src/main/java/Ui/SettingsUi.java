package Ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import utils.DateContext;
import utils.FinanceContext;

import java.util.prefs.Preferences;

public class SettingsUi {

    // DeepSeek Key preference node (keeps original path)
    private static final Preferences KEY_PREF = Preferences.userNodeForPackage(utils.DeepSeek.class);

    public static VBox createSettingsPane() {

        // ---------- Main Container ----------
        VBox root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_CENTER);

        // ---------- DeepSeek API Key ----------
        Label title = new Label("⚙ Settings");
        title.setStyle("-fx-font-size: 22;");
        PasswordField keyField = new PasswordField();
        keyField.setText(KEY_PREF.get("deepseek_api_key", ""));
        Button saveKey = new Button("Save API Key");
        saveKey.setOnAction(e -> KEY_PREF.put("deepseek_api_key", keyField.getText().trim()));
        VBox keyBox = new VBox(10, new Label("DeepSeek API Key"), keyField, saveKey);

        // ---------- Finance Cards ----------
        HBox cardsRow1 = new HBox(40);
        HBox cardsRow2 = new HBox(40);
        cardsRow1.setAlignment(Pos.CENTER);
        cardsRow2.setAlignment(Pos.CENTER);

        cardsRow1.getChildren().addAll(
                buildCard("Total Assets", FinanceContext.getTotalAssets(),
                        "#2f5597", "#ffffff", () -> promptDouble("Total Assets",
                                FinanceContext.getTotalAssets(), FinanceContext::setTotalAssets)),

                buildCard("Savings Goal", FinanceContext.getSavingsGoal(),
                        "#28a745", "#ffffff", () -> promptDouble("Savings Goal",
                                FinanceContext.getSavingsGoal(), FinanceContext::setSavingsGoal))
        );

        cardsRow2.getChildren().addAll(
                buildCard("Monthly Income", FinanceContext.getMonthlyIncome(),
                        "#f39c12", "#000000", () -> promptDouble("Monthly Income",
                                FinanceContext.getMonthlyIncome(), FinanceContext::setMonthlyIncome)),

                buildDateCard()   // Red card: Current Date
        );

        // ---------- Assemble and Return ----------
        root.getChildren().addAll(title, keyBox, cardsRow1, cardsRow2);
        return root;
    }

    /**
     * Build a generic finance setting card (for assets, income, goals)
     */
    private static VBox buildCard(String label, double value,
                                  String bgColor, String fgColor, Runnable onSet) {
        VBox box = new VBox(8);
        box.setPrefWidth(400);
        box.setStyle("-fx-background-color:" + bgColor + ";"
                + "-fx-background-radius:8;");
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:" + fgColor + "; -fx-font-size: 18;");
        Label val = new Label(String.valueOf(value));
        val.setStyle("-fx-text-fill:" + fgColor + "; -fx-font-size: 22; -fx-font-weight:bold;");
        Button setBtn = new Button("Set");
        setBtn.setOnAction(e -> {
            onSet.run();
            val.setText(String.valueOf(valueGetter(label)));
        });

        HBox row = new HBox(15, lbl, val, setBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        Label tip = new Label(getTip(label));
        tip.setStyle("-fx-text-fill:" + fgColor + ";");

        box.getChildren().addAll(row, tip);
        return box;
    }

    /**
     * Build a red date card for selecting year & month
     */
    private static VBox buildDateCard() {
        String bg = "#c0392b", fg = "#ffffff";
        VBox box = new VBox(8);
        box.setPrefWidth(400);
        box.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:8;");
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label("Current Date");
        lbl.setStyle("-fx-text-fill:" + fg + "; -fx-font-size: 18;");
        Label val = new Label(DateContext.getYear() + "-" + DateContext.getMonth());
        val.setStyle("-fx-text-fill:" + fg + "; -fx-font-size: 22; -fx-font-weight:bold;");

        Button setBtn = new Button("Set");
        setBtn.setOnAction(e -> {
            Dialog<ButtonType> dlg = new Dialog<>();
            dlg.setTitle("Set Year & Month");
            ComboBox<Integer> yBox = new ComboBox<>(), mBox = new ComboBox<>();
            for (int y = 2020; y <= 2035; y++) yBox.getItems().add(y);
            for (int m = 1; m <= 12; m++) mBox.getItems().add(m);
            yBox.setValue(DateContext.getYear());
            mBox.setValue(DateContext.getMonth());
            dlg.getDialogPane().setContent(new HBox(15, new Label("Year"), yBox, new Label("Month"), mBox));
            dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dlg.showAndWait().filter(bt -> bt == ButtonType.OK).ifPresent(bt -> {
                DateContext.set(yBox.getValue(), mBox.getValue());
                val.setText(yBox.getValue() + "-" + mBox.getValue());
            });
        });

        HBox row = new HBox(15, lbl, val, setBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        Label tip = new Label("The current year and month of your financial progress.");
        tip.setStyle("-fx-text-fill:" + fg + ";");

        box.getChildren().addAll(row, tip);
        return box;
    }

    // ---------- Utility Methods ----------

    /** Prompt user to enter a new double value */
    private static void promptDouble(String title, double oldVal, java.util.function.DoubleConsumer setter) {
        TextInputDialog dlg = new TextInputDialog(String.valueOf(oldVal));
        dlg.setTitle(title);
        dlg.setHeaderText("Enter new value:");
        dlg.showAndWait().ifPresent(s -> {
            try {
                double v = Double.parseDouble(s);
                setter.accept(v);
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid number").showAndWait();
            }
        });
    }

    /** Get current value based on label */
    private static double valueGetter(String label) {
        return switch (label) {
            case "Total Assets" -> FinanceContext.getTotalAssets();
            case "Savings Goal" -> FinanceContext.getSavingsGoal();
            case "Monthly Income" -> FinanceContext.getMonthlyIncome();
            default -> 0;
        };
    }

    /** Tooltip text for each card */
    private static String getTip(String label) {
        return switch (label) {
            case "Total Assets" -> "Total assets represent your overall financial worth.";
            case "Savings Goal" -> "This is the target amount you aim to save.";
            case "Monthly Income" -> "This is the amount you earn each month.";
            default -> "";
        };
    }
}
