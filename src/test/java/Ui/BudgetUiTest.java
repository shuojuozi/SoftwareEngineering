package Ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BudgetUiTest extends BaseJavaFXTest {

    @Test
    public void testBudgetUiContainsCardsAndChatComponents() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        final boolean[] assertions = new boolean[3];

        Platform.runLater(() -> {
            VBox layout = BudgetUi.createDashboardPane();

            assertions[0] = containsNodeOfType(layout, HBox.class);       // cards
            assertions[1] = containsNodeOfType(layout, TextField.class);  // chat input
            assertions[2] = containsNodeOfType(layout, WebView.class);    // chat WebView

            latch.countDown();
        });

        latch.await();  // 等待 UI 执行完毕

        assertTrue(assertions[0], "BudgetUi should contain a HBox for cards.");
        assertTrue(assertions[1], "BudgetUi should contain a TextField for chat input.");
        assertTrue(assertions[2], "BudgetUi should contain a WebView for chat display.");
    }

    private boolean containsNodeOfType(Parent parent, Class<? extends Node> type) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (type.isInstance(node)) {
                return true;
            } else if (node instanceof Parent p && containsNodeOfType(p, type)) {
                return true;
            }
        }
        return false;
    }
}
