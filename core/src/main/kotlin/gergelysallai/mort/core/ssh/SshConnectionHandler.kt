package gergelysallai.mort.core.ssh

import com.trilead.ssh2.Connection
import com.trilead.ssh2.SFTPv3Client
import java.io.IOException
import java.util.concurrent.Executor


class SshConnectionHandler(host: String,
                           private val connectionListener: SshConnectionListener,
                           private val sshExecutor: Executor,
                           private val callbackExecutor: Executor) {
    private val sshConnection = Connection(host)

    fun connect(user: String, password: String) {
        sshExecutor.execute {
            try {
                sshConnection.connect() // FIXME Insecure, prone to MiM attack, use host name verifier!
                val authSuccessful = sshConnection.authenticateWithPassword(user, password)
                callbackExecutor.execute {
                    if (authSuccessful) {
                        connectionListener.onConnectionStateChanged(ConnectionState.Connected)
                    } else {
                        connectionListener.onConnectionStateChanged(ConnectionState.AuthenticationError)
                    }
                }
            } catch (e: IOException) {
                connectionListener.onConnectionStateChanged(ConnectionState.ConnectionError)
            }

        }
    }

    fun disconnect() {
        sshExecutor.execute {
            sshConnection.close()
            callbackExecutor.execute { connectionListener.onConnectionStateChanged(ConnectionState.Disconnected) }
        }
    }

    fun createSftpHandler(directoryListingListener: DirectoryListingListener) {
        sshExecutor.execute {
            try {
                val sftpClient = SFTPv3Client(sshConnection)
                val sftpHandler = SftpHandler(sftpClient, directoryListingListener, sshExecutor, callbackExecutor)
                callbackExecutor.execute {
                    connectionListener.onSftpHandlerCreated(sftpHandler)
                }
            } catch (e: IOException) {
                callbackExecutor.execute {
                    connectionListener.onConnectionStateChanged(ConnectionState.ConnectionError)
                }
            }
        }
    }
}

enum class ConnectionState {
    Initialized,
    Connected,
    AuthenticationError,
    Disconnected,
    ConnectionError
}

interface SshConnectionListener {
    fun onConnectionStateChanged(connectionState: ConnectionState)
    fun onSftpHandlerCreated(sftpHandler: SftpHandler)
}