package gergelysallai.mort.core.util;

public class Verify {

    public static <T> T verifyNotNull(T value) {
        return verifyNotNull(value, null);
    }

    public static <T> T verifyNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }

    public static boolean verify(boolean expression) {
        return verify(expression, null);
    }

    public static boolean verify(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
        return true;
    }
}
