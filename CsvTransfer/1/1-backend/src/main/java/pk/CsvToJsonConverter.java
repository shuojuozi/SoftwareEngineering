package pk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    public List<Transaction> parseCsvToTransactions(String csvFilePath) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        boolean startParse = false;          // 标记何时开始真正解析
        boolean isAlipay  = csvFilePath.contains("alipay_record");      // 判断文件格式
        boolean isWeChat  = csvFilePath.contains("微信支付账单");
        Charset charset = StandardCharsets.UTF_8;
        if (isAlipay) {
            charset = Charset.forName("GBK");          // 支付宝对账单 → GBK
        }
Reader reader = new InputStreamReader(new FileInputStream(csvFilePath), charset);

/* ---------- 用 OpenCSV 构造 CSVReader ---------- */
try (CSVReader csvReader = new CSVReaderBuilder(reader)
         .withCSVParser(new CSVParserBuilder()
                 .withSeparator(',')              // 若是 TAB，可检测后换 '\t'
                 .withIgnoreQuotations(false)
                 .build())
         .build()) {
            String[] cols;
            while ((cols = csvReader.readNext()) != null) {
                if (cols.length == 0) continue;                     // 跳过空行
    
                /* ---- 识别标题行，开始进入数据区 ---- */
                if (!startParse) {
                    String header = String.join(",", cols);
                    if (header.contains("交易时间") &&
                        (header.contains("交易类型") || header.contains("交易分类")) &&
                        header.contains("金额")) {
                        startParse = true;
                    }
                    continue;
                }
    
                /* ---- 根据文件类型做列数和位置检查 ---- */
                if (isAlipay) {
                    /* 新格式应有 12 列；不足直接跳过 */
                    if (cols.length < 12) continue;
    
                    String transactionTime = cols[0].trim();
                    String transactionType = cols[1].trim();        // 交易分类
                    String counterparty    = cols[2].trim();        // 交易对方
                    /* cols[3] = 对方账号 -> 忽略 */
                    String item           = cols[4].trim();         // 商品说明
                    String incExp         = cols[5].trim();         // 收/支
                    String amountStr      = cols[6].trim();         // 金额
                    String paymentMethod  = cols[7].trim();         // 收/付款方式
                    String status         = cols[8].trim();         // 交易状态
                    String transactionId  = cols[9].trim();         // 交易订单号
                    String merchantId     = cols[10].trim();        // 商家订单号
                    String note           = cols[11].trim();        // 备注
    
                    addTransaction(transactions, transactionTime, transactionType,
                                   counterparty, item, incExp, amountStr,
                                   paymentMethod, status, transactionId, merchantId, note);
    
                } else { /* 默认按微信旧格式解析 */
    
                    if (cols.length < 11) continue;
    
                    String transactionTime = cols[0].trim();
                    String transactionType = cols[1].trim();        // 交易类型
                    String counterparty    = cols[2].trim();        // 交易对方
                    String item           = cols[3].trim();         // 商品
                    String incExp         = cols[4].trim();         // 收/支
                    String amountStr      = cols[5].trim();         // 金额(元)
                    String paymentMethod  = cols[6].trim();         // 支付方式
                    String status         = cols[7].trim();         // 当前状态
                    String transactionId  = cols[8].trim();         // 交易单号
                    String merchantId     = cols[9].trim();         // 商户单号
                    String note           = cols[10].trim();        // 备注
    
                    addTransaction(transactions, transactionTime, transactionType,
                                   counterparty, item, incExp, amountStr,
                                   paymentMethod, status, transactionId, merchantId, note);
                }
            }
        

    /* 之后保持你原来的 while ((cols = csvReader.readNext()) != null) { … } 逻辑 */

} catch (IOException | com.opencsv.exceptions.CsvValidationException e) {
            e.printStackTrace();
        }
    
        return transactions;
    }
    
    /* 将公共的“构造 Transaction + 金额解析 + 清洗”提出来，便于两种格式共用 */
    private void addTransaction(List<Transaction> list,
                                String time, String type, String cp, String item,
                                String incExp, String amtStr, String payMethod,
                                String status, String txId, String mchId, String note) {
    
        double amount = 0.0;
        if (amtStr != null) amtStr = amtStr.replace("¥", "").trim();
        try { amount = Double.parseDouble(amtStr); } catch (NumberFormatException ignored) {}
    
        Transaction tx = new Transaction();
        tx.setTransactionTime(time.replaceAll("\\s+", " "));
        tx.setTransactionType(type.replaceAll("\\s+", " "));
        tx.setCounterparty(cp.replaceAll("\\s+", " "));
        tx.setItem(item.replaceAll("\\s+", " "));
        tx.setIncExp(incExp.replaceAll("\\s+", " "));
        tx.setAmount(amount);
        tx.setPaymentMethod(payMethod.replaceAll("\\s+", " "));
        tx.setStatus(status.replaceAll("\\s+", " "));
        tx.setTransactionId(txId);
        tx.setMerchantId(mchId);
        tx.setNote(note.replaceAll("\\s+", " "));
    
        list.add(tx);
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
