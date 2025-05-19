package Ui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class UserUi extends NavigationSuper {

    public static double totalAssets = 10000;
    public static double savingsGoal = 15000;
    public static double monthlyIncome = 2500;
    public static int year = 2025;
    public static int month = 2;
    public static String currentDate = year + "-" + month;

    public static HBox createDashboardPane() {
        VBox mainPane = new VBox(50);
        mainPane.setStyle("-fx-padding: 50 20 20 20; -fx-background-color: #f4f4f9;");

        VBox totalAssetsBox = createInfoBox("Total Assets", totalAssets, "#4e79b5", "Total assets represent your overall financial worth.");
        VBox savingsGoalBox = createInfoBox("Savings Goal", savingsGoal, "#2ecc71", "This is the target amount you aim to save.");
        VBox monthlyIncomeBox = createInfoBox("Monthly Income", monthlyIncome, "#f39c12", "This is the amount you earn each month.");
        VBox dateBox = createInfoBox("Current Date", currentDate, "#e74c3c", "The current year and month of your financial progress.");

        HBox firstRow = new HBox(100, totalAssetsBox, savingsGoalBox);
        firstRow.setStyle("-fx-alignment: center;");
        HBox secondRow = new HBox(100, monthlyIncomeBox, dateBox);
        secondRow.setStyle("-fx-alignment: center;");

        mainPane.getChildren().addAll(firstRow, secondRow);

        HBox centerAlign = new HBox(mainPane);
        centerAlign.setStyle("-fx-alignment: center; -fx-fill-height: true;");
        mainPane.setStyle("-fx-background-color: white; -fx-padding: 50 20 20 20;");

        return centerAlign;
    }

    private static VBox createInfoBox(String labelText, Object value, String backgroundColor, String description) {
        HBox infoBox = new HBox(20);
        infoBox.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-background-color: " + backgroundColor + "; -fx-border-radius: 10px;");

        Text label = new Text(labelText);
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Text valueText = new Text(value.toString());
        valueText.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        Button setButton = new Button("Set");
        setButton.setStyle("-fx-background-color: white; -fx-text-fill: " + backgroundColor + "; -fx-padding: 5 10;");
        setButton.setMinWidth(50); // 设置最小宽度，避免文字裁切
        setButton.setMaxWidth(Double.MAX_VALUE); // 允许最大扩展
        setButton.setPrefWidth(Button.USE_COMPUTED_SIZE); // 使用计算宽度
        HBox.setHgrow(setButton, javafx.scene.layout.Priority.ALWAYS); // 自动扩展

        setButton.setOnAction(e -> showInputDialog(labelText, valueText));

        infoBox.getChildren().addAll(label, valueText, setButton);
        infoBox.setPrefWidth(350); // 原为300，增加宽度
        infoBox.setPrefHeight(120);

        Text descriptionText = new Text(description);
        descriptionText.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: #555555; -fx-padding: 10 0 0 0;");

        Text descriptionTitle = new Text(labelText + " Explanation");
        descriptionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        VBox container = new VBox(10, descriptionTitle, infoBox, descriptionText);
        container.setStyle("-fx-alignment: center;");
        return container;
    }

    private static void showInputDialog(String labelText, Text valueText) {
        Stage inputDialog = new Stage();
        inputDialog.setTitle("Set " + labelText);

        TextField inputField = new TextField();
        inputField.setPromptText("Enter new value");
        inputField.setText(valueText.getText());

        Button confirmButton = new Button("Confirm");
        confirmButton.setStyle("-fx-background-color: #4e79b5; -fx-text-fill: white;");
        confirmButton.setOnAction(e -> {
            String newValue = inputField.getText();
            switch (labelText) {
                case "Total Assets":
                    totalAssets = Double.parseDouble(newValue);
                    break;
                case "Savings Goal":
                    savingsGoal = Double.parseDouble(newValue);
                    break;
                case "Monthly Income":
                    monthlyIncome = Double.parseDouble(newValue);
                    break;
                case "Current Date":
                    String[] dateParts = newValue.split("-");
                    if (dateParts.length == 2) {
                        try {
                            year = Integer.parseInt(dateParts[0]);
                            month = Integer.parseInt(dateParts[1]);
                            currentDate = newValue;
                        } catch (NumberFormatException ex) {
                            // Invalid input, ignored
                        }
                    }
                    break;
                default:
                    break;
            }
            valueText.setText(newValue);
            inputDialog.close();
        });

        VBox dialogBox = new VBox(10);
        dialogBox.setStyle("-fx-padding: 20; -fx-background-color: #f4f4f9;");
        dialogBox.getChildren().addAll(inputField, confirmButton);

        Scene dialogScene = new Scene(dialogBox, 250, 150);
        inputDialog.setScene(dialogScene);
        inputDialog.show();
    }
}
