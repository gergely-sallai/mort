package gergelysallai.mort.android.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import gergelysallai.mort.android.LifecycleAppCompatActivity;
import gergelysallai.mort.android.R;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;


import static gergelysallai.mort.android.detail.DetailFragment.DIRECTORY_ENTRY_KEY;

public class DetailActivity extends LifecycleAppCompatActivity {

    private static final String FRAGMENT_TAG = "mort.android.DetailsFragmentTag";

    public static Intent createIntent(RemoteDirectoryEntry remoteDirectoryEntry, Context context) {
        final Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(DIRECTORY_ENTRY_KEY, remoteDirectoryEntry);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        final RemoteDirectoryEntry directoryEntry = (RemoteDirectoryEntry) getIntent().getSerializableExtra(DIRECTORY_ENTRY_KEY);
        if (getFragmentManager().findFragmentByTag(FRAGMENT_TAG) == null) {
            final DetailFragment detailFragment = DetailFragment.createInstance(directoryEntry);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.details_fragment_container, detailFragment, FRAGMENT_TAG)
                    .commit();
        }
    }
}
