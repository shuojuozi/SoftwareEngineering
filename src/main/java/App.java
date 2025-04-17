import pojo.Transaction;
import utils.DeepSeek;
import utils.JsonUtils;

import java.io.IOException;
import java.util.List;

public class App {
    public static void main(String[] args) {
        String result = DeepSeek.classifyTransaction("1000050001202503050621116686652");
        System.out.println(result);
    }
}
