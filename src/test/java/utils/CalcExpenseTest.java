package utils;

import pojo.Transaction;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CalcExpenseTest {

    @Test
    public void testSummarizeByBillingCycle() {
        List<Transaction> transactions = new ArrayList<>();

        transactions.add(makeTransaction("2025-05-10 10:00:00", "支出", 100));
        transactions.add(makeTransaction("2025-05-10 12:00:00", "收入", 300));
        transactions.add(makeTransaction("2025-05-10 09:00:00", "支出", 50));
        transactions.add(makeTransaction("2025-05-23 08:00:00", "支出", 150));
        transactions.add(makeTransaction("2025-05-23 15:00:00", "收入", 300));

        Map<String, Map<String, Double>> result = CalcExpense.summarizeByBillingCycle(transactions);

        // Validate 7-day summary
        Map<String, Double> stats7 = result.get("7Days");
        assertEquals(150.0, stats7.get("\"支出\""), 0.001);
        assertEquals(300.0, stats7.get("\"收入\""), 0.001);

        // Validate 30-day summary
        Map<String, Double> stats30 = result.get("30Days");
        assertEquals(300.0, stats30.get("\"支出\""), 0.001);
        assertEquals(600.0, stats30.get("\"收入\""), 0.001);
    }

    /**
     * Helper method to construct a Transaction object
     */
    private Transaction makeTransaction(String time, String incExp, double amount) {
        Transaction tx = new Transaction();
        tx.setTransactionTime(time);
        tx.setIncExp("\"" + incExp + "\""); // Add quotes to match CalcExpense condition
        tx.setAmount(amount);
        return tx;
    }
}
