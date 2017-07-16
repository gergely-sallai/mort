package gergelysallai.mort.core.data

import com.trilead.ssh2.SFTPv3DirectoryEntry
import java.io.Serializable


data class RemoteDirectoryEntry (
        @JvmField val fileName: String,
        @JvmField val parentDir: String,
        @JvmField val canonicalName: String,
        @JvmField val isDirectory: Boolean,
        @JvmField val isRegularFile: Boolean,
        @JvmField val isSymLink: Boolean,
        @JvmField val fileSize: Long?): Serializable {

    companion object {
        fun fromSFTPv3DirectoryEntry(sftPv3DirectoryEntry: SFTPv3DirectoryEntry, parentDir: String, canonicalName: String): RemoteDirectoryEntry{
            return RemoteDirectoryEntry(
                    sftPv3DirectoryEntry.filename,
                    parentDir,
                    canonicalName,
                    sftPv3DirectoryEntry.attributes.isDirectory,
                    sftPv3DirectoryEntry.attributes.isRegularFile,
                    sftPv3DirectoryEntry.attributes.isSymlink,
                    sftPv3DirectoryEntry.attributes.size)
        }
    }
}

data class DirectoryListing(
        @JvmField val current: RemoteDirectoryEntry,
        @JvmField val entries: List<RemoteDirectoryEntry> = listOf()
)
