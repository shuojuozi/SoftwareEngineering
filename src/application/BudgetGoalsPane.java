package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import os
public class BudgetGoalsPane {

    public VBox getView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_LEFT);

        // Monthly Budget
        HBox budgetBox = new HBox(10);
        budgetBox.setAlignment(Pos.CENTER_LEFT);
        Label budgetLabel = new Label("Set a monthly budget");
        TextField budgetField = new TextField("5000");
        ComboBox<String> currencyBox1 = new ComboBox<>();
        currencyBox1.getItems().addAll("Dollar(USD)", "Pound(GBP)", "CNY(¥)");
        currencyBox1.setValue("Dollar(USD)");
        budgetBox.getChildren().addAll(budgetLabel, budgetField, currencyBox1);

        // Savings Goal
        HBox savingBox = new HBox(10);
        savingBox.setAlignment(Pos.CENTER_LEFT);
        Label savingLabel = new Label("Set a savings goal");
        TextField savingField = new TextField("500000");
        ComboBox<String> currencyBox2 = new ComboBox<>();
        currencyBox2.getItems().addAll("Dollar(USD)", "Pound(GBP)", "CNY(¥)");
        currencyBox2.setValue("Dollar(USD)");
        savingBox.getChildren().addAll(savingLabel, savingField, currencyBox2);

        // Progress Bar
        Label progressTitle = new Label("Achieve progress");
        ProgressBar progressBar = new ProgressBar(0.1); // 示例用 10%
        progressBar.setPrefWidth(200);

        // Assistant
        Label assistantLabel = new Label("Smart Budget Assistant");
        assistantLabel.setFont(new Font(18));
        Label exampleLabel = new Label("For example, set a monthly budget of $5,000 and a savings goal");
        Label aiRecommendLabel = new Label("AI recommends saving suggestions");

        // Bottom Message Input
        TextField messageField = new TextField();
        messageField.setPromptText("Send me questions when you log in.");

        container.getChildren().addAll(
                budgetBox, savingBox, progressTitle, progressBar,
                assistantLabel, exampleLabel, aiRecommendLabel,
                messageField
        );

        return container;
    }
}
