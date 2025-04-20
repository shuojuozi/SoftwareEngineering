package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import pojo.Transaction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * DeepSeek 封装：多轮对话 + 本地持久化 + 业务助手方法
 */
public class DeepSeek {
    private static final Logger logger = Logger.getLogger(DeepSeek.class.getName());
    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final Preferences prefs = Preferences.userNodeForPackage(DeepSeek.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /* ---------------- 持久化文件 ---------------- */
    private static final File SESSION_FILE = new File(System.getProperty("user.home"), ".deepseek_sessions.json");

    /** sessionId -> messages */
    private static final Map<String, ArrayNode> SESSION_MAP = loadSessions();

    private static Map<String, ArrayNode> loadSessions() {
        if (!SESSION_FILE.exists()) return new ConcurrentHashMap<>();
        try {
            byte[] data = Files.readAllBytes(SESSION_FILE.toPath());
            if (data.length == 0) return new ConcurrentHashMap<>();
            Map<String, List<Map<String, String>>> raw = mapper.readValue(data, new TypeReference<>() {});
            Map<String, ArrayNode> map = new ConcurrentHashMap<>();
            raw.forEach((sid, list) -> map.put(sid, mapper.valueToTree(list)));
            return map;
        } catch (IOException e) {
            logger.warning("读取会话文件失败: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    private static void persistSessions() {
        try {
            Map<String, List<Map<String, String>>> out = new HashMap<>();
            SESSION_MAP.forEach((sid, arr) -> {
                List<Map<String, String>> lst = new ArrayList<>();
                arr.forEach(n -> lst.add(Map.of("role", n.get("role").asText(), "content", n.get("content").asText())));
                out.put(sid, lst);
            });
            mapper.writerWithDefaultPrettyPrinter().writeValue(SESSION_FILE, out);
        } catch (IOException e) {
            logger.warning("写入会话文件失败: " + e.getMessage());
        }
    }

    /* ---------------- 对话核心 ---------------- */
    private static final String DEFAULT_SYSTEM_PROMPT = "You are a helpful assistant";
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static String chat(String sessionId, String userInput) {
        String apiKey = prefs.get("deepseek_api_key", "");
        if (apiKey.isBlank())
            return "⚠️ 尚未配置 DeepSeek API Key，请在「⚙️ Settings」中填写";

        try {
            ArrayNode messages = SESSION_MAP.computeIfAbsent(sessionId, sid -> {
                ArrayNode arr = mapper.createArrayNode();
                ObjectNode sys = mapper.createObjectNode();
                sys.put("role", "system");
                sys.put("content", DEFAULT_SYSTEM_PROMPT);
                arr.add(sys);
                return arr;
            });

            ObjectNode userMsg = mapper.createObjectNode();
            userMsg.put("role", "user").put("content", userInput);
            messages.add(userMsg);

            String assistantContent = doChatCompletion(messages, apiKey);

            ObjectNode assistantMsg = mapper.createObjectNode();
            assistantMsg.put("role", "assistant").put("content", assistantContent);
            messages.add(assistantMsg);

            persistSessions();
            return assistantContent;
        } catch (IOException e) {
            logger.severe("DeepSeek 调用失败: " + e.getMessage());
            return "调用失败：" + e.getMessage();
        }
    }

    public static String communicateOnce(String userInput) {
        return chat("__SINGLE__" + System.nanoTime(), userInput);
    }

    private static String doChatCompletion(ArrayNode messages, String apiKey) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        ObjectNode body = mapper.createObjectNode();
        body.put("model", "deepseek-chat");
        body.set("messages", messages);
        body.put("max_tokens", 2048);
        body.put("temperature", 1);
        Request req = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful())
                throw new IOException("HTTP " + resp.code() + ": " + resp.body().string());
            JsonNode root = mapper.readTree(resp.body().string());
            return root.path("choices").get(0).path("message").path("content").asText();
        }
    }

    /* ================= 业务助手函数 ================= */

    /**
     * 分析指定年月账单，并给出预算 / 节流建议
     * @param year  年份，如 2025
     * @param month 月份 1-12
     * @param monthlyIncome 收入（元），可根据 UI 输入而定
     */
    public static String budgetSuggestion(int year, int month, double monthlyIncome) {
        List<Transaction> txs = JsonUtils.getTransactionsByMonth(year, month);
        if (txs.isEmpty()) return "无账单数据";

        // 将账单列表压缩到 prompt 中（如需进一步精简可仅抽取关键信息）
        StringBuilder sb = new StringBuilder();
        txs.forEach(t -> sb.append(t.toString().replace("\n", " ")).append("\n"));

        String prompt = "Here is my recent billing data (" + year + "-" + month + "). Please:\n" +
                "1. Summarize spending by category and highlight the highest.\n" +
                "2. Suggest a reasonable budget for next month based on those expenses.\n" +
                "3. Assuming my monthly income is " + monthlyIncome + " yuan, propose a savings target.\n" +
                "4. Point out categories that can be reduced and give cost‑saving tips.\n" +
                "Billing data:\n" + sb;

        // 使用独立 session，方便后续追问
        String sid = "budget_" + year + "_" + month;
        return chat(sid, prompt);
    }

    /* ================= 旧版分类函数保留 ================= */
    public static String classifyTransaction(String transactionId) {
        if (!transactionId.startsWith("\"")) transactionId = "\"" + transactionId + "\"";
        List<Transaction> list = JsonUtils.readTransactionsFromClasspath("temp.json");
        Transaction t = JsonUtils.findTransactionById(list, transactionId);
        if (t == null) return null;
        String input = t.toString().replace("\"", "\\\"").replace("\n", " ");
        String prompt = "You are a helpful assistant that classifies bill items into categories such as Food and Dining, Transportation, Housing, Entertainment, Shopping, Healthcare, Education and Training, Communication, Finance and Investment, transfer accounts. Respond with only the category.";
        String sid = "classify_" + transactionId;
        chat(sid, prompt);
        return chat(sid, input);
    }

    public static void classifyBatchTransaction(String jsonPath) throws InterruptedException, IOException {
        List<String> ids = JsonUtils.getAllTransactionIds(jsonPath);
        CountDownLatch latch = new CountDownLatch(ids.size());
        for (String id : ids) {
            executorService.submit(() -> { try { classifyTransaction(id); } finally { latch.countDown(); } });
        }
        latch.await();
    }
}
