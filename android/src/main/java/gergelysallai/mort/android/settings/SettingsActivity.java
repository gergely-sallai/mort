package gergelysallai.mort.android.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import gergelysallai.mort.android.R;

public class SettingsActivity extends AppCompatActivity {

    private EditText movies;
    private EditText tvShows;
    private EditText favorite1;
    private EditText favorite2;
    private EditText favorite3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Settings settings = Settings.loadSettings(this);

        movies = findViewById(R.id.movies);
        tvShows = findViewById(R.id.tvshows);
        favorite1 = findViewById(R.id.favorite1);
        favorite2 = findViewById(R.id.favorite2);
        favorite3 = findViewById(R.id.favorite3);

        movies.setText(settings.moviesPath);
        tvShows.setText(settings.tvShowPath);
        favorite1.setText(settings.favorite1);
        favorite2.setText(settings.favorite2);
        favorite3.setText(settings.favorite3);
    }

    @Override
    protected void onPause() {
        super.onPause();
        final String moviesPath = movies.getText().toString();
        final String tvShowsPath = tvShows.getText().toString();
        final String fav1Path = favorite1.getText().toString();
        final String fav2Path = favorite2.getText().toString();
        final String fav3Path = favorite3.getText().toString();
        final Settings settings = new Settings(
                moviesPath,
                tvShowsPath,
                fav1Path,
                fav2Path,
                fav3Path
        );
        Settings.saveSettings(settings, this);
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }
}
