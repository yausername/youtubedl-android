package com.yausername.youtubedl_android_example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yausername.youtubedl_android.YoutubeDL;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStreamingExample;
    private Button btnDownloadingExample;
    private Button btnCommandExample;
    private Button btnAuthorizedPlayback;
    private Button btnUpdate;
    private ProgressBar progressBar;

    private boolean updating = false;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initListeners();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void initListeners() {
        btnStreamingExample.setOnClickListener(this);
        btnDownloadingExample.setOnClickListener(this);
        btnCommandExample.setOnClickListener(this);
        btnAuthorizedPlayback.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
    }

    private void initViews() {
        btnStreamingExample = findViewById(R.id.btn_streaming_example);
        btnDownloadingExample = findViewById(R.id.btn_downloading_example);
        btnCommandExample = findViewById(R.id.btn_command_example);
        btnAuthorizedPlayback = findViewById(R.id.btn_authorized_playback);
        btnUpdate = findViewById(R.id.btn_update);
        progressBar = findViewById(R.id.progress_bar);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_streaming_example) {
            startActivity(new Intent(this, StreamingExampleActivity.class));
        } else if (id == R.id.btn_downloading_example) {
            startActivity(new Intent(this, DownloadingExampleActivity.class));
        } else if (id == R.id.btn_command_example) {
            startActivity(new Intent(this, CommandExampleActivity.class));
        } else if (id == R.id.btn_authorized_playback) {
            startActivity(new Intent(this, AuthorizedPlaybackActivity.class));
        } else if (id == R.id.btn_update) {
            AlertDialogHelper.showUpdateDialog(this, this::updateYoutubeDL);
        }
    }

    private void updateYoutubeDL(YoutubeDL.UpdateChannel updateChannel) {
        if (updating) {
            Toast.makeText(this, "Update is already in progress!", Toast.LENGTH_LONG).show();
            return;
        }

        updating = true;
        progressBar.setVisibility(View.VISIBLE);
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().updateYoutubeDL(this, updateChannel))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    progressBar.setVisibility(View.GONE);
                    switch (status) {
                        case DONE:
                            Toast.makeText(this, "Update successful " + YoutubeDL.getInstance().versionName(this), Toast.LENGTH_LONG).show();
                            break;
                        case ALREADY_UP_TO_DATE:
                            Toast.makeText(this, "Already up to date " + YoutubeDL.getInstance().versionName(this), Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(this, status.toString(), Toast.LENGTH_LONG).show();
                            break;
                    }
                    updating = false;
                }, e -> {
                    Log.e(TAG, "failed to update", e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "update failed", Toast.LENGTH_LONG).show();
                    updating = false;
                });
        compositeDisposable.add(disposable);
    }
}
