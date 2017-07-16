package gergelysallai.mort.core.ssh

import com.trilead.ssh2.SFTPv3Client
import com.trilead.ssh2.SFTPv3DirectoryEntry
import gergelysallai.mort.core.data.DirectoryListing
import gergelysallai.mort.core.data.RemoteDirectoryEntry
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.Executor


class SftpHandler(private val sftpClient: SFTPv3Client,
                  private val directoryListingListener: DirectoryListingListener,
                  private val sshExecutor: Executor,
                  private val callbackExecutor: Executor) {
    private val logger = LoggerFactory.getLogger("SftpHandler")

    private fun stats(path: String, fileName: String): RemoteDirectoryEntry {
        val relativePath = concatPaths(path, fileName)
        val stat = sftpClient.stat(relativePath)
        return RemoteDirectoryEntry(fileName, path, sftpClient.canonicalPath(relativePath), stat.isDirectory, stat.isRegularFile, stat.isSymlink, stat.size)
    }

    private fun concatPaths(path1: String, path2: String): String {
        if (path1.isNullOrEmpty()) {
            return path2
        }
        return path1 + "/" + path2
    }

    fun lsHome() {
        sshExecutor.execute {
            try {
                val fileName = "."
                val path = ""
                val current = stats(path, fileName)
                val ls: Vector<SFTPv3DirectoryEntry> = sftpClient.ls(fileName) as Vector<SFTPv3DirectoryEntry> // Lib is shitty had to force it :(
                val entries = ls.map {
                    val relPath = concatPaths(path, it.filename)
                    logger.info("Getting canonical name for: {}", relPath)
                    val canonicalPath = sftpClient.canonicalPath(relPath)
                    logger.info("Success for: {} :: {}", relPath, canonicalPath)
                    RemoteDirectoryEntry.fromSFTPv3DirectoryEntry(it, path, canonicalPath)
                }
                val directoryListing = DirectoryListing(current, entries)
                callbackExecutor.execute {
                    directoryListingListener.onDirectoryList(directoryListing)
                }
            } catch (e: IOException) {
                logger.error("Error while listing home: ", e)
                callbackExecutor.execute {
                    directoryListingListener.onDirectoryListError()
                }
            }
        }
    }

    fun ls(directory: RemoteDirectoryEntry) {
        sshExecutor.execute {
            try {
//                val ls: Vector<SFTPv3DirectoryEntry> = sftpClient.ls(directory.fileName) as Vector<SFTPv3DirectoryEntry> // Lib is shitty had to force it :(
//                val entries = ls.map { RemoteDirectoryEntry.fromSFTPv3DirectoryEntry(it, "e") }

                val directoryPath = concatPaths(directory.parentDir, directory.fileName)
                val ls: Vector<SFTPv3DirectoryEntry> = sftpClient.ls(directoryPath) as Vector<SFTPv3DirectoryEntry> // Lib is shitty had to force it :(
                val entries = ls.map {
                    val relPath = concatPaths(directoryPath, it.filename)
                    RemoteDirectoryEntry.fromSFTPv3DirectoryEntry(it, directoryPath, sftpClient.canonicalPath(relPath))
                }
                val directoryListing = DirectoryListing(directory, entries)
                callbackExecutor.execute {
                    directoryListingListener.onDirectoryList(directoryListing)
                }
            } catch (e: IOException) {
                logger.error("Error while listing {}: ", concatPaths(directory.fileName, directory.parentDir), e)
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