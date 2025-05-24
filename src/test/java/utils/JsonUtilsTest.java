package utils;

import org.junit.jupiter.api.Test;
import pojo.Transaction;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilsTest {

    @Test
    public void shouldReturnEmptyListWhenFileDoesNotExist() {
        List<Transaction> result = JsonUtils.readTransactionsFromClasspath("nonexistent.json");
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list if the file does not exist");
    }

    @Test
    public void shouldReadFromClasspathWhenFileInResources() {
        List<Transaction> result = JsonUtils.readTransactionsFromClasspath("transactionData.json");
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Should read data from classpath if file exists");
        assertEquals("2025030523001482391420276859", result.get(0).getTransactionId());
    }

    @Test
    public void shouldHandleIOExceptionWhenDirectoryDoesNotExist() {
        List<Transaction> list = List.of(new Transaction());

        String invalidPath = "nonexistent_dir/output.json";

        assertDoesNotThrow(() -> {
            JsonUtils.writeTransactionsToJson(list, invalidPath);
        });
    }
}
