package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import pojo.Transaction;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class DeepSeek {
    private static final Logger logger = Logger.getLogger(DeepSeek.class.getName());
    private static final String API_KEY = ConfigUtil.getApiKey();
    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 和DeepSeek进行对话
     *
     * @param userInput 用户的输入（字符串）
     * @return DeepSeek的返回内容
     */
    public static String communicate(String userInput) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        // 1. 构建 JSON 字符串（动态插入 userInput）
        String jsonBody = "{\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"content\": \"You are a helpful assistant\",\n" +
                "      \"role\": \"system\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"content\": \"" + userInput + "\",\n" +
                "      \"role\": \"user\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"model\": \"deepseek-chat\",\n" +
                "  \"max_tokens\": 2048,\n" +
                "  \"temperature\": 1,\n" +
                "  \"tool_choice\": \"none\",\n" +
                "  \"stream\": false\n" +
                "}";

        // 2. 发送请求
        RequestBody body = RequestBody.create(jsonBody, mediaType);
        Request request = new Request.Builder()
                .url(API_URL)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("请求失败，状态码：" + response.code());
                return "请求失败：" + response.body().string();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body().string());
                JsonNode contentNode = root.path("choices").get(0).path("message").path("content");

                return contentNode.asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "调用失败：" + e.getMessage();
        }
    }

    /**
     * 使用DeepSeek对账单进行分类
     *
     * @param transactionId 账单的id
     * @return DeepSeek返回的分类结果
     */
    public static String classifyTransaction(String transactionId) {
        if (!transactionId.startsWith("\"")) {
            transactionId = "\"" + transactionId + "\"";
        }
        List<Transaction> transactions;
        transactions = JsonUtils.readTransactionsFromClasspath("temp.json");
        Transaction transaction = JsonUtils.findTransactionById(transactions, transactionId);
        if (transaction == null) {
            System.out.println("transaction is null");
            return null;
        }
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/json");

        String input = transaction.toString().replace("\"", "\\\"").replace("\n", "\\n");

        String jsonBody = "{\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"content\": \"You are a helpful assistant that classifies bill items into categories such as Food and Dining, Transportation, Housing, Entertainment, Shopping, HealthcareEducation and Training, Communication, Finance and Investment, transfer accounts. Please classify the bill item below into one of these categories. Respond with only the category.\",\n" +
                "      \"role\": \"system\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"content\": \"" + input + "\",\n" +
                "      \"role\": \"user\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"model\": \"deepseek-chat\",\n" +
                "  \"max_tokens\": 2048,\n" +
                "  \"temperature\": 1,\n" +
                "  \"tool_choice\": \"none\",\n" +
                "  \"stream\": false\n" +
                "}";

        RequestBody body = RequestBody.create(jsonBody, mediaType);
        Request request = new Request.Builder()
                .url(API_URL)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("请求失败，状态码：" + response.code());
                return "请求失败：" + response.body().string();
            } else {

                System.out.println("request");

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body().string());
                JsonNode contentNode = root.path("choices").get(0).path("message").path("content");

                String category = contentNode.asText();
                System.out.println("update");
                JsonUtils.updateTempTransactionTypeById(transactionId, category);

                return contentNode.asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "调用失败：" + e.getMessage();
        }
    }

    /**
     * 批量对json文件里的transaction进行分类
     *
     * @param jsonPath json文件的路径
     */
    public static void classifyBatchTransaction(String jsonPath) throws IOException, InterruptedException {
        List<String> ids = JsonUtils.getAllTransactionIds(jsonPath);

        System.out.println(ids);

        // 使用 CountDownLatch 来确保所有异步请求都完成后再关闭线程池
        CountDownLatch latch = new CountDownLatch(ids.size());

        for (String id : ids) {
            executorService.submit(() -> {
                try {
                    String category = classifyTransaction(id);
                } finally {
                    latch.countDown();
                }
            });
        }

        System.out.println("Waiting for all tasks to finish...");
        // 等待所有请求完成
        latch.await();
        System.out.println("All tasks completed.");


        executorService.shutdown();
    }
}
