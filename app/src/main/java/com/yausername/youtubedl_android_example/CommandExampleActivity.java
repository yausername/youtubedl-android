package com.yausername.youtubedl_android_example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;


public class CommandExampleActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnRunCommand;
    private Button btnStopDownload;
    private EditText etCommand;
    private ProgressBar progressBar;
    private TextView tvCommandStatus;
    private TextView tvCommandOutput;
    private ProgressBar pbLoading;

    private boolean running = false;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final String processId = "MyMainDownload";

    private final Function3<Float, Long, String, Unit> callback = new Function3<Float, Long, String, Unit>() {
        @Override
        public Unit invoke(Float progress, Long o2, String line) {
            runOnUiThread(() -> {
                        progressBar.setProgress((int) progress.floatValue());
                        tvCommandStatus.setText(line);
                    }
            );
            return Unit.INSTANCE;
        }

    };

    private static final String TAG = CommandExampleActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_example);

        initViews();
        initListeners();
    }

    private void initViews() {
        btnRunCommand = findViewById(R.id.btn_run_command);
        btnStopDownload = findViewById(R.id.btn_stop_download);
        etCommand = findViewById(R.id.et_command);
        progressBar = findViewById(R.id.progress_bar);
        tvCommandStatus = findViewById(R.id.tv_status);
        pbLoading = findViewById(R.id.pb_status);
        tvCommandOutput = findViewById(R.id.tv_command_output);
    }

    private void initListeners() {
        btnRunCommand.setOnClickListener(this);
        btnStopDownload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_run_command:
                runCommand();
                break;
            case R.id.btn_stop_download:
                if (running) {
                    try {
                        YoutubeDL.getInstance().destroyProcessById(processId);
                        running = false;
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
                break;
        }
    }

    private void runCommand() {
        if (running) {
            Toast.makeText(CommandExampleActivity.this, "cannot start command. a command is already in progress", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isStoragePermissionGranted()) {
            Toast.makeText(CommandExampleActivity.this, "grant storage permission and retry", Toast.LENGTH_LONG).show();
            return;
        }

        String command = etCommand.getText().toString().trim();
        if (TextUtils.isEmpty(command)) {
            etCommand.setError(getString(R.string.command_error));
            return;
        }

        // this is not the recommended way to add options/flags/url and might break in future
        // use the constructor for url, addOption(key) for flags, addOption(key, value) for options
        YoutubeDLRequest request = new YoutubeDLRequest(Collections.emptyList());
        String commandRegex = "\"([^\"]*)\"|(\\S+)";
        Matcher m = Pattern.compile(commandRegex).matcher(command);
        while (m.find()) {
            if (m.group(1) != null) {
                request.addOption(m.group(1));
            } else {
                request.addOption(m.group(2));
            }
        }

        showStart();

        running = true;
        Disposable disposable =
                Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, processId,
                                callback))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(youtubeDLResponse -> {
                            pbLoading.setVisibility(View.GONE);
                            progressBar.setProgress(100);
                            tvCommandStatus.setText(getString(R.string.command_complete));
                            tvCommandOutput.setText(youtubeDLResponse.getOut());
                            Toast.makeText(CommandExampleActivity.this, "command successful", Toast.LENGTH_LONG).show();
                            running = false;
                        }, e -> {
                            if (BuildConfig.DEBUG) Log.e(TAG, "command failed", e);
                            pbLoading.setVisibility(View.GONE);
                            tvCommandStatus.setText(getString(R.string.command_failed));
                            tvCommandOutput.setText(e.getMessage());
                            Toast.makeText(CommandExampleActivity.this, "command failed", Toast.LENGTH_LONG).show();
                            running = false;
                        });
        compositeDisposable.add(disposable);

    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void showStart() {
        tvCommandStatus.setText(getString(R.string.command_start));
        progressBar.setProgress(0);
        pbLoading.setVisibility(View.VISIBLE);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }
}