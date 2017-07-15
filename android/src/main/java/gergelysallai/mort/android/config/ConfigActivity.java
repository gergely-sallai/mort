package gergelysallai.mort.android.config;

import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import gergelysallai.mort.android.R;
import gergelysallai.mort.android.list.ItemListActivity;

public class ConfigActivity extends AppCompatActivity {
    private static final String SHARED_PREF_NAME = "PreviousSessionData";
    public static final String HOST_NAME_KEY = "mort.android.HostNameKey";
    public static final String USER_NAME_KEY = "mort.android.UserNameKey";
    public static final String PASSWORD_KEY  = "mort.android.PasswordKey";

    private TextInputEditText hostView;
    private TextInputLayout hostViewHolder;
    private TextInputEditText userView;
    private TextInputLayout userViewholder;
    private TextInputEditText passwordView;
    private TextInputLayout passwordViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle(R.string.title_activity_config);

        hostViewHolder = (TextInputLayout) findViewById(R.id.host_layout);
        userViewholder = (TextInputLayout) findViewById(R.id.user_layout);
        passwordViewHolder = (TextInputLayout) findViewById(R.id.password_layout);
        hostView = (TextInputEditText) findViewById(R.id.host);
        userView = (TextInputEditText) findViewById(R.id.user);
        passwordView = (TextInputEditText) findViewById(R.id.password);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        loadValues();

        Button proceedButton = (Button) findViewById(R.id.email_sign_in_button);
        proceedButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    private void loadValues() {
        final SharedPreferences preferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        hostView.setText(preferences.getString(HOST_NAME_KEY, null));
        userView.setText(preferences.getString(USER_NAME_KEY, null));
    }

    private void saveValues(String host, String userName) {
        getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
                .edit()
                .putString(HOST_NAME_KEY, host)
                .putString(USER_NAME_KEY, userName)
                .apply();
    }

    private void attemptLogin() {
        final String errorString = getString(R.string.error_field_required);
        final String host = hostView.getText().toString();
        final String user = userView.getText().toString();
        final String password = passwordView.getText().toString();
        final boolean hostEmpty = TextUtils.isEmpty(host);
        final boolean userEmpty = TextUtils.isEmpty(user);
        final boolean passwordEmpty = TextUtils.isEmpty(password);
        if (hostEmpty) {
            hostViewHolder.setError(errorString);
        }
        if (userEmpty) {
            userViewholder.setError(errorString);
        }
        if (passwordEmpty) {
            passwordViewHolder.setError(errorString);
        }
        if (hostEmpty || userEmpty || passwordEmpty) {
            return;
        }
        saveValues(host, user);
        startActivity(ItemListActivity.createIntent(host, user, password, this));
    }

}
