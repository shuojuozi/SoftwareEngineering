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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 统一的 JSON / CSV 工具 —— 含 CSV 解析、JSON 合并去重、分类结果写回、
 * 手动添加账单 与 按年月筛选账单等业务辅助方法。
 */
public class JsonUtils {

    /* ---------------- 全局对象 & 常量 ---------------- */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String DATA_JSON_PATH = "src/main/resources/data/transactionData.json";
    private static final String TEMP_JSON_PATH = "src/main/resources/data/temp.json";

    static { objectMapper.enable(SerializationFeature.INDENT_OUTPUT); }

    /* ---------------- JSON 读写 ---------------- */
    public static List<Transaction> readTransactionsFromClasspath(String fileName) {
        Path p = Paths.get("src", "main", "resources", "data", fileName);
    
        // 1️⃣ 如果磁盘文件存在，直接读取
        if (Files.exists(p)) {
            try (InputStream in = Files.newInputStream(p)) {
                return objectMapper.readValue(in, new TypeReference<List<Transaction>>() {});
            } catch (IOException e) {
                System.err.println("读取 " + p + " 失败: " + e.getMessage());
                return Collections.emptyList();
            }
        }
    
        // 2️⃣ 尝试从 classpath 资源读取（打包后的 jar 里）
        try (InputStream in = JsonUtils.class.getClassLoader()
                                            .getResourceAsStream("data/" + fileName)) {
            if (in != null) {
                return objectMapper.readValue(in, new TypeReference<List<Transaction>>() {});
            }
        } catch (IOException ignored) { }
    
        // 3️⃣ 两处都没有 ⇒ 首次运行，返回空列表
        return Collections.emptyList();
    }
    

    private static List<Transaction> readJsonFile(String jsonPath) throws IOException {
        File f = new File(jsonPath);
        if (!f.exists()) return Collections.emptyList();
        return objectMapper.readValue(f, new TypeReference<List<Transaction>>() {});
    }

