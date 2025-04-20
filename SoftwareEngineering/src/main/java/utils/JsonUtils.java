package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import pojo.Transaction;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 通用 JSON / CSV 工具类 —— 统一了 CSV → Transaction 解析、JSON 合并去重与字段更新等能力。
 * <p>
 * ⚠ 解析 CSV 时自动识别「支付宝新 12 列 (GBK)」与「微信旧 11 列 (UTF‑8)」两种常见账单格式；
 * 若后续有新格式，可在 {@link #parseCsvToTransactions(String)} 增加分支即可。
 */
public class JsonUtils {

    /* ------------------------------------------------------------ */
    /*               全局静态对象 & 路径配置                         */
    /* ------------------------------------------------------------ */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 业务中固定的存储路径，如有需要可抽出来做配置
    private static final String DATA_JSON_PATH = "src/main/resources/data/transactionData.json";
    private static final String TEMP_JSON_PATH = "src/main/resources/data/temp.json";

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /* ------------------------------------------------------------ */
    /*                     读取 / 写入 JSON                          */
    /* ------------------------------------------------------------ */

    /**
     * 从 classpath 读取交易 JSON。
     */
    public static List<Transaction> readTransactionsFromClasspath(String resourcePath) {
        String filePath = "src/main/resources/data/" + resourcePath;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return objectMapper.readValue(fis, new TypeReference<List<Transaction>>() {
            });
        } catch (Exception e) {
            System.err.println("解析 JSON 失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /** 本地文件读取（内部使用） */
    private static List<Transaction> readJsonFile(String jsonPath) throws IOException {
        File f = new File(jsonPath);
        if (!f.exists()) {
            System.err.println("文件不存在: " + jsonPath);
            return Collections.emptyList();
        }
        return objectMapper.readValue(f, new TypeReference<List<Transaction>>() {
        });
    }

    /**
     * 合并多份 JSON 列表并按 transactionId 去重：若冲突保留“交易时间较晚”那条。
     */
    public static List<Transaction> mergeAndRemoveDuplicates(List<String> jsonPaths) throws IOException {
        Map<String, Transaction> map = new HashMap<>();
        for (String path : jsonPaths) {
            for (Transaction tx : readJsonFile(path)) {
                String id = Optional.ofNullable(tx.getTransactionId()).orElse("");
                if (id.isEmpty()) {
                    map.put(UUID.randomUUID().toString(), tx);
                    continue;
                }
                Transaction existing = map.get(id);
                if (existing == null) {
                    map.put(id, tx);
                } else {
                    Date newDate = parseDate(tx.getTransactionTime());
                    Date oldDate = parseDate(existing.getTransactionTime());
                    if (oldDate == null || (newDate != null && newDate.after(oldDate))) {
                        map.put(id, tx);
                    }
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    private static Date parseDate(String timeStr) {
        if (timeStr == null) return null;
        try {
            return sdf.parse(timeStr.replace("\"", "").trim());
        } catch (ParseException e) {
            return null;
        }
    }

    /* ------------------------------------------------------------ */
    /*                    CSV → Transaction                         */
    /* ------------------------------------------------------------ */

    /**
     * 读取 CSV 并解析为交易列表。支持：
     * <ul>
     *   <li>支付宝新版 12 列，文件名包含 "alipay_record"，编码 GBK</li>
     *   <li>微信旧版 11 列，文件名包含 "微信支付账单" or 其它，编码 UTF‑8</li>
     * </ul>
     */
    public static List<Transaction> parseCsvToTransactions(String csvFilePath) {
        List<Transaction> transactions = new ArrayList<>();
        boolean startParse = false;
        boolean isAlipay = csvFilePath.contains("alipay_record");
        Charset charset = isAlipay ? Charset.forName("GBK") : StandardCharsets.UTF_8;

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new FileInputStream(csvFilePath), charset))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(',')
                        .withIgnoreQuotations(false)
                        .build())
                .build()) {

            String[] cols;
            while ((cols = reader.readNext()) != null) {
                if (cols.length == 0) continue;

                // 标题行检测：出现关键列后才真正开始解析
                if (!startParse) {
                    String header = String.join(",", cols);
                    if (header.contains("交易时间") &&
                            (header.contains("交易类型") || header.contains("交易分类")) &&
                            header.contains("金额")) {
                        startParse = true;
                    }
                    continue;
                }

                // 支付宝新格式：12 列（含对方账号列，需跳过）
                if (isAlipay) {
                    if (cols.length < 12) continue;
                    addTransaction(transactions,
                            cols[0], cols[1], cols[2], // 时间、分类、对方
                            cols[4],                    // 商品说明 (cols[3] 为对方账号)
                            cols[5], cols[6],           // 收支、金额
                            cols[7], cols[8],           // 支付方式、状态
                            cols[9], cols[10], cols[11] // 交易id、商户id、备注
                    );
                } else { // 微信旧格式：11 列
                    if (cols.length < 11) continue;
                    addTransaction(transactions,
                            cols[0], cols[1], cols[2],
                            cols[3], cols[4], cols[5],
                            cols[6], cols[7], cols[8], cols[9], cols[10]
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * 内部工具：构造并清洗 Transaction 实例。
     */
    private static void addTransaction(List<Transaction> list,
                                       String time, String type, String cp, String item,
                                       String incExp, String amtStr, String payMethod,
                                       String status, String txId, String mchId, String note) {
        double amount = 0.0;
        if (amtStr != null) {
            amtStr = amtStr.replace("¥", "").trim();
            try {
                amount = Double.parseDouble(amtStr);
            } catch (NumberFormatException ignored) {
            }
        }

        Transaction tx = new Transaction();
        tx.setTransactionTime(time.replaceAll("\\s+", " "));
        tx.setTransactionType(type.replaceAll("\\s+", " "));
        tx.setCounterparty(cp.replaceAll("\\s+", " "));
        tx.setItem(item.replaceAll("\\s+", " "));
        tx.setIncExp(incExp.replaceAll("\\s+", " "));
        tx.setAmount(amount);
        tx.setPaymentMethod(payMethod.replaceAll("\\s+", " "));
        tx.setStatus(status.replaceAll("\\s+", " "));
        tx.setTransactionId(txId);
        tx.setMerchantId(mchId);
        tx.setNote(note.replaceAll("\\s+", " "));
        list.add(tx);
    }

    /* ------------------------------------------------------------ */
    /*                 CSV → JSON 全流程封装                        */
    /* ------------------------------------------------------------ */

    /**
     * 从 CSV 解析交易 → 分类 → 合并去重 → 写入正式 data.json。
     */
    public static void parseCsv2Json(String csvFilePath) throws IOException, InterruptedException {
        List<Transaction> transactions = parseCsvToTransactions(csvFilePath);

        // 若 data.json 已存在，需先写临时文件再做合并去重
        if (Files.exists(Paths.get(DATA_JSON_PATH))) {
            writeTransactionsToJson(transactions, TEMP_JSON_PATH);

            // 调用外部 AI 分类接口（如有需要可以抽象成策略模式）
            DeepSeek.classifyBatchTransaction(TEMP_JSON_PATH);

            List<String> paths = Arrays.asList(DATA_JSON_PATH, TEMP_JSON_PATH);
            List<Transaction> merged = mergeAndRemoveDuplicates(paths);
            writeTransactionsToJson(merged, DATA_JSON_PATH);
            Files.deleteIfExists(Paths.get(TEMP_JSON_PATH));
        } else {
            // 第一次直接输出
            writeTransactionsToJson(transactions, DATA_JSON_PATH);
        }
    }

    /**
     * 将 Transaction 列表以 JSON 数组的形式写入文件（统一使用全局 objectMapper）。
     */
    public static void writeTransactionsToJson(List<Transaction> transactions, String outputJson) {
        try (FileWriter fw = new FileWriter(outputJson)) {
            objectMapper.writeValue(fw, transactions);
            System.out.println("成功处理 " + transactions.size() + " 条交易记录，已输出到 " + outputJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ------------------------------------------------------------ */
    /*          交易分类结果写回 / 辅助查询方法                        */
    /* ------------------------------------------------------------ */

    public static synchronized void updateTransactionTypeById(String transactionId, String newType) throws IOException {
        updateTypeInJson(DATA_JSON_PATH, transactionId, newType);
    }

    public static synchronized void updateTempTransactionTypeById(String transactionId, String newType) throws IOException {
        updateTypeInJson(TEMP_JSON_PATH, transactionId, newType);
    }

    private static void updateTypeInJson(String jsonPath, String transactionId, String newType) throws IOException {
        if (!Files.exists(Paths.get(jsonPath))) {
            System.err.println("文件不存在: " + jsonPath);
            return;
        }
        File jsonFile = new File(jsonPath);
        JsonNode root = objectMapper.readTree(jsonFile);
        if (!root.isArray()) {
            System.err.println("JSON 格式不正确");
            return;
        }
        for (JsonNode node : root) {
            if (node.has("transactionId") && transactionId.equals(node.get("transactionId").asText())) {
                ((ObjectNode) node).put("transactionType", newType);
                break;
            }
        }
        objectMapper.writeValue(jsonFile, root);
    }

    /**
     * 查询指定 transactionId 的交易。
     */
    public static Transaction findTransactionById(List<Transaction> transactions, String transactionId) {
        for (Transaction tx : transactions) {
            if (transactionId.equals(tx.getTransactionId())) return tx;
        }
        return null;
    }

    /**
     * 读取某 JSON 文件中所有 transactionId。
     */
    public static List<String> getAllTransactionIds(String jsonPath) throws IOException {
        List<Transaction> list = objectMapper.readValue(new File(jsonPath),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Transaction.class));
        List<String> ids = new ArrayList<>();
        for (Transaction tx : list) {
            ids.add(tx.getTransactionId());
        }
        return ids;
    }
}
