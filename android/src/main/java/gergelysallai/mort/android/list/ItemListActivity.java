package gergelysallai.mort.android.list;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;
import gergelysallai.mort.android.LifecycleAppCompatActivity;
import gergelysallai.mort.android.R;
import gergelysallai.mort.android.config.ConfigActivity;
import gergelysallai.mort.android.connection.ConnectionManager;
import gergelysallai.mort.android.connection.SftpState;
import gergelysallai.mort.core.data.DirectoryListing;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;
import gergelysallai.mort.core.ssh.ConnectionState;
import gergelysallai.mort.core.ssh.SftpHandler;
import timber.log.Timber;


public class ItemListActivity extends LifecycleAppCompatActivity {

    private ConnectionManager connectionManager;
    private SftpHandler sftpHandler;
    private boolean firstConnect;
    private DirectoryListingAdapter adapter;

    private String host;
    private String user;
    private String password;

    private View errorPane;
    private View emptyPane;
    private View closedPane;
    private View progressPane;
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        loadValuesFromIntent(getIntent());
        initViews();
        setupRecyclerView(recyclerView);

        connectionManager = ViewModelProviders.of(this).get(ConnectionManager.class);
        if (savedInstanceState == null) {
            connectionManager.init(host);
            firstConnect = true;
        }
        connectionManager.getConnectionStateData().observe(this, new ConnectionStateObserver());
        connectionManager.getSftpStateData().observe(this, new SftpStateObserver());

    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        errorPane = findViewById(R.id.error_pane);
        emptyPane = findViewById(R.id.empty_pane);
        closedPane = findViewById(R.id.closed_pane);
        progressPane = findViewById(R.id.progress_pane);
        recyclerView = (RecyclerView) findViewById(R.id.item_list);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        setSupportActionBar(toolbar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void loadValuesFromIntent(Intent intent) {
        host = intent.getStringExtra(ConfigActivity.HOST_NAME_KEY);
        user = intent.getStringExtra(ConfigActivity.USER_NAME_KEY);
        password = intent.getStringExtra(ConfigActivity.PASSWORD_KEY);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new DirectoryListingAdapter(null);
        recyclerView.setAdapter(adapter);
    }

    private void showProgressPane() {
        recyclerView.setVisibility(View.GONE);
        closedPane.setVisibility(View.GONE);
        emptyPane.setVisibility(View.GONE);
        errorPane.setVisibility(View.GONE);

        progressPane.setVisibility(View.VISIBLE);
    }

    private void showErrorPane() {
        recyclerView.setVisibility(View.GONE);
        closedPane.setVisibility(View.GONE);
        emptyPane.setVisibility(View.GONE);
        progressPane.setVisibility(View.GONE);

        errorPane.setVisibility(View.VISIBLE);
    }

    private void showClosedPane() {
        recyclerView.setVisibility(View.GONE);
        emptyPane.setVisibility(View.GONE);
        progressPane.setVisibility(View.GONE);
        errorPane.setVisibility(View.GONE);

        closedPane.setVisibility(View.VISIBLE);
    }

    private void showEmptyPane() {
        recyclerView.setVisibility(View.GONE);
        progressPane.setVisibility(View.GONE);
        errorPane.setVisibility(View.GONE);
        closedPane.setVisibility(View.GONE);

        emptyPane.setVisibility(View.VISIBLE);
    }

    private void showRecyclerView() {
        progressPane.setVisibility(View.GONE);
        errorPane.setVisibility(View.GONE);
        closedPane.setVisibility(View.GONE);
        emptyPane.setVisibility(View.GONE);

        recyclerView.setVisibility(View.VISIBLE);
    }

    private void updateTitle(String title) {
        toolbar.setSubtitle(title);
    }

    public static Intent createIntent(@NonNull String host, @NonNull String user, @NonNull String password, Context context) {
        final Intent intent = new Intent(context, ItemListActivity.class);
        intent.putExtra(ConfigActivity.HOST_NAME_KEY, host);
        intent.putExtra(ConfigActivity.USER_NAME_KEY, user);
        intent.putExtra(ConfigActivity.PASSWORD_KEY, password);
        return intent;
    }

    private class ConnectionStateObserver implements Observer<ConnectionState> {
        @Override
        public void onChanged(ConnectionState connectionState) {
            switch (connectionState) {
                case Connected:
                    Timber.d("Connected");
                    connectionManager.createSftp();
                    break;
                case AuthenticationError:
                    Timber.e("AuthenticationError");
                    handleAuthError();
                    break;
                case Disconnected:
                    Timber.i("Disconnected");
                    if (firstConnect) {
                        connectionManager.connect(user, password);
                        firstConnect = false;
                    }
                    break;
                case ConnectionError:
                    Timber.e("ConnectionError");
                    handleConnectionError();
                    break;
            }
        }

        private void handleConnectionError() {
            connectionManager.disconnect();
            Toast.makeText(ItemListActivity.this, R.string.error_connection, Toast.LENGTH_LONG).show();
            startActivity(ConfigActivity.createIntent(ItemListActivity.this));
            finish();
        }

        private void handleAuthError() {
            connectionManager.disconnect();
            Toast.makeText(ItemListActivity.this, R.string.error_authentication, Toast.LENGTH_LONG).show();
            startActivity(ConfigActivity.createIntent(ItemListActivity.this));
            finish();
        }
    }

    private class SftpStateObserver implements Observer<Pair<SftpState, DirectoryListing>> {

        @Override
        public void onChanged(Pair<SftpState, DirectoryListing> sftpPair) {
            switch (sftpPair.first) {
                case Ok:
                    if (sftpPair.second != null) {
                        if (sftpPair.second.entries.size() > 0) {
                            showRecyclerView();
                        } else {
                            showEmptyPane();
                        }
                        updateTitle(sftpPair.second.current.fileName);
                        adapter.onDirectoryListingUpdate(sftpPair);
                    } else {
                        sftpHandler = connectionManager.getSftpHandler();
                        sftpHandler.ls(new RemoteDirectoryEntry("/storage", true, false, false, 0L));
                    }
                    break;
                case Error:
                    showErrorPane();
                    break;
                case Closed:
                    showClosedPane();
                    break;
            }
        }
    }
}
