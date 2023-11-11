package com.yausername.youtubedl_android_example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;


public class DownloadingExampleActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStartDownload;
    private Button btnStopDownload;
    private EditText etUrl;
    private Switch useConfigFile;
    private ProgressBar progressBar;
    private TextView tvDownloadStatus;
    private TextView tvCommandOutput;
    private ProgressBar pbLoading;

    private boolean downloading = false;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String processId = "MyDlProcess";


    private final Function3<Float, Long, String, Unit> callback = new Function3<Float, Long, String, Unit>() {
        @Override
        public Unit invoke(Float progress, Long o2, String line) {
            runOnUiThread(() -> {
                        progressBar.setProgress((int) progress.floatValue());
                        tvDownloadStatus.setText(line);
                    }
            );
            return Unit.INSTANCE;
        }
    };

    private static final String TAG = DownloadingExampleActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloading_example);

        initViews();
        initListeners();
    }

    private void initViews() {
        btnStartDownload = findViewById(R.id.btn_start_download);
        btnStopDownload = findViewById(R.id.btn_stop_download);
        etUrl = findViewById(R.id.et_url);
        useConfigFile = findViewById(R.id.use_config_file);
        progressBar = findViewById(R.id.progress_bar);
        tvDownloadStatus = findViewById(R.id.tv_status);
        pbLoading = findViewById(R.id.pb_status);
        tvCommandOutput = findViewById(R.id.tv_command_output);
    }

    private void initListeners() {
        btnStartDownload.setOnClickListener(this);
        btnStopDownload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_download:
                startDownload();
                break;
            case R.id.btn_stop_download:
                try {
                    YoutubeDL.getInstance().destroyProcessById(processId);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                break;
        }
    }

    private void startDownload() {
        if (downloading) {
            Toast.makeText(DownloadingExampleActivity.this, "cannot start download. a download is already in progress", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isStoragePermissionGranted()) {
            Toast.makeText(DownloadingExampleActivity.this, "grant storage permission and retry", Toast.LENGTH_LONG).show();
            return;
        }

        String url = etUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            etUrl.setError(getString(R.string.url_error));
            return;
        }

        YoutubeDLRequest request = getYoutubeDLRequest(url);

        showStart();

        downloading = true;
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, processId, callback))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(youtubeDLResponse -> {
                    pbLoading.setVisibility(View.GONE);
                    progressBar.setProgress(100);
                    tvDownloadStatus.setText(getString(R.string.download_complete));
                    tvCommandOutput.setText(youtubeDLResponse.getOut());
                    Toast.makeText(DownloadingExampleActivity.this, "download successful", Toast.LENGTH_LONG).show();
                    downloading = false;
                }, e -> {
                    if (BuildConfig.DEBUG) Log.e(TAG, "failed to download", e);
                    pbLoading.setVisibility(View.GONE);
                    tvDownloadStatus.setText(getString(R.string.download_failed));
                    tvCommandOutput.setText(e.getMessage());
                    Toast.makeText(DownloadingExampleActivity.this, "download failed", Toast.LENGTH_LONG).show();
                    downloading = false;
                });
        compositeDisposable.add(disposable);

    }

    private YoutubeDLRequest getYoutubeDLRequest(String url) {
        YoutubeDLRequest request = new YoutubeDLRequest(url);
        File youtubeDLDir = getDownloadLocation();
        File config = new File(youtubeDLDir, "config.txt");

        if (useConfigFile.isChecked() && config.exists()) {
            request.addOption("--config-location", config.getAbsolutePath());
        } else {
            request.addOption("--no-mtime");
            request.addOption("--downloader", "libaria2c.so");
            request.addOption("--external-downloader-args", "aria2c:\"--summary-interval=1\"");
            request.addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best");
            request.addOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s.%(ext)s");
        }
        return request;
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @NonNull
    private File getDownloadLocation() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File youtubeDLDir = new File(downloadsDir, "youtubedl-android");
        if (!youtubeDLDir.exists()) youtubeDLDir.mkdir();
        return youtubeDLDir;
    }

    private void showStart() {
        tvDownloadStatus.setText(getString(R.string.download_start));
        progressBar.setProgress(0);
        pbLoading.setVisibility(View.VISIBLE);
    }

    public static final int STORAGE_PERMISSION_REQUEST_CODE = 1;

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                requestStoragePermission();
                return false;
            }
        } else {
            return true;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "Request code:" + requestCode);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(DownloadingExampleActivity.this, "permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(DownloadingExampleActivity.this, "permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}