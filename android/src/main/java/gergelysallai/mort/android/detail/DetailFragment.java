package gergelysallai.mort.android.detail;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ToggleButton;
import gergelysallai.mort.android.R;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;

public class DetailFragment extends Fragment {
    public static final String DIRECTORY_ENTRY_KEY = "mort.android.DirectoryEntryKey";

    public static DetailFragment createInstance(RemoteDirectoryEntry directoryEntry) {
        final DetailFragment fragment = new DetailFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(DIRECTORY_ENTRY_KEY, directoryEntry);
        fragment.setArguments(bundle);
        return fragment;
    }

    private RemoteDirectoryEntry directoryEntry;
    private FloatingActionButton fab;
    private TextInputLayout titleWrapper;
    private TextInputLayout yearWrapper;
    private TextInputLayout fileNameWrapper;
    private TextInputLayout fileLocationWrapper;
    private ToggleButton editToggle;
    private RadioGroup typeRadioGroup;
    private EditText fileNameView;
    private EditText fileLocationView;
    private EditText titleView;
    private EditText yearView;
    private CheckBox useParentInstead;
    private Button createLink;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        directoryEntry = (RemoteDirectoryEntry) getArguments().getSerializable(DIRECTORY_ENTRY_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_details, container, false);
        titleWrapper = (TextInputLayout) view.findViewById(R.id.title_wrapper);
        titleView = (EditText) view.findViewById(R.id.title_view);
        yearWrapper = (TextInputLayout) view.findViewById(R.id.year_wrapper);
        yearView = (EditText) view.findViewById(R.id.year_view);
        editToggle = (ToggleButton) view.findViewById(R.id.override_toggle);
        typeRadioGroup = (RadioGroup) view.findViewById(R.id.type_radio_group);
        fileNameWrapper = (TextInputLayout) view.findViewById(R.id.file_name_wrapper);
        fileNameView = (EditText) view.findViewById(R.id.file_name_view);
        fileLocationWrapper = (TextInputLayout) view.findViewById(R.id.file_location_wrapper);
        fileLocationView = (EditText) view.findViewById(R.id.file_location_view);
        useParentInstead = (CheckBox) view.findViewById(R.id.use_parent_instead_checkbox);
        createLink = (Button) view.findViewById(R.id.create_link);
        fab = (FloatingActionButton) view.findViewById(R.id.floatingActionButton);

        titleView.setText(directoryEntry.fileName);
        fileNameView.setText(directoryEntry.fileName);
        fileLocationView.setText(directoryEntry.parentDir);

        return view;
    }

}
