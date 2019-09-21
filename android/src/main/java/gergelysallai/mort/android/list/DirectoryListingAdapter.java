package gergelysallai.mort.android.list;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.ViewGroup;

import androidx.recyclerview.selection.SelectionTracker;

import gergelysallai.mort.android.connection.SftpState;
import gergelysallai.mort.android.list.comparator.FileNameComparator;
import gergelysallai.mort.core.data.DirectoryListing;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


class DirectoryListingAdapter extends RecyclerView.Adapter<ViewHolderBase> implements DirectoryListingUpdateListener {

    private static final int TYPE_PARENT = 0;
    private static final int TYPE_DIRECTORY = 1;
    private static final int TYPE_FILE = 2;

    private final FileNameComparator fileNameComparator = new FileNameComparator();

    private final OnItemClickListener<RemoteDirectoryEntry> itemClickListener;

    @Nullable
    private SelectionTracker<String> tracker;

    private DirectoryListing directoryListing; // lateinit

    DirectoryListingAdapter(OnItemClickListener<RemoteDirectoryEntry> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolderBase onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_DIRECTORY:
                return new ViewHolderDirectory(parent, itemClickListener);
            case TYPE_FILE:
                return new ViewHolderFile(parent, itemClickListener);
            case TYPE_PARENT:
                return new ViewHolderDirectory(parent, itemClickListener);
            default:
                throw new IllegalStateException("Unsupported type!");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderBase holder, int position) {
        final boolean isActivated;
        if (tracker != null) {
            isActivated = tracker.isSelected(entriesOrEmptyList().get(position).canonicalName);
        } else {
            isActivated = false;
        }
        holder.bind(entriesOrEmptyList().get(position), isActivated);
    }

    private List<RemoteDirectoryEntry> entriesOrEmptyList() {
        if (directoryListing == null) {
            return Collections.emptyList();
        }
        return directoryListing.entries;
    }

    public List<RemoteDirectoryEntry> getData() {
        return entriesOrEmptyList();
    }

    @Override
    public int getItemCount() {
        return entriesOrEmptyList().size();
    }

    public void setTracker(@Nullable SelectionTracker<String> tracker) {
        this.tracker = tracker;
    }

    @Override
    public int getItemViewType(int position) {
        RemoteDirectoryEntry entry = entriesOrEmptyList().get(position);
        if (isParent(entry)) {
            return TYPE_PARENT;
        }
        if (entry.isDirectory) {
            return TYPE_DIRECTORY;
        }
        return TYPE_FILE;
    }

    private boolean isParent(RemoteDirectoryEntry entry) {
        return entry.fileName.equals("..");
    }

    private void updateDirectoryListing(DirectoryListing directoryListing) {
        if (tracker != null &&
            this.directoryListing != null &&
            !this.directoryListing.current.equals(directoryListing.current)
        ) {
            tracker.clearSelection();
        }
        final DirectoryListing listingUpdate = new DirectoryListing(
                directoryListing.current,
                new ArrayList<>(directoryListing.entries)
        );
        Collections.sort(listingUpdate.entries, fileNameComparator);
        this.directoryListing = listingUpdate;
        notifyDataSetChanged();
    }

    @Override
    public void onDirectoryListingUpdate(Pair<SftpState, DirectoryListing> update) {
        switch (update.first) {
            case Ok:
                if (update.second != null) {
                    updateDirectoryListing(update.second);
                }
                break;
            case Error:
                Timber.e("Error!");
                break;
            case Closed:
                Timber.w("Closed!");
                break;
        }
    }
}
