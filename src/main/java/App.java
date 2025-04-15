import utils.DeepSeek;

public class App {
    public static void main(String[] args) {
        String result = DeepSeek.communicate("这是一个接口测试，收到请回复");
        System.out.println(result);
    }
}
