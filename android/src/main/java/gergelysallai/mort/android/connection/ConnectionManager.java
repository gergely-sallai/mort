package gergelysallai.mort.android.connection;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import gergelysallai.mort.core.data.DirectoryListing;
import gergelysallai.mort.core.ssh.ConnectionState;
import gergelysallai.mort.core.ssh.DirectoryListingListener;
import gergelysallai.mort.core.ssh.SftpHandler;
import gergelysallai.mort.core.ssh.SshConnectionHandler;
import gergelysallai.mort.core.ssh.SshConnectionListener;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static gergelysallai.mort.util.Verify.verifyNotNull;

public class ConnectionManager extends ViewModel implements SshConnectionListener, DirectoryListingListener {

    private final ExecutorService connectionExecutor = Executors.newSingleThreadExecutor();
    private final Executor mainThreadExecutor = new MainThreadExecutor();
    private final MutableLiveData<ConnectionState> connectionStateData = new MutableLiveData<>();
    private final MutableLiveData<Pair<SftpState, DirectoryListing>> sftpStateData = new MutableLiveData<>();

    @Nullable
    private SshConnectionHandler sshConnectionHandler;
    @Nullable
    private SftpHandler sftpHandler;

    public void init(@NonNull String host) {
        sshConnectionHandler = new SshConnectionHandler(host, this, connectionExecutor, mainThreadExecutor);
        connectionStateData.setValue(ConnectionState.Disconnected);
    }

    public void connect(@NonNull String user, @NonNull String password) {
        verifyNotNull(user, "Username can not be null");
        verifyNotNull(password, "Password can not be null");
        assert sshConnectionHandler != null;
        sshConnectionHandler.connect(user, password);
    }

    public void disconnect() {
        assert sshConnectionHandler != null;
        sshConnectionHandler.disconnect();
    }

    public void createSftp() {
        assert sshConnectionHandler != null;
        sshConnectionHandler.createSftpHandler(this);
    }

    @NonNull
    public SftpHandler getSftpHandler() {
        verifyNotNull(sftpHandler, "SftpHandler is not initialized");
        return sftpHandler;
    }

    @NonNull
    public LiveData<ConnectionState> getConnectionStateData() {
        return connectionStateData;
    }

    @NonNull
    public LiveData<Pair<SftpState, DirectoryListing>> getSftpStateData() {
        return sftpStateData;
    }

    @Override
    public void onConnectionStateChanged(@NonNull ConnectionState connectionState) {
        connectionStateData.setValue(connectionState);
    }

    @Override
    public void onSftpHandlerCreated(@NonNull SftpHandler sftpHandler) {
        this.sftpHandler = sftpHandler;
        sftpStateData.setValue(Pair.<SftpState, DirectoryListing>create(SftpState.Ok, null));
    }

    @Override
    public void onDirectoryList(@NonNull DirectoryListing directoryListing) {
        sftpStateData.setValue(Pair.create(SftpState.Ok, directoryListing));
    }

    @Override
    public void onDirectoryListError() {
        sftpStateData.setValue(Pair.<SftpState, DirectoryListing>create(SftpState.Error, null));
    }

    @Override
    public void onClosed() {
        sftpStateData.setValue(Pair.<SftpState, DirectoryListing>create(SftpState.Closed, null));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (sftpHandler != null) {
            sftpHandler.close();
        }
        if (sshConnectionHandler != null) {
            sshConnectionHandler.disconnect();
        }
        connectionExecutor.shutdown();
    }
}
