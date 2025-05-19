package utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.chart.XYChart;
import pojo.Transaction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String DATA_JSON_PATH = "src/main/resources/data/transactionData.json";

    private static final String TEMP_JSON_PATH = "src/main/resources/data/temp.json";

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * 从类路径下的 JSON 文件读取交易数据
     *
     * @param resourcePath 类路径下的资源文件路径（如 "transactions.json"）
     * @return 交易记录列表（如果文件不存在或解析失败，返回空列表）
     */
    public static List<Transaction> readTransactionsFromClasspath(String resourcePath) {
//        try (InputStream inputStream = JsonUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
//            if (inputStream == null) {
//                System.err.println("未找到文件: " + resourcePath);
//                return Collections.emptyList();
//            }
//            // 将 JSON 数据解析为 List<Transaction>
//            return objectMapper.readValue(inputStream, new TypeReference<List<Transaction>>() {});
//        } catch (IOException e) {
//            System.err.println("解析 JSON 失败: " + e.getMessage());
//            return Collections.emptyList();
//        }
        String filePath = "src/main/resources/data/" + resourcePath;
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            if (fileInputStream == null) {
                System.err.println("未找到文件: " + filePath);
                return Collections.emptyList();
            }
            // 将 JSON 数据解析为 List<Transaction>
            return objectMapper.readValue(fileInputStream, new TypeReference<List<Transaction>>() {});
        } catch (Exception e) {
            System.err.println("解析 JSON 失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 从文件系统路径读取 JSON 数据
     *
     * @param jsonPath 文件路径
     */
    private static List<Transaction> readJsonFile(String jsonPath) throws IOException {
        File f = new File(jsonPath);
        if (!f.exists()) {
            System.err.println("文件不存在: " + jsonPath);
            return Collections.emptyList();
        }
        return objectMapper.readValue(f, new TypeReference<List<Transaction>>() {});
    }

    /**
     * 读取多个 JSON 文件，合并并去重。冲突策略：如果 transactionId 相同，则保留“交易时间较晚”的那条。
     * 可根据需求自行更改逻辑。(合并后顺序会改变)
     */
    public static List<Transaction> mergeAndRemoveDuplicates(List<String> jsonPaths) throws IOException {
        // 用 Map<transactionId, Transaction> 来存储最终结果，以 transactionId 去重
        Map<String, Transaction> map = new HashMap<>();

        for (String path : jsonPaths) {
            List<Transaction> tempList = readJsonFile(path);
            for (Transaction tx : tempList) {
                String txId = tx.getTransactionId();
                if (txId == null || txId.isEmpty()) {
                    // 若没有 transactionId，可视需求处理，这里先放进去
                    // 也可以用别的字段做组合唯一键
                    map.put(UUID.randomUUID().toString(), tx);
                } else {
                    // 检查是否已存在相同 transactionId
                    if (!map.containsKey(txId)) {
                        // 不存在，直接放入
                        map.put(txId, tx);
                    } else {
                        // 已存在 -> 进行冲突处理
                        Transaction existing = map.get(txId);
                        // 这里示例：保留“交易时间更晚”的那条
                        // 也可以比较金额、或合并备注等，按需求自定义
                        Date dNew = parseDate(tx.getTransactionTime());
                        Date dOld = parseDate(existing.getTransactionTime());

                        if (dNew != null && dOld != null) {
                            if (dNew.after(dOld)) {
                                // 如果新记录时间更晚，就替换掉旧的
                                map.put(txId, tx);
                            }
                        } else if (dOld == null && dNew != null) {
                            // 旧记录没有时间，新记录有时间 -> 用新记录
                            map.put(txId, tx);
                        }
                        // 如果 dNew < dOld 或相等，则什么都不做，保留旧记录
                    }
                }
            }
        }
        // Map 转换回 List
        return new ArrayList<>(map.values());
    }


    /**
     * 解析交易时间
     */
    private static Date parseDate(String timeStr) {
        if (timeStr == null) return null;
        // 去引号之类
        timeStr = timeStr.replace("\"", "").trim();
        try {
            return sdf.parse(timeStr);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 根据transactionId来从transaction列表中找到指定的transaction
     *
     * @param transactions 交易记录列表
     * @param transactionId 交易记录的id
     * @return 一个交易记录的对象
     */
    public static Transaction findTransactionById(List<Transaction> transactions, String transactionId) {
        if (transactions.isEmpty()) {
            System.out.println("transactions list is empty");
            return null;
        }
        System.out.println(transactionId);
        for (Transaction transaction : transactions) {
            if (transaction.getTransactionId().equals(transactionId)) {
                return transaction;
            }
        }

        System.out.println("no id matched");
        return null;
    }

    /**
     * 把csv文件转换为json文件并且存储到resources/data/data.json中
     * 示例：从类似微信的 CSV 文件中清洗并生成 JSON
     * 列顺序为：
     *  0: 交易时间
     *  1: 交易类型
     *  2: 交易对方
     *  3: 商品
     *  4: 收/支
     *  5: 金额(元)
     *  6: 支付方式
     *  7: 当前状态
     *  8: 交易单号
     *  9: 商户单号
     *  10: 备注
     *
     * @param csvFilePath csv文件的路径
     */
    public static void parseCsv2Json(String csvFilePath) throws IOException, InterruptedException {
        List<Transaction> transactions = parseCsvToTransactions(csvFilePath);
        System.out.println(transactions);

        // 如果已有data.json文件，则追加进去
        if (Files.exists(Paths.get(DATA_JSON_PATH))) {
            writeTransactionsToJson(transactions, TEMP_JSON_PATH);

            Path path = Paths.get("src/main/resources/data/temp.json");
            if (Files.exists(path)) {
                System.out.println("Found temp.json at: " + path);
            } else {
                System.out.println("temp.json not found at: " + path);
            }

            // 对temp.json进行分类
            DeepSeek.classifyBatchTransaction(TEMP_JSON_PATH);

            List<String> jsonPaths = Arrays.asList(DATA_JSON_PATH, TEMP_JSON_PATH);
            List<Transaction> mergedTransactions = mergeAndRemoveDuplicates(jsonPaths);
            writeTransactionsToJson(mergedTransactions, DATA_JSON_PATH);
            Files.delete(Paths.get(TEMP_JSON_PATH));
        }
    }

    /**
     * 手动增加账单
     *
     * @param transaction 账单对象*/
    public static void addManualTransaction(Transaction transaction) throws IOException, InterruptedException {
        List<Transaction> temp = new ArrayList<>();
        temp.add(transaction);
        writeTransactionsToJson(temp, TEMP_JSON_PATH);
        DeepSeek.classifyBatchTransaction(TEMP_JSON_PATH);
        List<String> jsonPaths = Arrays.asList(DATA_JSON_PATH, TEMP_JSON_PATH);
        List<Transaction> mergedTransactions = mergeAndRemoveDuplicates(jsonPaths);
        writeTransactionsToJson(mergedTransactions, DATA_JSON_PATH);
        Files.delete(Paths.get(TEMP_JSON_PATH));
    }

    /**
     * 读取 CSV 文件，清洗/解析数据，返回 Transaction 列表
     *
     * @param csvFilePath CSV 文件路径
     * @return 交易记录列表
     */
    public static List<Transaction> parseCsvToTransactions(String csvFilePath) {
        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFilePath), StandardCharsets.UTF_8))) {
            String line;
            boolean startParse = false; // 用于标记是否开始真正解析交易数据

            while ((line = br.readLine()) != null) {
                line = line.trim();
                // 跳过空行
                if (line.isEmpty()) {
                    continue;
                }

                System.out.println(line);

                // 根据逗号切分
                String[] cols = line.split(",", -1);
                // 如果实际 CSV 用 ";" 分隔，则改成 line.split(";", -1);

                // 判断是否是标题行（可根据关键列名，如 “交易时间”、“交易类型”等）
                if (!startParse) {
                    // 如果这一行包含特定关键字段，就说明接下来就是数据部分
                    // 例如："交易时间","交易类型","交易对方"
                    if (line.contains("交易时间") && line.contains("交易类型") && line.contains("金额")) {
                        startParse = true;
                    }
                    // 未开启解析前直接 continue
                    continue;
                }

                // 如果已经开始解析，但列数 < 11，则可能是无效行，跳过
                if (cols.length < 11) {
                    continue;
                }

                // 按顺序取值 (0~10)
                String transactionTime = cols[0].trim();    // 交易时间
                String transactionType = cols[1].trim();    // 交易类型
                String counterparty    = cols[2].trim();    // 交易对方
                String item           = cols[3].trim();     // 商品
                String incExp         = cols[4].trim();     // 收/支
                String amountStr      = cols[5].trim();     // 金额(元)
                String paymentMethod  = cols[6].trim();     // 支付方式
                String status         = cols[7].trim();     // 当前状态
                String transactionId  = cols[8].trim();     // 交易单号
                String merchantId     = cols[9].trim();     // 商户单号
                String note           = cols[10].trim();    // 备注

                // 如果 CSV 中的金额带 "¥" 符号，可去除
                amountStr = amountStr.replace("¥", "");

                // 解析金额
                double amount = 0.0;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    // 如果解析金额失败，可记录日志或跳过
                }

                // 进一步清洗，去掉多余空格
                transactionTime = transactionTime.replaceAll("\\s+", " ");
                transactionType = transactionType.replaceAll("\\s+", " ");
                counterparty    = counterparty.replaceAll("\\s+", " ");
                item           = item.replaceAll("\\s+", " ");
                incExp         = incExp.replaceAll("\\s+", " ");
                paymentMethod  = paymentMethod.replaceAll("\\s+", " ");
                status         = status.replaceAll("\\s+", " ");
                note           = note.replaceAll("\\s+", " ");

                // 构建 Transaction 对象
                Transaction tx = new Transaction();
                tx.setTransactionTime(transactionTime);
                tx.setTransactionType(transactionType);
                tx.setCounterparty(counterparty);
                tx.setItem(item);
                tx.setIncExp(incExp);
                tx.setAmount(amount);
                tx.setPaymentMethod(paymentMethod);
                tx.setStatus(status);
                tx.setTransactionId(transactionId);
                tx.setMerchantId(merchantId);
                tx.setNote(note);

                transactions.add(tx);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(transactions);

        return transactions;
    }

    /**
     * 将 Transaction 列表以 JSON 数组的形式写入文件
     *
     * @param transactions 交易列表
     * @param outputJson   输出 JSON 文件路径
     */
    public static void writeTransactionsToJson(List<Transaction> transactions, String outputJson) {
        ObjectMapper mapper = new ObjectMapper();
        // 让输出格式更加美观
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputJson), StandardCharsets.UTF_8)) {
            mapper.writeValue(writer, transactions);
            System.out.println("成功处理 " + transactions.size() + " 条交易记录，已输出到 " + outputJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据transactionId来更新transactionType为分类结果
     *
     * @param transactionId 账单的id
     * @param newType 分类的结果
     */
    public synchronized static void updateTransactionTypeById(String transactionId, String newType) throws IOException {
        if (!Files.exists(Paths.get(DATA_JSON_PATH))) {
            System.err.println("no data exist");
            return;
        }

        if (!transactionId.startsWith("\"")) {
            transactionId = "\"" + transactionId + "\"";
        }

        // 读取json文件
        File jsonFile = new File(DATA_JSON_PATH);
        JsonNode rootNode = objectMapper.readTree(jsonFile);

        // 遍历 JSON 数组，找到指定 transactionId 的记录并更新 transactionType
        if (rootNode.isArray()) {
            Iterator<JsonNode> iterator = rootNode.iterator();
            while (iterator.hasNext()) {
                JsonNode transactionNode = iterator.next();

                // 检查该记录的 transactionId 是否匹配
                if (transactionNode.has("transactionId") && transactionNode.get("transactionId").asText().equals(transactionId)) {
                    // 找到匹配的记录，更新 transactionType
                    ((ObjectNode) transactionNode).put("transactionType", "\"" + newType + "\"");
                    System.out.println("成功更新 transactionId 为 " + transactionId + " 的 transactionType");
                    break;
                }
            }

            // 将更新后的 JSON 数据写回文件
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);
        } else {
            System.err.println("JSON 文件格式不正确，无法处理");
        }
    }

    /**
     * 加入新数据时对新数据进行分类
     *
     * @param transactionId 账单的id
     * @param newType 分类的结果
     */
    public synchronized static void updateTempTransactionTypeById(String transactionId, String newType) throws IOException {
        if (!Files.exists(Paths.get(TEMP_JSON_PATH))) {
            System.err.println("no data exist");
            return;
        }

        // 读取json文件
        File jsonFile = new File(TEMP_JSON_PATH);
        JsonNode rootNode = objectMapper.readTree(jsonFile);

        // 遍历 JSON 数组，找到指定 transactionId 的记录并更新 transactionType
        if (rootNode.isArray()) {
            Iterator<JsonNode> iterator = rootNode.iterator();
            while (iterator.hasNext()) {
                JsonNode transactionNode = iterator.next();

                // 检查该记录的 transactionId 是否匹配
                if (transactionNode.has("transactionId") && transactionNode.get("transactionId").asText().equals(transactionId)) {
                    // 找到匹配的记录，更新 transactionType
                    ((ObjectNode) transactionNode).put("transactionType", "\"" + newType + "\"");
                    System.out.println("成功更新 transactionId 为 " + transactionId + " 的 transactionType");
                    break;
                } else if (!transactionNode.has("transactionId")){
                    System.out.println("no id");
                }
            }

            // 将更新后的 JSON 数据写回文件
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);
        } else {
            System.err.println("JSON 文件格式不正确，无法处理");
        }
    }

    /**
     * 获取一个transaction的json文件的所有id
     *
     * @param jsonPath json文件的路径
     * @return id的列表
     */
    public static List<String> getAllTransactionIds(String jsonPath) throws IOException {
        // 读取 JSON 文件并将其转换为 Transaction 对象列表
        List<Transaction> transactions = objectMapper.readValue(new File(jsonPath), objectMapper.getTypeFactory().constructCollectionType(List.class, Transaction.class));

        // 创建一个 List 来存储所有的 transactionId
        List<String> transactionIds = new ArrayList<>();

        // 遍历所有交易记录并获取 transactionId
        for (Transaction transaction : transactions) {
            transactionIds.add(transaction.getTransactionId());  // 假设 Transaction 类中有 getTransactionId() 方法
        }

        return transactionIds;
    }

    /**
     * 按年和月筛选账单
     *
     * @param year 账单的年份
     * @param month 账单的月份
     * @return 筛选后的账单列表
     */
    public static List<Transaction> getTransactionsByMonth(int year, int month) {
        List<Transaction> transactions = readTransactionsFromClasspath("transactionData.json");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return transactions.stream()
                .filter(t -> {
                    try {
                        LocalDateTime dateTime = LocalDateTime.parse(t.getTransactionTime(), formatter);
                        return dateTime.getYear() == year && dateTime.getMonthValue() == month;
                    } catch (Exception e) {
                        System.err.println("日期格式错误: " + t.getTransactionTime());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }
    /*public static double getExpensesOfRecentMonth(int year, int month, int date) {
        List<Transaction> transactions = readTransactionsFromClasspath("transactionData.json");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        double expense = 0;
        List<Transaction> a = transactions.stream()
                .filter(t -> {
                    try {
                        LocalDateTime dateTime = LocalDateTime.parse(t.getTransactionTime(), formatter);
                        // 同时验证日期和交易类型
                        return dateTime.getYear() == year && dateTime.getMonthValue() == month
                                && dateTime.getDayOfMonth() == date; // 新增类型判断
                    } catch (Exception e) {
                        System.err.println("日期格式错误: " + t.getTransactionTime());
                        return false;
                    }
                })
                .collect(Collectors.toList());
        for (Transaction transaction : a) {
            expense +=  transaction.getAmount();
        }

        return expense;
    }*/
    public static Map<Integer, Double> getDailyExpensesForMonth(int year, int month) {
        List<Transaction> transactions = readTransactionsFromClasspath("transactionData.json");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return transactions.stream()
                .filter(t -> {
                    try {
                        // 1. 解析交易时间
                        LocalDateTime dateTime = LocalDateTime.parse(t.getTransactionTime(), formatter);

                        // 2. 验证是否匹配年月且是支出
                        boolean isMatch = dateTime.getYear() == year
                                && dateTime.getMonthValue() == month
                                && "\"支出\"".equals(t.getIncExp());

                        // 调试输出（可选）
                        if (isMatch) {
                            int combined = dateTime.getYear() * 10000
                                    + dateTime.getMonthValue() * 100
                                    + dateTime.getDayOfMonth();
                            System.out.println("匹配到交易日期: " + combined);
                        }

                        return isMatch;
                    } catch (Exception e) {
                        System.err.println("日期格式错误: " + t.getTransactionTime());
                        return false;
                    }
                })
                // 3. 按日期（yyyyMMdd 整数）分组并求和金额
                .collect(Collectors.groupingBy(
                        t -> {
                            LocalDateTime dateTime = LocalDateTime.parse(t.getTransactionTime(), formatter);
                            return dateTime.getYear() * 10000
                                    + dateTime.getMonthValue() * 100
                                    + dateTime.getDayOfMonth();
                        },
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }


}