package gergelysallai.mort.core.ssh

import com.trilead.ssh2.SFTPv3Client
import com.trilead.ssh2.SFTPv3DirectoryEntry
import gergelysallai.mort.core.data.DirectoryListing
import gergelysallai.mort.core.data.RemoteDirectoryEntry
import java.io.IOException
import java.util.*
import java.util.concurrent.Executor


class SftpHandler(private val sftpClient: SFTPv3Client,
                  private val directoryListingListener: DirectoryListingListener,
                  private val sshExecutor: Executor,
                  private val callbackExecutor: Executor) {

    fun ls(directory: RemoteDirectoryEntry) {
        sshExecutor.execute {
            try {
                val ls: Vector<SFTPv3DirectoryEntry> = sftpClient.ls(directory.fileName) as Vector<SFTPv3DirectoryEntry> // Lib is shitty had to force it :(
                val entries = ls.map { RemoteDirectoryEntry.fromSFTPv3DirectoryEntry(it) }
                val directoryListing = DirectoryListing(directory, entries)
                callbackExecutor.execute {
                    directoryListingListener.onDirectoryList(directoryListing)
                }
            } catch (e: IOException) {
                callbackExecutor.execute {
                    directoryListingListener.onDirectoryListError()
                }
            }
        }
    }

    /**
     * Always call this, even if DirectoryListingError has happened!
     */
    fun close() {
        sshExecutor.execute {
            sftpClient.close()
            callbackExecutor.execute {
                directoryListingListener.onClosed()
            }
        }
    }
}

interface DirectoryListingListener {
    fun onDirectoryList(directoryListing: DirectoryListing)
    fun onDirectoryListError()
    fun onClosed()
}