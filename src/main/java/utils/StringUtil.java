package utils;

/** Utility class for string cleaning operations */
public final class StringUtil {

    /** Remove all quotes and trim whitespace */
    public static String cleanId(String s) {
        return s == null ? "" : s.replace("\"", "").trim();
    }
}
