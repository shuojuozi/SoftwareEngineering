package Ui;

import javafx.scene.chart.LineChart;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextArea;
import javafx.scene.Node;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AnalysisUiTest extends BaseJavaFXTest {

    @Test
    public void testAnalysisDashboardContainsLineChart() {
        VBox pane = AnalysisUi.createDashboardPane();
        boolean hasLineChart = pane.getChildren().stream()
                .anyMatch(n -> n instanceof LineChart);
        assertTrue(hasLineChart, "Dashboard should contain a LineChart");
    }

    @Test
    public void testAnalysisDashboardContainsTextArea() {
        VBox pane = AnalysisUi.createDashboardPane();
        boolean hasTextArea = pane.getChildren().stream()
                .anyMatch(n -> n instanceof TextArea);
        assertTrue(hasTextArea, "Dashboard should contain a TextArea for analysis");
    }
}
