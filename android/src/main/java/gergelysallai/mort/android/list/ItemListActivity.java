package gergelysallai.mort.android.list;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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


public class ItemListActivity extends LifecycleAppCompatActivity implements OnItemClickListener<RemoteDirectoryEntry> {

    private ConnectionManager connectionManager;
    private SftpHandler sftpHandler;
    private DirectoryListingAdapter adapter;

    private String host;
    private String user;
    private String password;

    private View messagePanel;
    private TextView messagePanelLabel;

    private View emptyPane;
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
        if (!connectionManager.isInitialized()) {
            connectionManager.init(host);
        } else if (connectionManager.hasSftpHandler()) {
            sftpHandler = connectionManager.getSftpHandler();
        }
        connectionManager.getConnectionStateData().observe(this, new ConnectionStateObserver());
        connectionManager.getSftpStateData().observe(this, new SftpStateObserver());

    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        messagePanel = findViewById(R.id.message_panel);
        messagePanelLabel = (TextView) findViewById(R.id.message_label);

        emptyPane = findViewById(R.id.empty_pane);
        progressPane = findViewById(R.id.progress_pane);
        recyclerView = (RecyclerView) findViewById(R.id.item_list);

        final Button messagePanelSignInButton = (Button) findViewById(R.id.relogin_button);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        setSupportActionBar(toolbar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        messagePanelSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ConfigActivity.createIntent(ItemListActivity.this));
                finish();
            }
        });
    }

    private void loadValuesFromIntent(Intent intent) {
        host = intent.getStringExtra(ConfigActivity.HOST_NAME_KEY);
        user = intent.getStringExtra(ConfigActivity.USER_NAME_KEY);
        password = intent.getStringExtra(ConfigActivity.PASSWORD_KEY);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new DirectoryListingAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void showProgressPane() {
        recyclerView.setVisibility(View.GONE);
        messagePanel.setVisibility(View.GONE);
        emptyPane.setVisibility(View.GONE);

        progressPane.setVisibility(View.VISIBLE);
    }

    private void showErrorPane(@StringRes int res) {
        recyclerView.setVisibility(View.GONE);
        emptyPane.setVisibility(View.GONE);
        progressPane.setVisibility(View.GONE);

        messagePanel.setVisibility(View.VISIBLE);
        messagePanelLabel.setText(res);
    }

    private void showClosedPane() {
        recyclerView.setVisibility(View.GONE);
        emptyPane.setVisibility(View.GONE);
        progressPane.setVisibility(View.GONE);

        messagePanel.setVisibility(View.VISIBLE);
        messagePanelLabel.setText(R.string.closed_sftp);
    }

    private void showEmptyPane() {
        recyclerView.setVisibility(View.GONE);
        progressPane.setVisibility(View.GONE);
        messagePanel.setVisibility(View.GONE);

        emptyPane.setVisibility(View.VISIBLE);
    }

    private void showRecyclerView() {
        progressPane.setVisibility(View.GONE);
        messagePanel.setVisibility(View.GONE);
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

    @Override
    public void onItemClicked(RemoteDirectoryEntry item) {
        if (item.isDirectory) {
            showProgressPane();
            sftpHandler.ls(item);
        } else {
            Snackbar.make(recyclerView, item.canonicalName, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok, null).show();
        }
    }

    private class ConnectionStateObserver implements Observer<ConnectionState> {
        @Override
        public void onChanged(ConnectionState connectionState) {
            switch (connectionState) {
                case Initialized:
                    connectionManager.connect(user, password);
                    break;
                case Connected:
                    Timber.d("Connected");
                    if (!connectionManager.hasSftpHandler()) {
                        connectionManager.createSftp();
                    }
                    break;
                case AuthenticationError:
                    Timber.e("AuthenticationError");
                    handleAuthError();
                    break;
                case Disconnected:
                    Timber.i("Disconnected");
                    handleDisconnected();
                    break;
                case ConnectionError:
                    Timber.e("ConnectionError");
                    handleConnectionError();
                    break;
            }
        }

        private void handleDisconnected() {
            showErrorPane(R.string.error_disconnected);
        }

        private void handleConnectionError() {
            connectionManager.disconnect();
            showErrorPane(R.string.error_connection);
        }

        private void handleAuthError() {
            connectionManager.disconnect();
            showErrorPane(R.string.error_authentication);
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
                        Timber.w(sftpPair.second.current.fileName);
                        Timber.e(sftpPair.second.current.canonicalName);
                        updateTitle(sftpPair.second.current.canonicalName);
                        adapter.onDirectoryListingUpdate(sftpPair);
                    } else {
                        sftpHandler = connectionManager.getSftpHandler();
                        sftpHandler.lsHome();
                    }
                    break;
                case Error:
                    handleSftpError();
                    break;
                case Closed:
                    showClosedPane();
                    break;
            }
        }

        private void handleSftpError() {
            showRecyclerView();
            Snackbar.make(recyclerView, R.string.error_sftp, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok, null).show();
        }
    }
}
