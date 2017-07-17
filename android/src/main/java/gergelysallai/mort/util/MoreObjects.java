package gergelysallai.mort.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static gergelysallai.mort.util.Verify.verifyNotNull;

public class MoreObjects {
    private MoreObjects() { }

    public static <T> T firstNonNull(@Nullable T first, @NonNull T second) {
        if (first == null) {
            return verifyNotNull(second, "Second value can not be null!");
        }
        return first;
    }
}
