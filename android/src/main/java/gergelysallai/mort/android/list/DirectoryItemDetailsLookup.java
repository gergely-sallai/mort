package gergelysallai.mort.android.list;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.selection.ItemDetailsLookup;

public final class DirectoryItemDetailsLookup extends ItemDetailsLookup<String> {
    private final RecyclerView recyclerView;

    public DirectoryItemDetailsLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<String> getItemDetails(@NonNull MotionEvent event) {
        final View view = recyclerView.findChildViewUnder(event.getX(), event.getY());
        if (view != null) {
            return ((ViewHolderBase) recyclerView.getChildViewHolder(view)).getItemDetails();
        }
        return null;
    }
}