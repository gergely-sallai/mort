package gergelysallai.mort.core.data

import com.trilead.ssh2.SFTPv3DirectoryEntry


data class RemoteDirectoryEntry(
        @JvmField val fileName: String,
        @JvmField val isDirectory: Boolean,
        @JvmField val isRegularFile: Boolean,
        @JvmField val isSymLink: Boolean,
        @JvmField val fileSize: Long?) {

    companion object {
        fun fromSFTPv3DirectoryEntry(sftPv3DirectoryEntry: SFTPv3DirectoryEntry): RemoteDirectoryEntry{
            return RemoteDirectoryEntry(
                    sftPv3DirectoryEntry.filename,
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
