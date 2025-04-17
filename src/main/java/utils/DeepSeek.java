package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import pojo.Transaction;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class DeepSeek {
    private static final Logger logger = Logger.getLogger(DeepSeek.class.getName());
    private static final String API_KEY = ConfigUtil.getApiKey();
    private static final String API_URL = "https://api.deepseek.com/chat/completions";

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
        transactionId = "\"" + transactionId + "\"";
        List<Transaction> transactions = JsonUtils.readTransactionsFromClasspath("transactionData.json");
        Transaction transaction = JsonUtils.findTransactionById(transactions, transactionId);
        if (transaction == null) return null;
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
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body().string());
                JsonNode contentNode = root.path("choices").get(0).path("message").path("content");

                String category = contentNode.asText();
                JsonUtils.updateTransactionTypeById(transactionId, "\"" + category + "\"");

                return contentNode.asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "调用失败：" + e.getMessage();
        }
    }
}
