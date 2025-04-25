import pojo.Transaction;
import utils.DeepSeek;
import utils.JsonUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        JsonUtils.parseCsv2Json("src/main/resources/data/csv/微信支付账单(20250225-20250324).csv");
//        JsonUtils.updateTransactionTypeById("1000050001202503050621116686652", "test");
//        String s = DeepSeek.budgetSuggestion(2025, 3);
//        System.out.println(s);
    }
}
