package gergelysallai.mort.core.ssh;

public interface ConnectionListener {

    void onConnectionStateChanged(ConnectionState connectionState);
}
