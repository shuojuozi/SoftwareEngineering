package pk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonMerger {

    private static final ObjectMapper mapper = new ObjectMapper();
    // 示例时间格式: "2025-03-24 13:14:10"
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void main(String[] args) throws IOException {
        // 示例：可以一次性合并多个 JSON 文件
        List<String> jsonFiles = Arrays.asList(
            "C:\\Users\\rebornxd\\Desktop\\SoftEngineering\\project\\CsvTransfer\\data\\微信支付账单(20250225-20250324).json",
            "C:\\Users\\rebornxd\\Desktop\\SoftEngineering\\project\\CsvTransfer\\data\\微信支付账单(20250311-20250411).json"
   
        );

        // 合并并去重/处理冲突
        List<Transaction> mergedList = mergeAndRemoveDuplicates(jsonFiles);

        // 按交易时间排序（如果需要）
        mergedList.sort(Comparator.comparing((Transaction t) -> parseDate(t.getTransactionTime()), 
            Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        // 输出结果
        String mergedOutput = "transactions_merged.json";
        mapper.writeValue(new File(mergedOutput), mergedList);
        System.out.println("合并完成，结果已写入: " + mergedOutput);
    }

    /**
     * 读取多个 JSON 文件，合并并去重。冲突策略：如果 transactionId 相同，则保留“交易时间较晚”的那条。
     * 可根据需求自行更改逻辑。
     */
    private static List<Transaction> mergeAndRemoveDuplicates(List<String> jsonPaths) throws IOException {
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
     * 从文件中读取 JSON 数组 -> List<Transaction>
     */
    private static List<Transaction> readJsonFile(String jsonPath) throws IOException {
        File f = new File(jsonPath);
        if (!f.exists()) {
            System.err.println("文件不存在: " + jsonPath);
            return Collections.emptyList();
        }
        return mapper.readValue(f, new TypeReference<List<Transaction>>() {});
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
}
