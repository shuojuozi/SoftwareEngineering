package Ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class TradeListUiTest extends BaseJavaFXTest {

    @Test
    public void testTransactionListPageStructure() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final VBox[] listPage = new VBox[1];

        Platform.runLater(() -> {
            listPage[0] = TradeListUi.createTransactionListPage();
            latch.countDown();
        });

        latch.await();

        VBox root = listPage[0];
        assertNotNull(root, "Transaction list page should not be null");

        // 验证是否包含“Transaction List”标题
        assertTrue(containsLabelText(root, "Transaction List"), "Should contain 'Transaction List' label");

        // 验证是否包含 TextField 搜索框
        assertTrue(containsNodeOfType(root, TextField.class), "Should contain TextField for search");

        // 验证是否包含 Search 按钮
        assertTrue(containsButtonWithText(root, "Search"), "Should contain 'Search' button");

        // 验证是否包含 TableView（用于交易记录）
        assertTrue(containsNodeOfType(root, TableView.class), "Should contain TableView for transactions");
    }

    private boolean containsLabelText(Node parent, String expected) {
        if (parent instanceof Label label) {
            return expected.equals(label.getText());
        } else if (parent instanceof javafx.scene.Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                if (containsLabelText(child, expected)) return true;
            }
        }
        return false;
    }

    private boolean containsButtonWithText(Node parent, String text) {
        if (parent instanceof Button btn) {
            return text.equals(btn.getText());
        } else if (parent instanceof javafx.scene.Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                if (containsButtonWithText(child, text)) return true;
            }
        }
        return false;
    }

    private boolean containsNodeOfType(Node parent, Class<?> type) {
        if (type.isInstance(parent)) {
            return true;
        } else if (parent instanceof javafx.scene.Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                if (containsNodeOfType(child, type)) return true;
            }
        }
        return false;
    }
}
