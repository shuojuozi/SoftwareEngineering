package utils;

import pojo.Transaction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CalcExpense {

    // Date format matches transactionTime format in JSON
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Summarize total income and expense within the first 7 and 30 days
     * starting from the earliest transaction date (billing start date).
     */
    public static Map<String, Map<String, Double>> summarizeByBillingCycle(List<Transaction> transactions) {
        // 1. Get the billing start date (earliest transaction date)
        Optional<LocalDate> startDateOpt = transactions.stream()
                .map(t -> parseTransactionDate(t.getTransactionTime()))
                .min(Comparator.naturalOrder());

        if (startDateOpt.isEmpty()) {
            return Map.of(); // Return empty if no transaction data
        }

        LocalDate startDate = startDateOpt.get();
        LocalDate endDate7Days = startDate.plusDays(6);   // 7-day window (inclusive)
        LocalDate endDate30Days = startDate.plusDays(29); // 30-day window (inclusive)

        // 2. Calculate total amounts within 7-day and 30-day ranges
        Map<String, Double> stats7Days = filterAndSum(transactions, startDate, endDate7Days);
        Map<String, Double> stats30Days = filterAndSum(transactions, startDate, endDate30Days);

        return Map.of(
                "7Days", stats7Days,
                "30Days", stats30Days
        );
    }

    /**
     * Summarize expense amount and proportion by transaction type
     * @return Map structure:
     *         {
     *             "Dining Amount": 500.00,
     *             "Dining Percentage": "25.00%",
     *             "Shopping Amount": 1500.00,
     *             "Shopping Percentage": "75.00%"
     *         }
     */
    public static Map<String, Object> summarizeExpenseByCategory(List<Transaction> transactions) {
        // 1. Filter expense records and group by transaction type
        Map<String, Double> expenseByType = transactions.stream()
                .filter(t -> "\"支出\"".equals(t.getIncExp()))
                .collect(Collectors.groupingBy(
                        Transaction::getTransactionType,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        // 2. Calculate total expense amount
        double totalExpense = expenseByType.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        // 3. Construct result with amount and percentage
        Map<String, Object> result = new HashMap<>();
        expenseByType.forEach((type, amount) -> {
            // Format amount to 2 decimal places
            result.put(type + " Amount", String.format("%.2f", amount));

            // Calculate percentage (avoid division by zero)
            if (totalExpense != 0) {
                double percentage = (amount / totalExpense) * 100;
                result.put(type + " Percentage", String.format("%.2f%%", percentage));
            } else {
                result.put(type + " Percentage", "0.00%");
            }
        });

        return result;
    }

    /**
     * Filter transactions within a date range and summarize by income/expense
     */
    private static Map<String, Double> filterAndSum(List<Transaction> transactions,
                                                    LocalDate startDate,
                                                    LocalDate endDate) {
        return transactions.stream()
                .filter(t -> isWithinRange(t, startDate, endDate))
                .collect(Collectors.groupingBy(
                        Transaction::getIncExp,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    /**
     * Parse transaction date (ignoring time part)
     */
    private static LocalDate parseTransactionDate(String transactionTime) {
        try {
            return LocalDateTime.parse(transactionTime, DATE_TIME_FORMATTER).toLocalDate();
        } catch (Exception e) {
            System.err.println("Date parsing failed: " + transactionTime);
            return LocalDate.MAX; // Use max date to avoid affecting min() calculation
        }
    }

    /**
     * Check if a transaction falls within the specified date range
     */
    private static boolean isWithinRange(Transaction t, LocalDate start, LocalDate end) {
        LocalDate date = parseTransactionDate(t.getTransactionTime());
        return !date.isBefore(start) && !date.isAfter(end);
    }
}
