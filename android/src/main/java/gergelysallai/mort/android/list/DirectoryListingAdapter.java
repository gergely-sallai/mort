package gergelysallai.mort.android.list;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.ViewGroup;
import gergelysallai.mort.android.connection.SftpState;
import gergelysallai.mort.core.data.DirectoryListing;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;
import timber.log.Timber;


class DirectoryListingAdapter extends RecyclerView.Adapter<ViewHolderBase> implements DirectoryListingUpdateListener {

    private static final int TYPE_PARENT = 0;
    private static final int TYPE_DIRECTORY = 1;
    private static final int TYPE_FILE = 2;

    private final OnItemClickListener<RemoteDirectoryEntry> itemClickListener;

    private DirectoryListing directoryListing;

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
        holder.bind(directoryListing.entries.get(position));
    }

    @Override
    public int getItemCount() {
        return directoryListing.entries.size();
    }

    @Override
    public int getItemViewType(int position) {
        RemoteDirectoryEntry entry = directoryListing.entries.get(position);
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
        this.directoryListing = directoryListing;
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
