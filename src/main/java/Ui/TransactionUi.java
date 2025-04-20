package Ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TransactionUi extends Ui.NavigationSuper {
    private BorderPane root;

    public TransactionUi() {
        root = new BorderPane();
        root.setLeft(createSidebar());
        root.setRight(createTransactionDetailPage());
    }

    @Override
    public void start(Stage primaryStage) {
        // 创建主布局
        root = new BorderPane();
        // 添加侧边栏
        root.setLeft(createSidebar());
        // 初始化默认显示交易详情页
        root.setCenter(createTransactionDetailPage());

        // 创建场景并设置舞台
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Transaction Details");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    // 创建交易详情页面
    public static VBox createTransactionDetailPage() {
        VBox mainContent = new VBox();
        mainContent.setPadding(new Insets(20));

        // 标题和按钮区域
        HBox header = new HBox();
        header.setSpacing(10);
        header.setStyle("-fx-background-color: #007bff; -fx-padding: 10;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Transaction Details");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(new Font(24));

        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // 使按钮靠右
        header.getChildren().addAll(titleLabel, spacer, editButton, deleteButton);

        // 交易详情区域
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

        // 修改历史区域
        VBox modificationHistory = new VBox();
        modificationHistory.setPadding(new Insets(20));
        modificationHistory.setSpacing(5);
        modificationHistory.getChildren().add(new Label("Modification History"));
        modificationHistory.getChildren().add(new Label("2025-04-14: Transaction created."));
        modificationHistory.getChildren().add(new Label("2025-04-15: Amount changed from 50.00 to 100.00."));

        // 汇总所有部分
        mainContent.getChildren().addAll(header, transactionDetails, modificationHistory);

        return mainContent;
    }


}
