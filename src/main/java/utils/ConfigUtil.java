package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {
    private static final Properties props = new Properties();

    static {
        try (InputStream inputStream = ConfigUtil.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("找不到 config.properties 文件，请确保它位于 src/main/resources 下");
            }
            props.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("读取 config.properties 失败", e);
        }
    }

    /**
     * 从resources的config.properties中取得api_key（该文件没有传到github上）
     *
     * @return 字符串api_key
     */
    public static String getApiKey() {
        return props.getProperty("api.key");
    }
}
