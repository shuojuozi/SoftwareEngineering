package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeepSeekTest {

    @Test
    public void testChatReturnsMessageOrError() {
        // Test condition: sessionId is valid, userInput is meaningful
        String sessionId = "test_session";
        String userInput = "What is a good way to save money?";

        String response = DeepSeek.chat(sessionId, userInput);
        assertNotNull(response);
        assertFalse(response.isBlank(), "Response should not be blank");

        // Additional assertions can be added, e.g., check if response contains keywords like "recommendation" (optional)
    }
}
