package gergelysallai.mort.android.list;

import android.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import gergelysallai.mort.android.LifecycleAppCompatActivity;
import gergelysallai.mort.android.R;
import gergelysallai.mort.android.login.LoginActivity;
import gergelysallai.mort.android.connection.ConnectionManager;
import gergelysallai.mort.android.connection.SftpState;
import gergelysallai.mort.android.detail.Detail;
import gergelysallai.mort.android.detail.DetailActivity;
import gergelysallai.mort.android.detail.DetailFragment;
import gergelysallai.mort.android.settings.Settings;
import gergelysallai.mort.android.settings.SettingsActivity;
import gergelysallai.mort.core.data.DirectoryListing;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;
import gergelysallai.mort.core.ssh.ConnectionState;
import gergelysallai.mort.core.ssh.SftpHandler;
import gergelysallai.mort.core.ssh.TaskCompletionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import timber.log.Timber;


import java.util.Locale;
import java.util.Set;

import static gergelysallai.mort.android.detail.DetailActivity.ISMOVIE_KEY;
import static gergelysallai.mort.android.detail.DetailActivity.TITLE_KEY;
import static gergelysallai.mort.android.detail.DetailActivity.YEAR_KEY;
import static gergelysallai.mort.android.detail.DetailFragment.DIRECTORY_ENTRY_KEY;
import static gergelysallai.mort.util.Verify.verifyNotNull;


public class ItemListActivity extends LifecycleAppCompatActivity implements OnItemClickListener<RemoteDirectoryEntry>, Detail.ResultListener {

    private static final Logger logger = LoggerFactory.getLogger(ItemListActivity.class);

    private static final String FRAGMENT_TAG = "mort.android.DetailsFragmentTag";
    private static final int RESULT_REQUEST_CODE = 1337;
    private static final int FAV1 = 7663;
    private static final int FAV2 = 7664;
    private static final int FAV3 = 7665;

    private ConnectionManager connectionManager;
    private SftpHandler sftpHandler;
    private DirectoryListingAdapter adapter;
    private Settings settings;

