package gergelysallai.mort.android.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import gergelysallai.mort.android.LifecycleAppCompatActivity;
import gergelysallai.mort.android.R;
import gergelysallai.mort.core.data.DirectoryListing;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;

import java.util.ArrayList;
import java.util.List;


public class ItemListActivity extends LifecycleAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        View recyclerView = findViewById(R.id.item_list);
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        final DirectoryListingAdapter adapter = new DirectoryListingAdapter(null);
        recyclerView.setAdapter(adapter);
        adapter.onDirectoryList(createData());
    }

    static DirectoryListing createData() {
        final List<RemoteDirectoryEntry> entries = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            final boolean isDir = i < 18;
            entries.add(new RemoteDirectoryEntry("Folder "+i, isDir, !isDir, false, 1000L + i));
        }
        final RemoteDirectoryEntry parent = new RemoteDirectoryEntry("/", true, false, false, 2000L);
        final RemoteDirectoryEntry current = new RemoteDirectoryEntry("/home", true, false, false, 100L);
        return new DirectoryListing(current, parent, entries);
    }
}
