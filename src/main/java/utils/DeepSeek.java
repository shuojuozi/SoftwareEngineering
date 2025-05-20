package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import pojo.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class DeepSeek {
    private static final Logger logger = Logger.getLogger(DeepSeek.class.getName());
    private static final String API_KEY = ConfigUtil.getApiKey();
    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // 重用OkHttpClient
    private static final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 和DeepSeek进行对话
     *
     * @param userInput 用户的输入（字符串）
     * @return DeepSeek的返回内容
     */
    public static String communicate(String userInput) {
        MediaType mediaType = MediaType.parse("application/json");
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
                logger.severe("请求失败，状态码：" + response.code());
                return "请求失败：" + response.body().string();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body().string());
                JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
                return contentNode.asText();
            }
        } catch (IOException e) {
            logger.severe("调用失败：" + e.getMessage());
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

        List<Transaction> transactions = JsonUtils.readTransactionsFromClasspath("temp.json");
        Transaction transaction = JsonUtils.findTransactionById(transactions, transactionId);

        if (transaction == null) {
            logger.warning("transaction is null");
            return null;
        }

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
                logger.severe("请求失败，状态码：" + response.code());
                return "请求失败：" + response.body().string();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body().string());
                JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
                String category = contentNode.asText();
                JsonUtils.updateTempTransactionTypeById(transactionId, category);
                return category;
            }
        } catch (IOException e) {
            logger.severe("调用失败：" + e.getMessage());
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
        logger.info("要处理的交易ID数量: " + ids.size());

        Semaphore semaphore = new Semaphore(5); // 限制并发线程数
        CountDownLatch latch = new CountDownLatch(ids.size());

        for (String id : ids) {
            executorService.submit(() -> {
                try {
                    semaphore.acquire(); // 获取许可（控制并发数）
                    boolean success = false;
                    int attempts = 0;
                    while (!success && attempts < 3) { // 最多重试 3 次
                        attempts++;
                        try {
                            String category = classifyTransaction(id);
                            logger.info("交易ID: " + id + " 分类结果: " + category);
                            success = true;
                        } catch (Exception e) {
                            logger.warning("交易ID: " + id + " 第 " + attempts + " 次分类失败: " + e.getMessage());
                            Thread.sleep(1000); // 等待再重试
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    semaphore.release();
                    latch.countDown();
                }
            });
        }

        logger.info("等待所有任务完成...");
        latch.await();
        logger.info("全部任务已完成！");
    }

    /**
     * AI根据用户的消费行为提供一些个性化建议
     *
     * @param year 传输给AI的账单的年份
     * @param month 传输给AI的账单的月份
     * @return AI的回答
     */
    public static String budgetSuggestion(int year, int month) {
        List<Transaction> transactions = JsonUtils.getTransactionsByMonth(year, month);
        List<String> inputs = new ArrayList<>();
        for (Transaction transaction : transactions) {
            String input = transaction.toString().replace("\"", "\\\"").replace("\n", "\\n");
            inputs.add(input);
        }

        MediaType mediaType = MediaType.parse("application/json");
        String prompt = "Here is my recent billing data, please help me analyze it as follows: \n" +
                "1.Summarize each category of expenditure (such as catering, transportation, shopping, etc.) and indicate the category with the highest expenditure \n" +
                "2.Based on last month's expenses, it is recommended to have a reasonable budget for each category for next month\n" +
                "3.Assuming my monthly income is 5000 yuan, please suggest a reasonable savings target\n" +
                "4.Please indicate which expenditure categories can be reduced and provide cost saving suggestions\n" +
                "The billing data is as follows: \n" + inputs;

        String jsonBody = "{\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"content\": \"You are a helpful assistant who specializes in personal finance.\",\n" +
                "      \"role\": \"system\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"content\": \"" + prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\",\n" +
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
                logger.severe("请求失败，状态码：" + response.code());
                return "请求失败：" + response.body().string();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body().string());
                JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
                return contentNode.asText();
            }
        } catch (IOException e) {
            logger.severe("调用失败：" + e.getMessage());
            return "调用失败：" + e.getMessage();
        }
    }

    public static String analyzeSpendingBehavior(int year, int month) {
        // 获取指定月份的所有交易数据
        List<Transaction> transactions = JsonUtils.getTransactionsByMonth(year, month);
        List<String> inputs = new ArrayList<>();

        // 构建用于分析的输入内容
        for (Transaction transaction : transactions) {
            String input = transaction.toString().replace("\"", "\\\"").replace("\n", "\\n");
            inputs.add(input);
        }

        // 构建提示信息
        String prompt = "Here is my recent billing data, please help me analyze it as follows: \n" +
                "1.Summarize each category of expenditure (such as catering, transportation, shopping, etc.) and indicate the category with the highest expenditure. \n" +
                "2.Based on last month's expenses, it is recommended to have a reasonable budget for each category for next month.\n" +
                "3.Assuming my monthly income is 5000 yuan, please suggest a reasonable savings target.\n" +
                "4.Please indicate which expenditure categories can be reduced and provide cost-saving suggestions.\n" +
                "The billing data is as follows: \n" + String.join("\n", inputs);

        // 构建请求体
        MediaType mediaType = MediaType.parse("application/json");
        String jsonBody = "{\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"content\": \"You are a helpful assistant who specializes in personal finance.\",\n" +
                "      \"role\": \"system\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"content\": \"" + prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\",\n" +
                "      \"role\": \"user\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"model\": \"deepseek-chat\",\n" +
                "  \"max_tokens\": 2048,\n" +
                "  \"temperature\": 1,\n" +
                "  \"tool_choice\": \"none\",\n" +
                "  \"stream\": false\n" +
                "}";

        // 创建请求体和请求对象
        RequestBody body = RequestBody.create(jsonBody, mediaType);
        Request request = new Request.Builder()
                .url(API_URL)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        // 发送请求并处理响应
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.severe("请求失败，状态码：" + response.code());
                return "请求失败：" + response.body().string();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body().string());
                JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
                return contentNode.asText();
            }
        } catch (IOException e) {
            logger.severe("调用失败：" + e.getMessage());
            return "调用失败：" + e.getMessage();
        }
    }

}
