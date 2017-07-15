package gergelysallai.mort.android.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import gergelysallai.mort.core.data.DirectoryListing;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;
import gergelysallai.mort.core.ssh.DirectoryListingListener;


class DirectoryListingAdapter extends RecyclerView.Adapter<ViewHolderBase> implements DirectoryListingListener {

    private static final int TYPE_PARENT = 0;
    private static final int TYPE_DIRECTORY = 1;
    private static final int TYPE_FILE = 2;

    private final OnItemClickListener<RemoteDirectoryEntry> itemClickListener;

    private DirectoryListing directoryListing;
    private boolean hasParentItem;
    private int itemCount;

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
        if (isParent(position)) {
            holder.bind(directoryListing.parent);
        } else {
            holder.bind(directoryListing.entries.get(position - parentOffset(hasParentItem)));
        }
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (isParent(position)) {
            return TYPE_PARENT;
        }
        RemoteDirectoryEntry entry = directoryListing.entries.get(position - parentOffset(hasParentItem));
        if (entry.isDirectory) {
            return TYPE_DIRECTORY;
        }
        return TYPE_FILE;
    }

    private boolean isParent(int position) {
        return position == 0 && hasParentItem;
    }

    private int parentOffset(boolean hasParentItem) {
        return (hasParentItem ? 1 : 0);
    }

    @Override
    public void onDirectoryList(@NonNull DirectoryListing directoryListing) {
        this.directoryListing = directoryListing;
        this.hasParentItem = directoryListing.parent != null;
        this.itemCount = directoryListing.entries.size() + parentOffset(hasParentItem);
        notifyDataSetChanged();
    }

    @Override
    public void onDirectoryListError() {

    }

    @Override
    public void onClosed() {

    }
}