    public static void writeTransactionsToJson(List<Transaction> list, String path) {
        try (FileWriter fw = new FileWriter(path)) {
            objectMapper.writeValue(fw, list);
            System.out.println("成功处理 " + list.size() + " 条交易记录 -> " + path);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /* ---------------- JSON 合并去重 ---------------- */
    public static List<Transaction> mergeAndRemoveDuplicates(List<String> paths) throws IOException {
        Map<String, Transaction> map = new HashMap<>();
        for (String p : paths) {
            for (Transaction tx : readJsonFile(p)) {
                String id = Optional.ofNullable(tx.getTransactionId()).orElse(UUID.randomUUID().toString());
                Transaction existing = map.get(id);
                if (existing == null) { map.put(id, tx); continue; }
                Date newDate = parseDate(tx.getTransactionTime());
                Date oldDate = parseDate(existing.getTransactionTime());
                if (oldDate == null || (newDate != null && newDate.after(oldDate))) map.put(id, tx);
            }
        }
        return new ArrayList<>(map.values());
    }

    private static Date parseDate(String s) {
        if (s == null) return null;
        try { return sdf.parse(s.replace("\"","")); } catch (ParseException e) { return null; }
    }

    /* ---------------- CSV → Transaction ---------------- */
    public static List<Transaction> parseCsvToTransactions(String csvFilePath) {
        List<Transaction> list = new ArrayList<>();
        boolean start = false;
        boolean isAlipay = csvFilePath.contains("alipay_record");
        Charset charset = isAlipay ? Charset.forName("GBK") : StandardCharsets.UTF_8;

        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(csvFilePath), charset))
                .withCSVParser(new CSVParserBuilder().withSeparator(',').build()).build()) {
            String[] cols;
            while ((cols = reader.readNext()) != null) {
                if (!start) {
                    String h = String.join(",", cols);
                    if (h.contains("交易时间") && h.contains("金额")) start = true;
                    continue;
                }
                if (isAlipay) {
                    if (cols.length < 12) continue;
                    addTx(list, cols[0], cols[1], cols[2], cols[4], cols[5], cols[6], cols[7], cols[8], cols[9], cols[10], cols[11]);
                } else {
                    if (cols.length < 11) continue;
                    addTx(list, cols[0], cols[1], cols[2], cols[3], cols[4], cols[5], cols[6], cols[7], cols[8], cols[9], cols[10]);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private static void addTx(List<Transaction> list, String time, String type, String cp, String item,
                              String incExp, String amtStr, String pay, String status, String txId, String mchId, String note) {
        double amount = 0.0;
        try { amount = Double.parseDouble(amtStr.replace("¥", "").trim()); } catch (NumberFormatException ignored) {}
        Transaction tx = new Transaction();
        tx.setTransactionTime(time.trim());
        tx.setTransactionType(type.trim());
        tx.setCounterparty(cp.trim());
        tx.setItem(item.trim());
        tx.setIncExp(incExp.trim());
        tx.setAmount(amount);
        tx.setPaymentMethod(pay.trim());
        tx.setStatus(status.trim());
        tx.setTransactionId(txId.trim());
        tx.setMerchantId(mchId.trim());
        tx.setNote(note.trim());
        list.add(tx);
    }

    /* ---------------- CSV → JSON 全流程 ---------------- */
    public static void parseCsv2Json(String csvFilePath) throws IOException, InterruptedException {
        List<Transaction> list = parseCsvToTransactions(csvFilePath);
        if (Files.exists(Paths.get(DATA_JSON_PATH))) {
            writeTransactionsToJson(list, TEMP_JSON_PATH);
            DeepSeek.classifyBatchTransaction(TEMP_JSON_PATH);
            List<Transaction> merged = mergeAndRemoveDuplicates(Arrays.asList(DATA_JSON_PATH, TEMP_JSON_PATH));
            writeTransactionsToJson(merged, DATA_JSON_PATH);
            Files.deleteIfExists(Paths.get(TEMP_JSON_PATH));
        } else {
            writeTransactionsToJson(list, DATA_JSON_PATH);
        }
    }

    /* ---------------- 分类结果写回 ---------------- */
    public static synchronized void updateTransactionTypeById(String id, String type) throws IOException {
        updateTypeInJson(DATA_JSON_PATH, id, type);
    }
    public static synchronized void updateTempTransactionTypeById(String id, String type) throws IOException {
        updateTypeInJson(TEMP_JSON_PATH, id, type);
    }
    private static void updateTypeInJson(String path, String id, String type) throws IOException {
        if (!Files.exists(Paths.get(path))) return;
        JsonNode root = objectMapper.readTree(new File(path));
        if (!root.isArray()) return;
        for (JsonNode node : root) {
            if (id.equals(node.path("transactionId").asText())) {
                ((ObjectNode) node).put("transactionType", type);
                break;
            }
        }
        objectMapper.writeValue(new File(path), root);
    }

    /* ---------------- 辅助查询 ---------------- */
    public static Transaction findTransactionById(List<Transaction> list, String id) {
        for (Transaction t : list) if (id.equals(t.getTransactionId())) return t;
        return null;
    }

    public static List<String> getAllTransactionIds(String jsonPath) throws IOException {
        List<Transaction> list = objectMapper.readValue(new File(jsonPath),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Transaction.class));
        List<String> ids = new ArrayList<>();
        for (Transaction t : list) ids.add(t.getTransactionId());
        return ids;
    }

    /* ============================================================ */
    /*                业务层缺失的两个方法补回                         */
    /* ============================================================ */

    /** 手动添加单条交易并分类合并 */
    public static void addManualTransaction(Transaction tx) throws IOException, InterruptedException {
        writeTransactionsToJson(Collections.singletonList(tx), TEMP_JSON_PATH);
        DeepSeek.classifyBatchTransaction(TEMP_JSON_PATH);
        List<Transaction> merged = mergeAndRemoveDuplicates(Arrays.asList(DATA_JSON_PATH, TEMP_JSON_PATH));
        writeTransactionsToJson(merged, DATA_JSON_PATH);
        Files.deleteIfExists(Paths.get(TEMP_JSON_PATH));
    }

    /** 按年月筛选账单 */
    public static List<Transaction> getTransactionsByMonth(int year, int month) {
        List<Transaction> list = readTransactionsFromClasspath("transactionData.json");
        List<Transaction> out = new ArrayList<>();
        for (Transaction t : list) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(t.getTransactionTime().replace("\"",""), dtf);
                if (ldt.getYear() == year && ldt.getMonthValue() == month) out.add(t);
            } catch (Exception ignored) {}
        }
        return out;
    }
}
