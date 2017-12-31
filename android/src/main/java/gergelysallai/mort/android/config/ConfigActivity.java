package gergelysallai.mort.android.config;

import android.content.Context;
import android.content.Intent;
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
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle(R.string.title_activity_config);

        hostViewHolder = findViewById(R.id.host_layout);
        userViewholder = findViewById(R.id.user_layout);
        passwordViewHolder = findViewById(R.id.password_layout);
        hostView = findViewById(R.id.host);
        userView = findViewById(R.id.user);
        passwordView = findViewById(R.id.password);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_GO || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        loadValues();

        Button proceedButton = findViewById(R.id.sign_in_button);
        proceedButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    private void loadValues() {
        final SharedPreferences preferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        final String hostName = preferences.getString(HOST_NAME_KEY, null);
        final String userName = preferences.getString(USER_NAME_KEY, null);
        if (!TextUtils.isEmpty(hostName)) {
            hostView.setText(hostName);
            userView.requestFocus();
            if (!TextUtils.isEmpty(userName)) {
                userView.setText(userName);
                passwordView.requestFocus();
            }
        }
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
        finish();
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, ConfigActivity.class);
    }
}

