package Ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsUiTest extends BaseJavaFXTest {

    @Test
    public void testSettingsPaneStructure() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final VBox[] settingsPane = new VBox[1];

        Platform.runLater(() -> {
            settingsPane[0] = SettingsUi.createSettingsPane();
            latch.countDown();
        });

        latch.await();

        assertNotNull(settingsPane[0], "Settings pane should not be null");

        boolean hasSettingsTitle = containsLabelText(settingsPane[0], "⚙ Settings");
        boolean hasApiKeyField = containsNodeOfType(settingsPane[0], PasswordField.class);
        boolean hasSaveButton = containsButtonWithText(settingsPane[0], "Save API Key");
        boolean hasCardTip = containsLabelText(settingsPane[0], "This is the target amount you aim to save.");

        assertTrue(hasSettingsTitle, "Should contain title '⚙ Settings'");
        assertTrue(hasApiKeyField, "Should contain PasswordField for API Key");
        assertTrue(hasSaveButton, "Should contain Save API Key button");
        assertTrue(hasCardTip, "Should include finance tip label");
    }

    private boolean containsLabelText(Parent parent, String expectedText) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Label label && expectedText.equals(label.getText())) {
                return true;
            } else if (node instanceof Parent p && containsLabelText(p, expectedText)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsNodeOfType(Parent parent, Class<? extends Node> clazz) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (clazz.isInstance(node)) {
                return true;
            } else if (node instanceof Parent p && containsNodeOfType(p, clazz)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsButtonWithText(Parent parent, String text) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Button button && text.equals(button.getText())) {
                return true;
            } else if (node instanceof Parent p && containsButtonWithText(p, text)) {
                return true;
            }
        }
        return false;
    }
}
