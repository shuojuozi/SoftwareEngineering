package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import pojo.Transaction;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Unified JSON/CSV utility — includes CSV parsing, JSON merge/deduplication,
 * writing back classified results, adding transactions manually, filtering by year/month,
 * and other business-level helper methods.
 */
public class JsonUtils {

    /* ---------------- Global Objects & Constants ---------------- */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String BASE_PATH = "src/main/resources/data";
    private static final String DATA_JSON_PATH = BASE_PATH + "\\transactionData.json";
    private static final String TEMP_JSON_PATH = BASE_PATH + "\\temp.json";

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /* ---------------- JSON Read ---------------- */
    public static List<Transaction> readTransactionsFromClasspath(String fileName) {
        Path p = Paths.get(BASE_PATH, fileName);
        System.out.println("Reading " + p + " ...");

        // 1️⃣ Try reading from disk if file exists
        if (Files.exists(p)) {
            try (InputStream in = Files.newInputStream(p)) {
                return objectMapper.readValue(in, new TypeReference<List<Transaction>>() {});
            } catch (IOException e) {
                System.err.println("Failed to read " + p + ": " + e.getMessage());
                return Collections.emptyList();
            }
        }

        // 2️⃣ Try reading from JAR classpath
        try (InputStream in = JsonUtils.class.getClassLoader()
                .getResourceAsStream("data/" + fileName)) {
            if (in != null) {
                return objectMapper.readValue(in, new TypeReference<List<Transaction>>() {});
            }
        } catch (IOException ignored) {}

        // 3️⃣ File not found anywhere — assume first-time run
        return Collections.emptyList();
    }

    private static List<Transaction> readJsonFile(String jsonPath) throws IOException {
        File f = new File(jsonPath);
        if (!f.exists()) return Collections.emptyList();
        return objectMapper.readValue(f, new TypeReference<List<Transaction>>() {});
    }

