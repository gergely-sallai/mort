package gergelysallai.mort.core.ssh

import com.trilead.ssh2.SFTPv3Client
import com.trilead.ssh2.SFTPv3DirectoryEntry
import gergelysallai.mort.core.data.DirectoryListing
import gergelysallai.mort.core.data.RemoteDirectoryEntry
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.Executor

private const val PARENT_FOLDER = ".."
private const val THIS_FOLDER = "."
private const val EMPTY_PATH = ""
private const val PATH_SEPARATOR = "/"

class SftpHandler(private val sftpClient: SFTPv3Client,
                  private val directoryListingListener: DirectoryListingListener,
                  private val sshExecutor: Executor,
                  private val callbackExecutor: Executor) {
    private val logger = LoggerFactory.getLogger("SftpHandler")

    private lateinit var homeFolderAbsPath: String

    private fun stats(path: String, fileName: String): RemoteDirectoryEntry {
        val relativePath = concatPaths(path, fileName)
        val stat = sftpClient.stat(relativePath)
        return RemoteDirectoryEntry(fileName, path, sftpClient.canonicalPath(relativePath), stat.isDirectory, stat.isRegularFile, stat.isSymlink, stat.size)
    }

    private fun concatPaths(path1: String, path2: String): String {
        if (path1.isNullOrEmpty()) {
            return path2
        }
        return path1 + PATH_SEPARATOR + path2
    }

    private fun lsSynchronous(directory: RemoteDirectoryEntry): DirectoryListing {
        val directoryPath = concatPaths(directory.parentDir, directory.fileName)
        val simplifiedPath = makeRelative(sftpClient.canonicalPath(directoryPath))
        logger.debug("Simplified: {} to: {}", directoryPath, simplifiedPath)
        val ls: Vector<SFTPv3DirectoryEntry> = sftpClient.ls(simplifiedPath) as Vector<SFTPv3DirectoryEntry> // Lib is shitty had to force it :(
        val entries = ls.map {
            val relPath = concatPaths(simplifiedPath, it.filename)
            logger.debug("Getting canonical name for: {}", relPath)
            val canonicalPath = sftpClient.canonicalPath(relPath)
            logger.debug("Success for: {} :: {}", relPath, canonicalPath)
            RemoteDirectoryEntry.fromSFTPv3DirectoryEntry(it, simplifiedPath, canonicalPath)
        }
        return DirectoryListing(directory, entries)
    }

    fun lsHome() {
        sshExecutor.execute {
            try {
                val current = stats(EMPTY_PATH, THIS_FOLDER)
                homeFolderAbsPath = current.canonicalName
                val directoryListing = lsSynchronous(current)
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
                val directoryListing = lsSynchronous(directory)
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

    private fun makeRelative(canonicalPath: String): String {
        if (canonicalPath == homeFolderAbsPath) {
            return THIS_FOLDER
        } else if (canonicalPath.startsWith(homeFolderAbsPath)) {
            return canonicalPath.replaceFirst(homeFolderAbsPath + PATH_SEPARATOR, EMPTY_PATH)
        } else {
            val segmentNum = homeFolderAbsPath.split("/").dropWhile { it.isEmpty() }.size
            val relativeUp = Array(segmentNum, { PARENT_FOLDER }).joinToString(separator = PATH_SEPARATOR)
            if (canonicalPath == PATH_SEPARATOR) {
                return relativeUp
            }
            return relativeUp + canonicalPath
        }
    }
}

interface DirectoryListingListener {
    fun onDirectoryList(directoryListing: DirectoryListing)
    fun onDirectoryListError()
    fun onClosed()
}