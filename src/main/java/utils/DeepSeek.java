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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/** DeepSeek API wrapper: multi-turn conversation + parallel classification + progress callback */
public class DeepSeek {
    private static final Logger logger = Logger.getLogger(DeepSeek.class.getName());
    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final Preferences prefs = Preferences.userNodeForPackage(DeepSeek.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /* ---------------- Session Persistence ---------------- */
    private static final File SESSION_FILE = new File(System.getProperty("user.home"), ".deepseek_sessions.json");
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
            logger.warning("Failed to load session file: " + e.getMessage());
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
            logger.warning("Failed to write session file: " + e.getMessage());
        }
    }

    /* ---------------- Core Chat Logic ---------------- */
    private static final String DEFAULT_SYS = "You are a helpful assistant";

    public static String chat(String sessionId, String userInput) {
        String key = prefs.get("deepseek_api_key", "");
        if (key.isBlank())
            return "⚠️ DeepSeek API Key is not configured. Please enter it in the ⚙️ Settings.";

        try {
            ArrayNode msgs = SESSION_MAP.computeIfAbsent(sessionId, k -> {
                ArrayNode arr = mapper.createArrayNode();

                // System prompt
                ObjectNode sys = mapper.createObjectNode();
                sys.put("role", "system");
                sys.put("content", DEFAULT_SYS);
                arr.add(sys);

                // Financial context (only on first session creation)
                ObjectNode ctx = mapper.createObjectNode();
                ctx.put("role", "system");
                ctx.put("content", buildFinancialContext());
                arr.add(ctx);

                return arr;
            });

            ObjectNode user = mapper.createObjectNode();
            user.put("role", "user").put("content", userInput);
            msgs.add(user);

            String rsp = doCompletion(msgs, key);

            ObjectNode asst = mapper.createObjectNode();
            asst.put("role", "assistant").put("content", rsp);
            msgs.add(asst);

            persistSessions();
            return rsp;
        } catch (Exception e) {
            logger.severe("DeepSeek API call failed: " + e.getMessage());
            return "Call failed: " + e.getMessage();
        }
    }

    /** Build context string using current financial data */
    private static String buildFinancialContext() {
        double totalAssets = FinanceContext.getTotalAssets();
        double savingsGoal = FinanceContext.getSavingsGoal();
        double monthlyIncome = FinanceContext.getMonthlyIncome();

        int year = DateContext.getYear();
        int month = DateContext.getMonth();
        double spent = JsonUtils.getTransactionsByMonth(year, month)
                .stream()
                .mapToDouble(Transaction::getAmount)
                .sum();

        return String.format(
                "User Profile:\n" +
                        "- Total assets: %.2f yuan\n" +
                        "- Savings goal: %.2f yuan\n" +
                        "- Monthly income: %.2f yuan\n" +
                        "- Expenditure for %d-%d so far: %.2f yuan\n" +
                        "Use this information when giving financial advice.",
                totalAssets, savingsGoal, monthlyIncome, year, month, spent
        );
    }

    private static String doCompletion(ArrayNode msgs, String key) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        ObjectNode body = mapper.createObjectNode();
        body.put("model", "deepseek-chat").set("messages", msgs);

        Request req = new Request.Builder().url(API_URL)
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + key)
                .build();

        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful())
                throw new IOException("HTTP " + resp.code() + ": " + resp.body().string());

            JsonNode root = mapper.readTree(resp.body().string());
            return root.path("choices").get(0).path("message").path("content").asText();
        }
    }

    /* ---------------- Classification Utilities ---------------- */
    private static final List<String> ALLOWED = List.of(
            "food and dining", "transportation", "housing", "entertainment", "shopping",
            "healthcare", "education and training", "communication", "finance and investment", "transfer accounts"
    );

    private static String normalize(String raw) {
        String s = raw.toLowerCase().replaceAll("[^a-z ]", " ").replaceAll("\\s{2,}", " ").trim();
        for (String c : ALLOWED) if (s.contains(c)) return c;
        return "unknown";
    }

    /**
     * Parallel batch classification: updates memory and writes to file once
     */
    public static void classifyBatchTransaction(String jsonPath, ProgressCallback cb) throws IOException, InterruptedException {
        ObjectNode[] nodes = mapper.readValue(new File(jsonPath), ObjectNode[].class);
        Map<String, ObjectNode> index = new ConcurrentHashMap<>();
        for (ObjectNode n : nodes) {
            String id = StringUtil.cleanId(n.path("transactionId").asText());
            index.put(id, n);
        }
        int total = index.size();
        AtomicInteger done = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(total);

        index.forEach((id, node) -> pool.submit(() -> {
            try {
                String cat = classifyInMemory(id, node);
                node.put("transactionType", cat);
            } finally {
                cb.update(done.incrementAndGet() * 1.0 / total);
                latch.countDown();
            }
        }));
        latch.await();
        pool.shutdown();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(jsonPath), nodes);
    }

    private static String classifyInMemory(String id, JsonNode node) {
        String prompt = "You are a helpful assistant that classifies bill items into categories "
                + "(food and dining, transportation, housing, entertainment, shopping, healthcare, "
                + "education and training, communication, finance and investment, transfer accounts). "
                + "Respond ONLY with the category.";
        return normalize(chat("classify_" + id, prompt + "\n" + node.toString()));
    }

    /** Deprecated method kept for backward compatibility — calls internal version with empty callback */
    public static void classifyBatchTransaction(String jsonPath) throws IOException, InterruptedException {
        classifyBatchTransaction(jsonPath, p -> {});
    }

    /** Progress callback interface */
    @FunctionalInterface
    public interface ProgressCallback {
        void update(double p);
    }

    // -----------------------------------------
    // Other helper methods like budgetSuggestion can be kept or added here.
    // -----------------------------------------
}
