package pk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 示例：使用 OpenCSV 解析 CSV（含引号/逗号），再转换为 JSON
 * 列顺序为：
 *   0: 交易时间
 *   1: 交易类型
 *   2: 交易对方
 *   3: 商品
 *   4: 收/支
 *   5: 金额(元)
 *   6: 支付方式
 *   7: 当前状态
 *   8: 交易单号
 *   9: 商户单号
 *   10: 备注
 */
public class CsvToJsonConverter {

    public List<Transaction> parseCsvToTransactions(String csvFilePath) {
        List<Transaction> transactions = new ArrayList<>();
        boolean startParse = false; // 用于标记何时开始真正解析交易数据

        try (
            FileReader fr = new FileReader(csvFilePath);
            CSVReader csvReader = new CSVReaderBuilder(fr)
                    .withCSVParser(new CSVParserBuilder()
                            .withSeparator(',')          // 指定分隔符
                            .withIgnoreQuotations(false) // 允许解析引号
                            .build())
                    .build()
        ) {
            String[] cols;

            while ((cols = csvReader.readNext()) != null) {
                // 跳过空行
                if (cols.length == 0) {
                    continue;
                }

                // 根据业务逻辑判断何时开始解析(如遇到标题行"交易时间","交易类型","金额"等)
                if (!startParse) {
                    String line = String.join(",", cols);
                    if (line.contains("交易时间") && line.contains("交易类型") && line.contains("金额")) {
                        // 标题行出现 -> 后续行开始正式解析
                        startParse = true;
                    }
                    continue;
                }

                // 如果已经开始解析，但列数 < 11，则可能是无效行，跳过
                if (cols.length < 11) {
                    continue;
                }

                // 按照列顺序依次提取
                String transactionTime = cols[0].trim(); // 交易时间
                String transactionType = cols[1].trim(); // 交易类型
                String counterparty    = cols[2].trim(); // 交易对方
                String item           = cols[3].trim();  // 商品
                String incExp         = cols[4].trim();  // 收/支
                String amountStr      = cols[5].trim();  // 金额(元)
                String paymentMethod  = cols[6].trim();  // 支付方式
                String status         = cols[7].trim();  // 当前状态
                String transactionId  = cols[8].trim();  // 交易单号
                String merchantId     = cols[9].trim();  // 商户单号
                String note           = cols[10].trim(); // 备注

                // 如果金额带¥符号，去除
                amountStr = amountStr.replace("¥", "");

                // 解析金额
                double amount = 0.0;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    // 如果格式有问题，也可记录日志或跳过
                }

                // 做一些简单的空格清理
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

                // 收集到列表
                transactions.add(tx);
            }
        } catch (IOException | com.opencsv.exceptions.CsvValidationException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    /**
     * 将 Transaction 列表以 JSON 数组的形式写入文件
     */
    public void writeTransactionsToJson(List<Transaction> transactions, String outputJson) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // 美化输出

        try (FileWriter fw = new FileWriter(outputJson)) {
            mapper.writeValue(fw, transactions);
            System.out.println("成功处理 " + transactions.size() + " 条交易记录，已输出到 " + outputJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
