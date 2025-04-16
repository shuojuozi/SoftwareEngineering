import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;

public class Sidebar {

    private Label dashboardLabel;
    private Label classifiedManagementLabel;
    private Label tradeManagementLabel;
    private Label transactionDetailsLabel;
    private Label budgetLabel;
    private Label analysisLabel;

    // 构造器
    public Sidebar() {
        // 创建 Label 标签，代替 Button 按钮
        dashboardLabel = new Label("\uD83D\uDCCADashboard");
        classifiedManagementLabel = new Label("Classified management of expenditure");
        tradeManagementLabel = new Label("Trade management");
        transactionDetailsLabel = new Label("Transaction details");
        budgetLabel = new Label("Budgeting and savings goals");
        analysisLabel = new Label("Analysis");

        // 为标签添加点击事件
        dashboardLabel.setOnMouseClicked(this::handleDashboardClick);
        classifiedManagementLabel.setOnMouseClicked(this::handleClassifiedManagementClick);
        tradeManagementLabel.setOnMouseClicked(this::handleTradeManagementClick);
        transactionDetailsLabel.setOnMouseClicked(this::handleTransactionDetailsClick);
        budgetLabel.setOnMouseClicked(this::handleBudgetClick);
        analysisLabel.setOnMouseClicked(this::handleAnalysisClick);
    }

    // 获取左侧导航栏
    public VBox getSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #f8f9fa;");

        sidebar.getChildren().addAll(
                dashboardLabel,  // Dashboard标签
                tradeManagementLabel,  // 交易管理
                transactionDetailsLabel,  // 交易详情
                classifiedManagementLabel,  // 分类管理标签
                budgetLabel,  // 预算目标
                analysisLabel,  // 数据分析
                new Label("👤 Tester")
        );

        return sidebar;
    }

    // 返回各个标签
    public Label getDashboardLabel() {
        return dashboardLabel;
    }

    public Label getClassifiedManagementLabel() {
        return classifiedManagementLabel;
    }

    public Label getTradeManagementLabel() {
        return tradeManagementLabel;
    }

    public Label getTransactionDetailsLabel() {
        return transactionDetailsLabel;
    }

    public Label getBudgetLabel() {
        return budgetLabel;
    }

    public Label getAnalysisLabel() {
        return analysisLabel;
    }

    // 点击事件处理，设置高亮效果
    private void handleDashboardClick(MouseEvent event) {
        System.out.println("Dashboard clicked!");
        setHighlight(dashboardLabel);
        // 通知Main页面切换到Dashboard内容
    }

    private void handleClassifiedManagementClick(MouseEvent event) {
        System.out.println("Classified Management clicked!");
        setHighlight(classifiedManagementLabel);
        // 通知Main页面切换到分类管理页面
    }

    private void handleTradeManagementClick(MouseEvent event) {
        System.out.println("Trade Management clicked!");
        setHighlight(tradeManagementLabel);
        // 通知Main页面切换到交易管理页面
    }

    private void handleTransactionDetailsClick(MouseEvent event) {
        System.out.println("Transaction Details clicked!");
        setHighlight(transactionDetailsLabel);
        // 通知Main页面切换到交易详情页面
    }

    private void handleBudgetClick(MouseEvent event) {
        System.out.println("Budgeting and Savings Goals clicked!");
        setHighlight(budgetLabel);
        // 通知Main页面切换到预算页面
    }

    private void handleAnalysisClick(MouseEvent event) {
        System.out.println("Analysis clicked!");
        setHighlight(analysisLabel);
        // 通知Main页面切换到分析页面
    }

    // 设置点击高亮效果
    private void setHighlight(Label clickedLabel) {
        // Reset the background color of all labels
        resetHighlight();

        // Set the background color of the clicked label to blue
        clickedLabel.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 10px;");
    }

    // 重置所有标签的背景颜色
    private void resetHighlight() {
        // Reset the background color of all labels to none
        dashboardLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: #13b0cc;");
        classifiedManagementLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
        tradeManagementLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
        transactionDetailsLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
        budgetLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
        analysisLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
    }
}
