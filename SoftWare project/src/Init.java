import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Init extends Application {

    @Override
    public void start(Stage stage) {
        // 左侧导航栏
        Sidebar sidebar = new Sidebar();
        // 主界面区域（Dashboard）
        Dashboard dashboard = new Dashboard();

        // 布局设置
        BorderPane root = new BorderPane();
        root.setLeft(sidebar.getSidebar()); // 添加左侧导航栏
        root.setCenter(dashboard.getDashboardContent()); // 默认展示主界面内容

        // 为左侧导航栏的 Label 绑定事件
        sidebar.getDashboardLabel().setOnMouseClicked(event -> {
            root.setCenter(dashboard.getDashboardContent()); // 切换到 Dashboard 页面
        });

        sidebar.getClassifiedManagementLabel().setOnMouseClicked(event -> {
            ClassifiedManagement classifiedManagement = new ClassifiedManagement();
            root.setCenter(classifiedManagement.getClassifiedManagementContent()); // 切换到分类管理页面
        });

        sidebar.getTradeManagementLabel().setOnMouseClicked(event -> {
            // 这里可以添加其他页面的内容切换逻辑
            System.out.println("Trade Management clicked!");
        });

        sidebar.getTransactionDetailsLabel().setOnMouseClicked(event -> {
            // 交易详情的页面切换逻辑
            System.out.println("Transaction Details clicked!");
        });

        sidebar.getBudgetLabel().setOnMouseClicked(event -> {
            // 预算页面的内容切换
            System.out.println("Budget clicked!");
        });

        sidebar.getAnalysisLabel().setOnMouseClicked(event -> {
            // 数据分析的内容切换
            System.out.println("Analysis clicked!");
        });

        // 创建场景
        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.setTitle("Financial Dashboard");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
