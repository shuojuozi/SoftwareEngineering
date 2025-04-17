import pojo.Transaction;
import utils.DeepSeek;
import utils.JsonUtils;

import java.io.IOException;
import java.util.List;

public class App {
    public static void main(String[] args) {
        String result = DeepSeek.classifyTransaction("4200002635202503247357723190");
        System.out.println(result);
    }
}
