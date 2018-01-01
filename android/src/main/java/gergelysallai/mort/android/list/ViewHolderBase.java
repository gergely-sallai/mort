package gergelysallai.mort.android.list;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import gergelysallai.mort.android.R;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;

import static android.support.v4.content.res.ResourcesCompat.getColor;


abstract class ViewHolderBase extends RecyclerView.ViewHolder implements View.OnClickListener {
    @Nullable
    private final OnItemClickListener<RemoteDirectoryEntry> clickListener;

    private RemoteDirectoryEntry data;

    ViewHolderBase(ViewGroup parent, @Nullable OnItemClickListener<RemoteDirectoryEntry> clickListener) {
        super(inflateView(R.layout.item_list_content, parent));

        this.clickListener = clickListener;
        if (clickListener != null) {
            itemView.setOnClickListener(this);
        }
    }

    void bind(RemoteDirectoryEntry data) {
        this.data = data;
    }

    @Override
    public void onClick(View v) {
        if (clickListener != null) {
            clickListener.onItemClicked(data);
        }
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
}
