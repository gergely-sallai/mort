package gergelysallai.mort.android.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

public class Settings {
    private static final String PREFERENCES = "Settings.SharedPreferencesKey";
    private static final String MOVIES_KEY = "Settings.MoviesKey";
    private static final String TVSHOWS_KEY = "Settings.TvShowsKey";
    private static final String FAVORITE1_KEY = "Settings.Favorite1Key";
    private static final String FAVORITE2_KEY = "Settings.Favorite2Key";
    private static final String FAVORITE3_KEY = "Settings.Favorite3Key";

    @Nullable
    public final String moviesPath;
    @Nullable
    public final String tvShowPath;
    @Nullable
    public final String favorite1;
    @Nullable
    public final String favorite2;
    @Nullable
    public final String favorite3;

    Settings(@Nullable String moviesPath,
             @Nullable String tvShowPath,
             @Nullable String favorite1,
             @Nullable String favorite2,
             @Nullable String favorite3) {
        this.moviesPath = moviesPath;
        this.tvShowPath = tvShowPath;
        this.favorite1 = favorite1;
        this.favorite2 = favorite2;
        this.favorite3 = favorite3;
    }

    static void saveSettings(Settings settings, Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        preferences
            .edit()
            .putString(MOVIES_KEY, settings.moviesPath)
            .putString(TVSHOWS_KEY, settings.tvShowPath)
            .putString(FAVORITE1_KEY, settings.favorite1)
            .putString(FAVORITE2_KEY, settings.favorite2)
            .putString(FAVORITE3_KEY, settings.favorite3)
            .apply();
    }

    public static Settings loadSettings(Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        final String movies = preferences.getString(MOVIES_KEY, null);
        final String tvShows = preferences.getString(TVSHOWS_KEY, null);
        final String fav1 = preferences.getString(FAVORITE1_KEY, null);
        final String fav2 = preferences.getString(FAVORITE2_KEY, null);
        final String fav3 = preferences.getString(FAVORITE3_KEY, null);

        return new Settings(movies, tvShows, fav1, fav2, fav3);
    }
}
