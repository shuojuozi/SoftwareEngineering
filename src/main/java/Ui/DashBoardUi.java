package Ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import pojo.Transaction;
import utils.JsonUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 更新版 DashBoardUi —— 同步分类兼容 & 动态年月
 */
class DashBoardUi extends NavigationSuper {
    public static VBox createDashboardPane(){
        LocalDate now = LocalDate.now();
        List<Transaction> txs = JsonUtils.getTransactionsByMonth(now.getYear(), now.getMonthValue());
        Map<String, Double> cat = new HashMap<>(); double total=0;
        for(Transaction t:txs){
            String c=normalize(t.getTransactionType());
            cat.merge(c,t.getAmount(),Double::sum); total+=t.getAmount(); }
        double housing=cat.getOrDefault("housing",0.0), dining=cat.getOrDefault("food and dining",0.0), entertainment=cat.getOrDefault("entertainment",0.0), transport=cat.getOrDefault("transportation",0.0), shopping=cat.getOrDefault("shopping",0.0), health=cat.getOrDefault("healthcareeducation and training",0.0), communication=cat.getOrDefault("communication",0.0), investment=cat.getOrDefault("finance and investment",0.0), transfer=cat.getOrDefault("transfer accounts",0.0);
        /* 假设值 */
        double totalAssets=10000, monthlyIncome=2500, savingsGoal=15000, progress=(totalAssets+monthlyIncome-total)/savingsGoal;
        HBox summary=new HBox(20, info("Total Assets",totalAssets,"#cce5ff","#004085"), info("Monthly Expense",total,"#f8d7da","#721c24"), info("Monthly Income",monthlyIncome,"#d4edda","#155724"), info("Savings Goal",savingsGoal,"#f8d7da","#721c24"), info("Goal Progress",progress*100,"#fff3cd","#856404")); summary.setPadding(new Insets(20)); summary.setAlignment(Pos.CENTER);
        CategoryAxis x=new CategoryAxis(); NumberAxis y=new NumberAxis(); BarChart<String,Number> bar=new BarChart<>(x,y);
        XYChart.Series<String,Number> ser=new XYChart.Series<>(); ser.setName(now.getMonth().toString()); ser.getData().addAll(new XYChart.Data<>("Housing",housing),new XYChart.Data<>("Dining",dining),new XYChart.Data<>("Entertainment",entertainment),new XYChart.Data<>("Transport",transport),new XYChart.Data<>("Shopping",shopping),new XYChart.Data<>("Health",health),new XYChart.Data<>("Communication",communication),new XYChart.Data<>("Investment",investment),new XYChart.Data<>("Transfer",transfer)); bar.getData().add(ser);
        PieChart pie=new PieChart(); pie.setTitle("Spending"); pie.getData().addAll(new PieChart.Data("Housing",housing),new PieChart.Data("Dining",dining),new PieChart.Data("Entertainment",entertainment),new PieChart.Data("Transport",transport),new PieChart.Data("Shopping",shopping),new PieChart.Data("Health",health),new PieChart.Data("Communication",communication),new PieChart.Data("Investment",investment),new PieChart.Data("Transfer",transfer));
        HBox charts=new HBox(20,bar,pie); charts.setPadding(new Insets(20)); charts.setAlignment(Pos.CENTER);
        return new VBox(summary,charts);
    }
    private static String normalize(String s){ return s==null?"":s.replace("\"","" ).toLowerCase().trim(); }
    private static VBox info(String t, double v, String bg, String fg){ VBox b=new VBox(10); b.setStyle("-fx-background-color:"+bg+";-fx-background-radius:10;-fx-padding:10;"); Label l1=new Label(t); l1.setFont(new Font(16)); l1.setStyle("-fx-text-fill:"+fg); Label l2=new Label(String.format(v%1.0==v?"$%,.0f":"$%,.2f",v)); l2.setFont(new Font(20)); l2.setStyle("-fx-text-fill:"+fg); b.getChildren().addAll(l1,l2); return b; }
}