    private boolean isTwoPane;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        settings = Settings.loadSettings(this);
        if (TextUtils.isEmpty(settings.moviesPath) || TextUtils.isEmpty(settings.tvShowPath)) {
            startActivity(SettingsActivity.createIntent(this));
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean hasFav = false;
        final SubMenu subMenu = menu.findItem(R.id.overflow_bookmarks).getSubMenu();
        subMenu.removeItem(FAV1);
        subMenu.removeItem(FAV2);
        subMenu.removeItem(FAV3);
        if (!TextUtils.isEmpty(settings.favorite1)) {
            hasFav = true;
            subMenu.add(R.id.group_overflow_bookmarks, FAV1, Menu.NONE, getString(R.string.bookmark, 1));
        }
        if (!TextUtils.isEmpty(settings.favorite2)) {
            hasFav = true;
            subMenu.add(R.id.group_overflow_bookmarks, FAV2, Menu.NONE, getString(R.string.bookmark, 2));

        }
        if (!TextUtils.isEmpty(settings.favorite3)) {
            hasFav = true;
            subMenu.add(R.id.group_overflow_bookmarks, FAV3, Menu.NONE, getString(R.string.bookmark, 3));

        }
        menu.findItem(R.id.overflow_bookmarks).setVisible(hasFav);
        menu.findItem(R.id.overflow_movies).setVisible(!TextUtils.isEmpty(settings.moviesPath));
        menu.findItem(R.id.overflow_tvshows).setVisible(!TextUtils.isEmpty(settings.tvShowPath));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(SettingsActivity.createIntent(this));
                return true;
            case R.id.overflow_movies:
                sftpHandler.ls(settings.moviesPath);
                return true;
            case R.id.overflow_tvshows:
                sftpHandler.ls(settings.tvShowPath);
                return true;
            case FAV1:
                sftpHandler.ls(settings.favorite1);
                return true;
            case FAV2:
                sftpHandler.ls(settings.favorite2);
                return true;
            case FAV3:
                sftpHandler.ls(settings.favorite3);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(RemoteDirectoryEntry item) {
        if (item.isDirectory) {
            showProgressPane();
            sftpHandler.ls(item);
        } else {
            openDetails(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_REQUEST_CODE && resultCode == RESULT_OK) {
            final RemoteDirectoryEntry file = (RemoteDirectoryEntry) data.getSerializableExtra(DIRECTORY_ENTRY_KEY);
            final String title = data.getStringExtra(TITLE_KEY);
            final int year = data.getIntExtra(YEAR_KEY, 0);
            final boolean isMovie = data.getBooleanExtra(ISMOVIE_KEY, false);
            onResult(file, title, year, isMovie);
        }
    }

    @Override
    public void onResult(@NonNull RemoteDirectoryEntry file, @NonNull String title, int year, boolean isMovie) {
        if (isTwoPane) {
            final Fragment fragment = getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
            verifyNotNull(fragment, "Fragment must not be null");
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }

        final String containerDir = isMovie ? settings.moviesPath : settings.tvShowPath;
        final String dirName = String.format(Locale.ENGLISH, "%s/%s (%d)", containerDir, title, year);

        logger.debug("Creating link for: {} in: {}", file.canonicalName, dirName);
        sftpHandler.createHardLinkAndFolder(file.canonicalName, dirName, new TaskCompletionListener() {
            @Override
            public void onTaskCompleted() {
                Toast.makeText(ItemListActivity.this, "link created", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onTaskFailed(@Nullable String message) {
                Toast.makeText(ItemListActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initViews() {
        isTwoPane = findViewById(R.id.item_detail_container) != null;
        toolbar = findViewById(R.id.toolbar);
        messagePanel = findViewById(R.id.message_panel);
        messagePanelLabel = findViewById(R.id.message_label);

        emptyPane = findViewById(R.id.empty_pane);
        progressPane = findViewById(R.id.progress_pane);
        recyclerView = findViewById(R.id.item_list);

        final Button messagePanelSignInButton = findViewById(R.id.relogin_button);

        setSupportActionBar(toolbar);
        messagePanelSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(LoginActivity.createIntent(ItemListActivity.this));
                finish();
            }
        });
    }

    private void loadValuesFromIntent(Intent intent) {
        host = intent.getStringExtra(LoginActivity.HOST_NAME_KEY);
        user = intent.getStringExtra(LoginActivity.USER_NAME_KEY);
        password = intent.getStringExtra(LoginActivity.PASSWORD_KEY);
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

    private void openDetails(RemoteDirectoryEntry item) {
        if (isTwoPane) {
            final DetailFragment fragment = DetailFragment.createInstance(item);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.item_detail_container, fragment, FRAGMENT_TAG)
                    .addToBackStack(FRAGMENT_TAG)
                    .commit();
        } else {
            startActivityForResult(DetailActivity.createIntent(item, this), RESULT_REQUEST_CODE);
        }
    }

    public static Intent createIntent(@NonNull String host, @NonNull String user, @NonNull String password, Context context) {
        final Intent intent = new Intent(context, ItemListActivity.class);
        intent.putExtra(LoginActivity.HOST_NAME_KEY, host);
        intent.putExtra(LoginActivity.USER_NAME_KEY, user);
        intent.putExtra(LoginActivity.PASSWORD_KEY, password);
        return intent;
    }

    private class ConnectionStateObserver implements Observer<ConnectionState> {
        @Override
        public void onChanged(ConnectionState connectionState) {
            switch (connectionState) {
                case Initialized:
                    Timber.d("Initialized");
                    connectionManager.connect(user, password);
                    break;
                case Authenticating:
                    Timber.d("Authenticating");
                    showProgressPane();
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
                        Timber.d(sftpPair.second.current.fileName);
                        Timber.d(sftpPair.second.current.canonicalName);
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
