package pk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 示例：从类似微信的 CSV 文件中清洗并生成 JSON
 * 列顺序为：
 *  0: 交易时间
 *  1: 交易类型
 *  2: 交易对方
 *  3: 商品
 *  4: 收/支
 *  5: 金额(元)
 *  6: 支付方式
 *  7: 当前状态
 *  8: 交易单号
 *  9: 商户单号
 *  10: 备注
 */
public class CsvToJsonConverter {

    /**
     * 读取 CSV 文件，清洗/解析数据，返回 Transaction 列表
     *
     * @param csvFilePath CSV 文件路径
     * @return 交易记录列表
     */
    public List<Transaction> parseCsvToTransactions(String csvFilePath) {
        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean startParse = false; // 用于标记是否开始真正解析交易数据

            while ((line = br.readLine()) != null) {
                line = line.trim();
                // 跳过空行
                if (line.isEmpty()) {
                    continue;
                }

                // 根据逗号切分
                String[] cols = line.split(",", -1);
                // 如果实际 CSV 用 ";" 分隔，则改成 line.split(";", -1);

                // 判断是否是标题行（可根据关键列名，如 “交易时间”、“交易类型”等）
                if (!startParse) {
                    // 如果这一行包含特定关键字段，就说明接下来就是数据部分
                    // 例如："交易时间","交易类型","交易对方"
                    if (line.contains("交易时间") && line.contains("交易类型") && line.contains("金额")) {
                        startParse = true;
                    }
                    // 未开启解析前直接 continue
                    continue;
                }

                // 如果已经开始解析，但列数 < 11，则可能是无效行，跳过
                if (cols.length < 11) {
                    continue;
                }

                // 按顺序取值 (0~10)
                String transactionTime = cols[0].trim();    // 交易时间
                String transactionType = cols[1].trim();    // 交易类型
                String counterparty    = cols[2].trim();    // 交易对方
                String item           = cols[3].trim();     // 商品
                String incExp         = cols[4].trim();     // 收/支
                String amountStr      = cols[5].trim();     // 金额(元)
                String paymentMethod  = cols[6].trim();     // 支付方式
                String status         = cols[7].trim();     // 当前状态
                String transactionId  = cols[8].trim();     // 交易单号
                String merchantId     = cols[9].trim();     // 商户单号
                String note           = cols[10].trim();    // 备注

                // 如果 CSV 中的金额带 "¥" 符号，可去除
                amountStr = amountStr.replace("¥", "");

                // 解析金额
                double amount = 0.0;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    // 如果解析金额失败，可记录日志或跳过
                }

                // 进一步清洗，去掉多余空格
                transactionTime = transactionTime.replaceAll("\\s+", " ");
                transactionType = transactionType.replaceAll("\\s+", " ");
                counterparty    = counterparty.replaceAll("\\s+", " ");
                item           = item.replaceAll("\\s+", " ");
                incExp         = incExp.replaceAll("\\s+", " ");
                paymentMethod  = paymentMethod.replaceAll("\\s+", " ");
                status         = status.replaceAll("\\s+", " ");
                note           = note.replaceAll("\\s+", " ");

                // 构建 Transaction 对象
                Transaction tx = new Transaction();
                tx.setTransactionTime(transactionTime);
                tx.setTransactionType(transactionType);
                tx.setCounterparty(counterparty);
                tx.setItem(item);
                tx.setIncExp(incExp);
                tx.setAmount(amount);
                tx.setPaymentMethod(paymentMethod);
                tx.setStatus(status);
                tx.setTransactionId(transactionId);
                tx.setMerchantId(merchantId);
                tx.setNote(note);

                transactions.add(tx);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    /**
     * 将 Transaction 列表以 JSON 数组的形式写入文件
     *
     * @param transactions 交易列表
     * @param outputJson   输出 JSON 文件路径
     */
    public void writeTransactionsToJson(List<Transaction> transactions, String outputJson) {
        ObjectMapper mapper = new ObjectMapper();
        // 让输出格式更加美观
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try (FileWriter fw = new FileWriter(outputJson)) {
            mapper.writeValue(fw, transactions);
            System.out.println("成功处理 " + transactions.size() + " 条交易记录，已输出到 " + outputJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
