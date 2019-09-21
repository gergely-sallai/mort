package gergelysallai.mort.android.list;

import android.support.annotation.NonNull;

import androidx.recyclerview.selection.SelectionTracker;

import java.util.List;

import gergelysallai.mort.core.data.RemoteDirectoryEntry;

public class FileSelectionPredicate extends SelectionTracker.SelectionPredicate<String> {

    private final DirectoryListingAdapter directoryListingAdapter;

    public FileSelectionPredicate(DirectoryListingAdapter directoryListingAdapter) {
        this.directoryListingAdapter = directoryListingAdapter;
    }

    @Override
    public boolean canSetStateForKey(@NonNull String key, boolean nextState) {
        final List<RemoteDirectoryEntry> entryList = directoryListingAdapter.getData();
        for (int i = 0; i < entryList.size(); i++) {
            RemoteDirectoryEntry entry = entryList.get(i);
            if (key.equals(entry.canonicalName)) {
                return !entry.isDirectory;
            }
        }
        return false;
    }

    @Override
    public boolean canSetStateAtPosition(int position, boolean nextState) {
        RemoteDirectoryEntry remoteDirectoryEntry = directoryListingAdapter.getData().get(position);
        return !remoteDirectoryEntry.isDirectory;
    }

    @Override
    public boolean canSelectMultiple() {
        return true;
    }
}
