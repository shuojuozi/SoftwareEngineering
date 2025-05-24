package Ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionUiTest extends BaseJavaFXTest {

    @Test
    public void testTransactionDetailPageStructure() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final VBox[] detailPage = new VBox[1];

        Platform.runLater(() -> {
            detailPage[0] = TransactionUi.createTransactionDetailPage();
            latch.countDown();
        });

        latch.await();

        assertNotNull(detailPage[0], "Transaction detail page should not be null");

        assertTrue(containsLabelText(detailPage[0], "Transaction Details"), "Should contain header 'Transaction Details'");
        assertTrue(containsLabelText(detailPage[0], "Date:"), "Should contain 'Date:' label");
        assertTrue(containsLabelText(detailPage[0], "Amount:"), "Should contain 'Amount:' label");
        assertTrue(containsLabelText(detailPage[0], "Modification History"), "Should contain 'Modification History'");
        assertTrue(containsButtonWithText(detailPage[0], "Edit"), "Should contain 'Edit' button");
        assertTrue(containsButtonWithText(detailPage[0], "Delete"), "Should contain 'Delete' button");
    }

    private boolean containsLabelText(Parent parent, String expectedText) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Label label && expectedText.equals(label.getText())) {
                return true;
            } else if (node instanceof Parent child && containsLabelText(child, expectedText)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsButtonWithText(Parent parent, String expectedText) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Button button && expectedText.equals(button.getText())) {
                return true;
            } else if (node instanceof Parent child && containsButtonWithText(child, expectedText)) {
                return true;
            }
        }
        return false;
    }
}
