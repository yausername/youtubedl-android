package com.yausername.youtubedl_android_example;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.security.GeneralSecurityException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Signs in against the configured app backend and stores its user token in Android Keystore-backed storage. */
public class LoginActivity extends AppCompatActivity {
    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private ProgressBar progressBar;
    private TextView statusView;
    @Nullable private Call<LoginResponse> loginCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.login_username);
        passwordInput = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.login_progress);
        statusView = findViewById(R.id.login_status);
        loginButton.setOnClickListener(view -> login());
    }

    private void login() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (TextUtils.isEmpty(username)) {
            usernameInput.setError(getString(R.string.login_username_required));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError(getString(R.string.login_password_required));
            return;
        }

        setLoading(true);
        statusView.setText(R.string.login_requesting);
        loginCall = PlaybackApiClient.create(getApplicationContext())
                .login(new LoginRequest(username, password));
        loginCall.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);
                LoginResponse body = response.body();
                if (!response.isSuccessful() || body == null || body.code != 0
                        || body.data == null || TextUtils.isEmpty(body.data.token)) {
                    statusView.setText(R.string.login_rejected);
                    return;
                }
                try {
                    new SessionManager(LoginActivity.this).saveAccessToken(body.data.token);
                    setResult(RESULT_OK);
                    finish();
                } catch (GeneralSecurityException error) {
                    statusView.setText(R.string.login_storage_error);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable error) {
                if (isFinishing() || isDestroyed() || call.isCanceled()) return;
                setLoading(false);
                statusView.setText(R.string.login_network_error);
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        if (loginCall != null) loginCall.cancel();
        super.onDestroy();
    }
}
