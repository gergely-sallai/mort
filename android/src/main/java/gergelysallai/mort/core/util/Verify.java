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
}
