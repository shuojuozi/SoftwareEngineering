package Ui;

import javafx.scene.layout.VBox;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.Node;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DashBoardUiTest extends BaseJavaFXTest {

    @Test
    public void testDashboardContainsPieChart() {
        VBox pane = DashBoardUi.createDashboardPane(2025, 4);
        boolean hasPieChart = pane.lookupAll(".chart").stream()
                .anyMatch(n -> n instanceof PieChart);
        assertTrue(hasPieChart, "Dashboard should contain a PieChart");
    }

    @Test
    public void testDashboardContainsBarChart() {
        VBox pane = DashBoardUi.createDashboardPane(2025, 4);
        boolean hasBarChart = pane.lookupAll(".chart").stream()
                .anyMatch(n -> n instanceof BarChart);
        assertTrue(hasBarChart, "Dashboard should contain a BarChart");
    }
}
