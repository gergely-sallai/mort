package gergelysallai.mort.core.ssh

import com.trilead.ssh2.Connection
import com.trilead.ssh2.SFTPv3Client
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.Executor


class SshConnectionHandler(host: String,
                           private val connectionListener: SshConnectionListener,
                           private val sshExecutor: Executor,
                           private val callbackExecutor: Executor) {

    private val logger = LoggerFactory.getLogger("SshConnectionHandler")

    private val sshConnection = Connection(host)

    fun connect(user: String, password: String) {
        sshExecutor.execute {
            try {
                sshConnection.connect() // FIXME Insecure, prone to MiM attack, use host name verifier!
                callbackExecutor.execute {
                    connectionListener.onConnectionStateChanged(ConnectionState.Authenticating)
                }
                val serverAuthMethods = getSupportedAuthMethods(sshConnection.getRemainingAuthMethods(user))

                if (serverAuthMethods.isEmpty()) {
                    callbackExecutor.execute {
                        connectionListener.onConnectionStateChanged(ConnectionState.AuthenticationError)
                    }
                    return@execute
                }

                val authSuccessful: Boolean
                when (serverAuthMethods.first()) {
                    SupportedAuthMethods.password -> {
                        authSuccessful = sshConnection.authenticateWithPassword(user, password)
                    }
                }
                callbackExecutor.execute {
                    if (authSuccessful) {
                        connectionListener.onConnectionStateChanged(ConnectionState.Connected)
                    } else {
                        connectionListener.onConnectionStateChanged(ConnectionState.AuthenticationError)
                    }
                }
            } catch (e: IOException) {
                logger.error("Error while connecting:", e)
                callbackExecutor.execute {
                    connectionListener.onConnectionStateChanged(ConnectionState.ConnectionError)
                }
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

    private fun getSupportedAuthMethods(serverAuthMethods: Array<String>): List<SupportedAuthMethods> {
        return serverAuthMethods.map {
            val method: SupportedAuthMethods? = SupportedAuthMethods.forName(it)
            if (method != null) {
                method
            } else {
                logger.info("Server has an auth type that this client does not support: {}", it)
                null
            }
        }.filterNotNull().sorted()
    }
}

private enum class SupportedAuthMethods(val authMethodName: kotlin.String) {
    password("password");
    companion object {
        fun forName(name: String): SupportedAuthMethods? {
            return values().find { it.authMethodName == name }
        }
    }
}

enum class ConnectionState {
    Initialized,
    Authenticating,
    Connected,
    AuthenticationError,
    Disconnected,
    ConnectionError
}

interface SshConnectionListener {
    fun onConnectionStateChanged(connectionState: ConnectionState)
    fun onSftpHandlerCreated(sftpHandler: SftpHandler)
}