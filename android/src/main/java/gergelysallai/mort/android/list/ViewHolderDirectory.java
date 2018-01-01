package gergelysallai.mort.android.list;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import gergelysallai.mort.android.R;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;


class ViewHolderDirectory extends ViewHolderBase {
    private final Drawable folderDrawable;
    private final TextView fileName;
    private final TextView details;
    private final ImageView imageView;

    ViewHolderDirectory(ViewGroup parent, OnItemClickListener<RemoteDirectoryEntry> clickListener) {
        super(parent, clickListener);
        folderDrawable = loadDrawable(R.drawable.ic_folder_black_48dp);
        fileName = itemView.findViewById(R.id.filename);
        details = itemView.findViewById(R.id.details);
        imageView = itemView.findViewById(R.id.image);
    }

    @Override
    void bind(RemoteDirectoryEntry data) {
        super.bind(data);
        final Resources resources = itemView.getResources();
        fileName.setText(data.fileName);
        imageView.setImageDrawable(folderDrawable);
        details.setText(resources.getString(R.string.list_item_file_size, toHumanFileSize(data.fileSize)));
    }
}
