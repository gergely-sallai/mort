package gergelysallai.mort.android.detail;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ToggleButton;
import gergelysallai.mort.android.R;
import gergelysallai.mort.core.data.RemoteDirectoryEntry;

import static gergelysallai.mort.util.MoreObjects.firstNonNull;

public class DetailFragment extends Fragment {
    public static final String DIRECTORY_ENTRY_KEY = "mort.android.DirectoryEntryKey";

    public static DetailFragment createInstance(RemoteDirectoryEntry directoryEntry) {
        final DetailFragment fragment = new DetailFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(DIRECTORY_ENTRY_KEY, directoryEntry);
        fragment.setArguments(bundle);
        return fragment;
    }

    private Detail.TitleUpdateListener titleUpdateListener = Detail.NoOpTitleUpdateListener;
    private Detail.ResultListener resultListener;
    private RemoteDirectoryEntry directoryEntry;

    private String originalTitle;
    private Integer originalYear;

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Detail.TitleUpdateListener) {
            titleUpdateListener = firstNonNull((Detail.TitleUpdateListener) activity, Detail.NoOpTitleUpdateListener);
        }
        resultListener = (Detail.ResultListener) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_details, container, false);
        titleWrapper = view.findViewById(R.id.title_wrapper);
        titleView = view.findViewById(R.id.title_view);
        yearWrapper = view.findViewById(R.id.year_wrapper);
        yearView = view.findViewById(R.id.year_view);
        editToggle = view.findViewById(R.id.override_toggle);
        typeRadioGroup = view.findViewById(R.id.type_radio_group);
        fileNameWrapper = view.findViewById(R.id.file_name_wrapper);
        fileNameView = view.findViewById(R.id.file_name_view);
        fileLocationWrapper = view.findViewById(R.id.file_location_wrapper);
        fileLocationView = view.findViewById(R.id.file_location_view);
        useParentInstead = view.findViewById(R.id.use_parent_instead_checkbox);
        createLink = view.findViewById(R.id.create_link);
        fab = view.findViewById(R.id.floatingActionButton);

        editToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    resetToOriginal();
                    clearErrors();
                }
                titleView.setEnabled(isChecked);
                yearView.setEnabled(isChecked);
                titleUpdateListener.onUpdateTitle(titleView.getText().toString(), yearView.getText().toString());
            }
        });
        createLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearErrors();
                verifyValuesThenPublish();
            }
        });

        final String fileName = directoryEntry.fileName;
        final String parentDir = directoryEntry.parentDir;
        originalTitle = fileName;

        fileNameView.setText(fileName);
        fileLocationView.setText(parentDir);
        titleUpdateListener.onUpdateTitle(fileName, parentDir);

        return view;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (!editToggle.isChecked()) {
            resetToOriginal();
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    private void clearErrors() {
        titleWrapper.setError(null);
        yearWrapper.setError(null);
        fileNameWrapper.setError(null);
        fileLocationWrapper.setError(null);
    }

    private void verifyValuesThenPublish() {
        final boolean isMovie;
        switch (typeRadioGroup.getCheckedRadioButtonId()) {
            case R.id.radio_movie:
                isMovie = true;
                break;
            case R.id.radio_tv_show:
                isMovie = false;
                break;
            default:
                throw new IllegalStateException("Unknown radio button checked..");
        }

        final String title = titleView.getText().toString();
        if (TextUtils.isEmpty(title)) {
            titleWrapper.setError(getString(R.string.details_error_title_empty));
            return;
        }
        final int year;
        try {
            year = Integer.parseInt(yearView.getText().toString());
        } catch (NumberFormatException e) {
            yearWrapper.setError(getString(R.string.details_error_number_required));
            return;
        }
        resultListener.onResult(directoryEntry, title, year, isMovie);
    }

    private void updateValues(String title, int year, boolean isMovie, boolean useParent) {
        titleView.setText(title);
        yearView.setText(Integer.toString(year));
        typeRadioGroup.check(isMovie ? R.id.radio_movie : R.id.radio_tv_show);
        useParentInstead.setChecked(useParent);
    }

    private void resetToOriginal() {
        titleView.setText(originalTitle);
        yearView.setText(originalYear == null ? "" : originalYear.toString());
    }

}
