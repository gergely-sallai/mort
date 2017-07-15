package gergelysallai.mort.android.list.comparator;

import gergelysallai.mort.core.data.RemoteDirectoryEntry;

import java.util.Comparator;

public class FileNameComparator implements Comparator<RemoteDirectoryEntry> {

    @Override
    public int compare(RemoteDirectoryEntry o1, RemoteDirectoryEntry o2) {
        return o1.fileName.compareTo(o2.fileName);
    }
}
