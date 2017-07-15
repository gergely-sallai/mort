package gergelysallai.mort.android.list;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.ViewGroup;
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

    private List<RemoteDirectoryEntry> entryList = Collections.emptyList();

    DirectoryListingAdapter(OnItemClickListener<RemoteDirectoryEntry> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public ViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(ViewHolderBase holder, int position) {
        holder.bind(entryList.get(position));
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    @Override
    public int getItemViewType(int position) {
        RemoteDirectoryEntry entry = entryList.get(position);
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
        entryList = new ArrayList<>();
        entryList.addAll(directoryListing.entries);
        Collections.sort(entryList, fileNameComparator);
        notifyDataSetChanged();
    }

    @Override
    public void onDirectoryListingUpdate(Pair<SftpState, DirectoryListing> update) {
        switch (update.first) {
            case Ok:
                Timber.d("Updated: %s", update.second);
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
