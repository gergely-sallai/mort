package gergelysallai.mort.android.list;

import android.util.Pair;
import gergelysallai.mort.android.connection.SftpState;
import gergelysallai.mort.core.data.DirectoryListing;

public interface DirectoryListingUpdateListener {
    void onDirectoryListingUpdate(Pair<SftpState, DirectoryListing> update);
}
