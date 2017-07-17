package gergelysallai.mort.android.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import gergelysallai.mort.android.LifecycleAppCompatActivity;
import gergelysallai.mort.android.R;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;


import static gergelysallai.mort.android.detail.DetailFragment.DIRECTORY_ENTRY_KEY;
import static gergelysallai.mort.util.Verify.verifyNotNull;

public class DetailActivity extends LifecycleAppCompatActivity implements Detail.TitleUpdateListener, Detail.ResultListener {

    public static final String TITLE_KEY = "mort.android.ResultTitleKey";
    public static final String YEAR_KEY = "mort.android.ResultYearKey";
    public static final String ISMOVIE_KEY = "mort.android.ResultIsMovieKey";

    private static final String FRAGMENT_TAG = "mort.android.DetailsFragmentTag";

    public static Intent createIntent(RemoteDirectoryEntry remoteDirectoryEntry, Context context) {
        final Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(DIRECTORY_ENTRY_KEY, remoteDirectoryEntry);
        return intent;
    }

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        final RemoteDirectoryEntry directoryEntry = (RemoteDirectoryEntry) getIntent().getSerializableExtra(DIRECTORY_ENTRY_KEY);
        if (getFragmentManager().findFragmentByTag(FRAGMENT_TAG) == null) {
            final DetailFragment detailFragment = DetailFragment.createInstance(directoryEntry);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.details_fragment_container, detailFragment, FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onUpdateTitle(String title, String subtitle) {
        toolbar.setTitle(title);
        toolbar.setSubtitle(subtitle);
    }

    @Override
    public void onResult(@NonNull RemoteDirectoryEntry file, @NonNull String title, int year, boolean isMovie) {
        verifyNotNull(file, "File can not be null!");
        verifyNotNull(title, "Title can not be null!");
        final Intent intent = new Intent();
        intent.putExtra(DIRECTORY_ENTRY_KEY, file);
        intent.putExtra(TITLE_KEY, title);
        intent.putExtra(YEAR_KEY, year);
        intent.putExtra(ISMOVIE_KEY, isMovie);
        setResult(RESULT_OK, intent);
        finish();
    }
}
