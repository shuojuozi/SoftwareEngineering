package utils;
import pojo.Transaction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.io.File;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从类路径下的 JSON 文件读取交易数据
     * @param resourcePath 类路径下的资源文件路径（如 "transactions.json"）
     * @return 交易记录列表（如果文件不存在或解析失败，返回空列表）
     */
    public static List<Transaction> readTransactionsFromClasspath(String resourcePath) {
        try (InputStream inputStream = JsonUtils.class.getClassLoader().getResourceAsStream("data/1.json")) {
            if (inputStream == null) {
                System.err.println("未找到文件: " + resourcePath);
                return Collections.emptyList();
            }
            // 将 JSON 数据解析为 List<Transaction>
            return objectMapper.readValue(inputStream, new TypeReference<List<Transaction>>() {});
        } catch (IOException e) {
            System.err.println("解析 JSON 失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 从文件系统路径读取 JSON 数据
     * @param filePath 文件绝对路径（如 "/data/transactions.json"）
     */

    public static List<Transaction> readTransactionsFromFile(String filePath) {
        try {
            return objectMapper.readValue(new File("/data/1.json"), new TypeReference<List<Transaction>>() {});
        } catch (IOException e) {
            System.err.println("读取文件失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}