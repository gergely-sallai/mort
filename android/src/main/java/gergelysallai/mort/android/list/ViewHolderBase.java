package gergelysallai.mort.android.list;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.selection.ItemDetailsLookup;

import gergelysallai.mort.android.R;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;

import static android.support.v4.content.res.ResourcesCompat.getColor;


abstract class ViewHolderBase extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private final View overFlow;
    private final ClipboardManager clipboardManager;
    @Nullable
    private final OnItemClickListener<RemoteDirectoryEntry> clickListener;

    private RemoteDirectoryEntry data;

    ViewHolderBase(ViewGroup parent, @Nullable OnItemClickListener<RemoteDirectoryEntry> clickListener) {
        super(inflateView(R.layout.item_list_content, parent));
        clipboardManager = (ClipboardManager) itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        this.clickListener = clickListener;
        if (clickListener != null) {
            itemView.setOnClickListener(this);
        }
        overFlow = itemView.findViewById(R.id.overflow);
        overFlow.setOnClickListener(this);
    }

    void bind(RemoteDirectoryEntry data, boolean isActivated) {
        this.data = data;
        itemView.setActivated(isActivated);
    }

    @Override
    public void onClick(View v) {
        if (v == itemView) {
            if (clickListener != null) {
                clickListener.onItemClicked(data);
            }
        } else if (v == overFlow) {
            PopupMenu popup = new PopupMenu(itemView.getContext(), overFlow);
            MenuInflater inflater = popup.getMenuInflater();
            popup.setOnMenuItemClickListener(this);
            inflater.inflate(R.menu.list_overflow, popup.getMenu());
            popup.show();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.copy_file_name:
                copyTextToClipboard(data.fileName);
                return true;
            case R.id.copy_file_path:
                copyTextToClipboard(data.canonicalName);
                return true;
        }
        return false;
    }

    public ItemDetailsLookup.ItemDetails<String> getItemDetails() {
        if (data == null) {
            return null;
        }
        return new DirectoryDetailsEntry(getAdapterPosition(), data.canonicalName);
    }

    private void copyTextToClipboard(String text) {
        ClipData clip = ClipData.newPlainText("MoRT Copied path", text);
        clipboardManager.setPrimaryClip(clip);
    }

    protected Drawable loadDrawable(@DrawableRes int drawableRes) {
        final Resources.Theme theme = itemView.getContext().getTheme();
        final Drawable drawable = itemView.getResources().getDrawable(drawableRes, theme);
        drawable.setTint(getColor(itemView.getResources(), R.color.colorPrimary, theme));
        return drawable;
    }

    protected String toHumanFileSize(@Nullable Long fileSize) {
        final String humanFileSize;
        if (fileSize != null) {
            humanFileSize = Formatter.formatShortFileSize(itemView.getContext(), fileSize);
        } else {
            humanFileSize = itemView.getResources().getString(R.string.file_size_unknown);
        }
        return humanFileSize;
    }

    private static View inflateView(@LayoutRes int layoutResId, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return inflater.inflate(layoutResId, parent, false);
    }

    private static class DirectoryDetailsEntry extends ItemDetailsLookup.ItemDetails<String> {

        private final int position;
        private final String key;

        public DirectoryDetailsEntry(int position, String key) {
            this.position = position;
            this.key = key;
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public String getSelectionKey() {
            return key;
        }
    }
}
