package gergelysallai.mort.core.ssh;

import com.trilead.ssh2.SFTPv3Client;

import java.util.concurrent.Executor;

public class SftpHandler {

    private final Executor sshExecutor;
    private final Executor callbackExecutor;
    private final SFTPv3Client sftpClient;

    SftpHandler(SFTPv3Client sftpClient, Executor sshExecutor, Executor callbackExecutor) {
        this.sshExecutor = sshExecutor;
        this.callbackExecutor = callbackExecutor;
        this.sftpClient = sftpClient;
    }

    public void ls(final String path) {
        sshExecutor.execute(new Runnable() {
            @Override
            public void run() {
                sftpClient.
            }
        });
    }
}
