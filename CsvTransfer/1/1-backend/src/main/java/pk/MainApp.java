package pk;

import java.util.List;
import java.io.File;
import java.io.IOException;

public class MainApp {
    public static void main(String[] args) {
        try {
            String csvInput = "C:\\Users\\rebornxd\\Desktop\\SoftEngineering\\project\\CsvTransfer\\data\\alipay_record_20250411_181043.csv";
            
            // 从输入文件路径生成输出文件路径
            File inputFile = new File(csvInput);
            String inputPath = inputFile.getParent();
            String fileName = inputFile.getName();
            String jsonOutput = inputPath + File.separator + fileName.substring(0, fileName.lastIndexOf(".")) + ".json";

            CsvToJsonConverter converter = new CsvToJsonConverter();
            List<Transaction> transactions = converter.parseCsvToTransactions(csvInput);
            converter.writeTransactionsToJson(transactions, jsonOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}