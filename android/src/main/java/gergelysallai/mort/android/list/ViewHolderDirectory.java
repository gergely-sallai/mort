package gergelysallai.mort.android.list;

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
        fileName = (TextView) itemView.findViewById(R.id.filename);
        details = (TextView) itemView.findViewById(R.id.details);
        imageView = (ImageView) itemView.findViewById(R.id.image);
    }

    @Override
    void bind(RemoteDirectoryEntry data) {
        super.bind(data);
        fileName.setText(data.fileName);
        imageView.setImageDrawable(folderDrawable);
        details.setText("Size: " + data.fileSize);
    }
}
