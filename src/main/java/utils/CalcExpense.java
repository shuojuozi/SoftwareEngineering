package utils;

import pojo.Transaction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CalcExpense {

        // 日期格式与 JSON 中的 transactionTime 一致
        private static final DateTimeFormatter DATE_TIME_FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 统计从账单开始日期（最早交易日期）起的7天和30天内的收支总和
     */
    public static Map<String, Map<String, Double>> summarizeByBillingCycle(List<Transaction> transactions) {
        // 1. 获取账单开始日期（最早的交易日期）
        Optional<LocalDate> startDateOpt = transactions.stream()
                .map(t -> parseTransactionDate(t.getTransactionTime()))
                .min(Comparator.naturalOrder());

        if (startDateOpt.isEmpty()) {
            return Map.of(); // 无交易数据时返回空结果
        }

        LocalDate startDate = startDateOpt.get();
        LocalDate endDate7Days = startDate.plusDays(6);   // 第7天（含开始当天）
        LocalDate endDate30Days = startDate.plusDays(29); // 第30天（含开始当天）

        // 2. 统计7天和30天内的金额
        Map<String, Double> stats7Days = filterAndSum(transactions, startDate, endDate7Days);
        Map<String, Double> stats30Days = filterAndSum(transactions, startDate, endDate30Days);

        return Map.of(
                "7Days", stats7Days,
                "30Days", stats30Days
        );
    }


    /**
     * 按交易类型统计支出金额及占比
     * @return Map结构:
     *         {
     *             "餐饮金额": 500.00,
     *             "餐饮支出占比": "25.00%",
     *             "购物金额": 1500.00,
     *             "购物支出占比": "75.00%"
     *         }
     */
    public static Map<String, Object> summarizeExpenseByCategory(List<Transaction> transactions) {
        // 1. 过滤出所有支出记录，并按交易类型分组求和
        Map<String, Double> expenseByType = transactions.stream()
                .filter(t -> "\"支出\"".equals(t.getIncExp()))
                .collect(Collectors.groupingBy(
                        Transaction::getTransactionType,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        // 2. 计算总支出金额
        double totalExpense = expenseByType.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        // 3. 构建结果（金额 + 占比）
        Map<String, Object> result = new HashMap<>();
        expenseByType.forEach((type, amount) -> {
            // 金额保留两位小数
            result.put(type + "金额", String.format("%.2f", amount));

            // 计算占比（避免除零）
            if (totalExpense != 0) {
                double percentage = (amount / totalExpense) * 100;
                result.put(type + "支出占比", String.format("%.2f%%", percentage));
            } else {
                result.put(type + "支出占比", "0.00%");
            }
        });

        return result;
    }



    /**
     * 过滤指定日期范围内的交易并统计金额
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
     * 解析交易日期（忽略时间部分）
     */
    private static LocalDate parseTransactionDate(String transactionTime) {
        try {
            return LocalDateTime.parse(transactionTime, DATE_TIME_FORMATTER).toLocalDate();
        } catch (Exception e) {
            System.err.println("日期解析失败: " + transactionTime);
            return LocalDate.MAX; // 无效日期置为最大值，避免干扰 min() 计算
        }
    }

    /**
     * 判断交易是否在日期范围内
     */
    private static boolean isWithinRange(Transaction t, LocalDate start, LocalDate end) {
        LocalDate date = parseTransactionDate(t.getTransactionTime());
        return !date.isBefore(start) && !date.isAfter(end);
    }
}