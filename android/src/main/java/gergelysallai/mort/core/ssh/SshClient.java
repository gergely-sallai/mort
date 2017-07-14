package gergelysallai.mort.core.ssh;

import com.trilead.ssh2.Connection;
import timber.log.Timber;

import java.io.IOException;
import java.util.concurrent.Executor;

import static gergelysallai.mort.core.util.Verify.verifyNotNull;

public class SshClient {

    private final Connection sshConnection;
    private final Executor sshExecutor;
    private final Executor callbackExecutor;
    private final ConnectionListener connectionListener;

    public SshClient(String host, ConnectionListener connectionListener, Executor sshExecutor, Executor callbackExecutor) {
        this.connectionListener = verifyNotNull(connectionListener);
        this.sshExecutor = verifyNotNull(sshExecutor);
        this.callbackExecutor = verifyNotNull(callbackExecutor);
        sshConnection = new Connection(host);
    }

    public void connect(final String user, final String password) {
        sshExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    sshConnection.connect(); // FIXME Insecure, prone to MiM attack, use host name verifier!
                    final boolean authSuccessful = sshConnection.authenticateWithPassword(user, password);
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (authSuccessful) {
                                connectionListener.onConnectionStateChanged(ConnectionState.Connected);
                            } else {
                                connectionListener.onConnectionStateChanged(ConnectionState.AuthenticationError);
                            }
                        }
                    });
                } catch (IOException e) {
                    Timber.e(e);
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            connectionListener.onConnectionStateChanged(ConnectionState.ConnectionError);
                        }
                    });
                } finally {
                    // TODO remove this!
                    sshConnection.close();
                }
            }
        });
    }

    public void disconnect() {
        sshExecutor.execute(new Runnable() {
            @Override
            public void run() {
                sshConnection.close();
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        connectionListener.onConnectionStateChanged(ConnectionState.Disconnected);
                    }
                });
            }
        });
    }



}