    public static void writeTransactionsToJson(List<Transaction> list, String path) {
        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileWriter fw = new FileWriter(file)) {
                objectMapper.writeValue(fw, list);
                System.out.println("Successfully wrote " + list.size() + " transactions to -> " + path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ---------------- JSON Merge & Deduplication ---------------- */
    public static List<Transaction> mergeAndRemoveDuplicates(List<String> paths) throws IOException {
        Map<String, Transaction> map = new HashMap<>();
        for (String p : paths) {
            for (Transaction tx : readJsonFile(p)) {
                String id = Optional.ofNullable(tx.getTransactionId()).orElse(UUID.randomUUID().toString());
                Transaction existing = map.get(id);
                if (existing == null) {
                    map.put(id, tx);
                    continue;
                }
                Date newDate = parseDate(tx.getTransactionTime());
                Date oldDate = parseDate(existing.getTransactionTime());
                if (oldDate == null || (newDate != null && newDate.after(oldDate))) {
                    map.put(id, tx);
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    private static Date parseDate(String s) {
        if (s == null) return null;
        try {
            return sdf.parse(s.replace("\"", ""));
        } catch (ParseException e) {
            return null;
        }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void addTx(List<Transaction> list,
                              String time, String type, String cp, String item,
                              String incExp, String amtStr, String pay,
                              String status, String txId, String mchId, String note) {

        txId = StringUtil.cleanId(txId);
        mchId = StringUtil.cleanId(mchId);

        double amount = 0.0;
        try {
            amount = Double.parseDouble(amtStr.replace("¥", "").trim());
        } catch (NumberFormatException ignored) {}

        Transaction tx = new Transaction();
        tx.setTransactionTime(time.trim());
        tx.setTransactionType(type.trim());
        tx.setCounterparty(cp.trim());
        tx.setItem(item.trim());
        tx.setIncExp(incExp.trim());
        tx.setAmount(amount);
        tx.setPaymentMethod(pay.trim());
        tx.setStatus(status.trim());
        tx.setTransactionId(txId);
        tx.setMerchantId(mchId);
        tx.setNote(note.trim());

        list.add(tx);
    }

    /* ---------------- CSV → JSON Full Process with Callback ---------------- */
    public static void parseCsv2Json(String csvFilePath, DeepSeek.ProgressCallback cb) throws IOException, InterruptedException {
        List<Transaction> list = parseCsvToTransactions(csvFilePath);
        writeTransactionsToJson(list, TEMP_JSON_PATH);
        DeepSeek.classifyBatchTransaction(TEMP_JSON_PATH, cb);

        List<Transaction> merged = mergeAndRemoveDuplicates(Arrays.asList(DATA_JSON_PATH, TEMP_JSON_PATH));
        writeTransactionsToJson(merged, DATA_JSON_PATH);
        Files.deleteIfExists(Paths.get(TEMP_JSON_PATH));
    }

    public static void parseCsv2Json(String csvFilePath) throws IOException, InterruptedException {
        parseCsv2Json(csvFilePath, p -> {});
    }

    /* ---------------- Update Classification Results ---------------- */
    public static synchronized void updateTransactionTypeById(String id, String type) throws IOException {
        updateTypeInJson(DATA_JSON_PATH, id, type);
    }

    public static synchronized void updateTempTransactionTypeById(String transactionId, String type) throws IOException {
        if (!Files.exists(Paths.get(TEMP_JSON_PATH))) return;
        JsonNode rootNode = objectMapper.readTree(new File(TEMP_JSON_PATH));
        if (!rootNode.isArray()) return;

        String cleanInputId = StringUtil.cleanId(transactionId);

        for (JsonNode node : rootNode) {
            String storedId = StringUtil.cleanId(node.path("transactionId").asText());
            if (cleanInputId.equals(storedId)) {
                ((ObjectNode) node).put("transactionId", storedId);
                ((ObjectNode) node).put("type", type);
                objectMapper.writeValue(new File(TEMP_JSON_PATH), rootNode);
                break;
            }
        }
    }

    private static void updateTypeInJson(String path, String id, String newType) throws IOException {
        if (!Files.exists(Paths.get(path))) return;
        JsonNode root = objectMapper.readTree(new File(path));
        if (!root.isArray()) return;

        String cleanInputId = StringUtil.cleanId(id);

        for (JsonNode node : root) {
            String storedId = StringUtil.cleanId(node.path("transactionId").asText());
            if (cleanInputId.equals(storedId)) {
                ObjectNode obj = (ObjectNode) node;
                obj.put("transactionId", storedId);
                obj.put("transactionType", newType.toLowerCase());
                obj.remove("type");
                objectMapper.writeValue(new File(path), root);
                break;
            }
        }
    }

    /* ---------------- Utility Query ---------------- */
    public static Transaction findTransactionById(List<Transaction> list, String id) {
        id = StringUtil.cleanId(id);
        for (Transaction t : list) {
            if (id.equals(StringUtil.cleanId(t.getTransactionId())))
                return t;
        }
        return null;
    }

    /* ---------------- Business Utilities ---------------- */

    /** Add a transaction manually and merge it into data file after classification */
    public static void addManualTransaction(Transaction tx) throws IOException, InterruptedException {
        writeTransactionsToJson(Collections.singletonList(tx), TEMP_JSON_PATH);
        DeepSeek.classifyBatchTransaction(TEMP_JSON_PATH);
        List<Transaction> merged = mergeAndRemoveDuplicates(Arrays.asList(DATA_JSON_PATH, TEMP_JSON_PATH));
        writeTransactionsToJson(merged, DATA_JSON_PATH);
        Files.deleteIfExists(Paths.get(TEMP_JSON_PATH));
    }

    /** Filter transactions by year and month */
    public static List<Transaction> getTransactionsByMonth(int year, int month) {
        List<Transaction> list = readTransactionsFromClasspath("transactionData.json");
        List<Transaction> out = new ArrayList<>();
        for (Transaction t : list) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(t.getTransactionTime().replace("\"", ""), dtf);
                if (ldt.getYear() == year && ldt.getMonthValue() == month)
                    out.add(t);
            } catch (Exception ignored) {}
        }
        return out;
    }

    /** Escape CSV fields: sanitize special characters and escape quotes */
    private static String escapeCsv(String s) {
        if (s == null) return "";
        String clean = s.replaceAll("\\p{C}", "").trim();
        return clean.replace("\"", "\"\"");
    }

    /** Export list of transactions to a CSV file */
    public static void exportTransactions(List<Transaction> transactions, String outputCsvPath) throws IOException {
        try (
                OutputStream os = new FileOutputStream(outputCsvPath);
                OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                BufferedWriter bw = new BufferedWriter(osw);
                PrintWriter pw = new PrintWriter(bw)
        ) {
            pw.write("\uFEFF"); // BOM for Excel compatibility
            pw.println("交易时间,交易类型,交易对象,商品,收/支,金额,支付方式,状态,交易号,商户号,备注");

            for (Transaction t : transactions) {
                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        escapeCsv(t.getTransactionTime()),
                        escapeCsv(t.getTransactionType()),
                        escapeCsv(t.getCounterparty()),
                        escapeCsv(t.getItem()),
                        escapeCsv(t.getIncExp()),
                        t.getAmount(),
                        escapeCsv(t.getPaymentMethod()),
                        escapeCsv(t.getStatus()),
                        escapeCsv(t.getTransactionId()),
                        escapeCsv(t.getMerchantId()),
                        escapeCsv(t.getNote()));
            }
        }
    }

    /** Export transactionData.json into CSV file (UTF-8 encoding) */
    public static void exportTransactionsToCsv(String jsonFileName, String outputCsvPath) throws IOException {
        String jsonPath = Paths.get(BASE_PATH, jsonFileName).toString();
        List<Transaction> list = objectMapper.readValue(
                new File(jsonPath),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Transaction.class)
        );
        exportTransactions(list, outputCsvPath);
    }
}
