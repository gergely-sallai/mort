package gergelysallai.mort.android.list;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import gergelysallai.mort.core.data.RemoteDirectoryEntry;

public class ItemKeyProvider extends androidx.recyclerview.selection.ItemKeyProvider<String> {

    private final DirectoryListingAdapter adapter;

    ItemKeyProvider(DirectoryListingAdapter adapter) {
        super(ItemKeyProvider.SCOPE_MAPPED);
        this.adapter = adapter;
    }

    @Nullable
    @Override
    public String getKey(int position) {
        return adapter.getData().get(position).canonicalName;
    }

    @Override
    public int getPosition(@NonNull String key) {
        final List<RemoteDirectoryEntry> entryList = adapter.getData();
        for (int i = 0; i < entryList.size(); i++) {
            if (key.equals(entryList.get(i).canonicalName)) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }
}
