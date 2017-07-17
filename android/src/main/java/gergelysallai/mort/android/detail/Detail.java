package gergelysallai.mort.android.detail;

import android.support.annotation.NonNull;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;

public final class Detail {
    public static final TitleUpdateListener NoOpTitleUpdateListener = new TitleUpdateListener() {
        @Override
        public void onUpdateTitle(String title, String subtitle) {
            // No op
        }
    };

    private Detail() {}

    interface TitleUpdateListener {
        void onUpdateTitle(String title, String subtitle);
    }

    public interface ResultListener {
        void onResult(@NonNull RemoteDirectoryEntry file, @NonNull String title, int year, boolean isMovie);
    }
}

