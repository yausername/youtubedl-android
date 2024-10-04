package com.yausername.youtubedl_android_example;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class StreamingExampleActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStartStream;
    private EditText etUrl;
    private VideoView videoView;
    private ProgressBar pbLoading;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private static final String TAG = "StreamingExample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_example);

        initViews();
        initListeners();
    }

    private void initViews() {
        btnStartStream = findViewById(R.id.btn_start_streaming);
        etUrl = findViewById(R.id.et_url);
        videoView = findViewById(R.id.video_view);
        pbLoading = findViewById(R.id.pb_status);
    }

    private void initListeners() {
        btnStartStream.setOnClickListener(this);
        videoView.setOnPreparedListener(() -> videoView.start());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start_streaming) {
            startStream();
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void startStream() {
        String url = etUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            etUrl.setError(getString(R.string.url_error));
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);
        Disposable disposable = Observable.fromCallable(() -> {
                    YoutubeDLRequest request = new YoutubeDLRequest(url);
                    // best stream containing video+audio
                    request.addOption("-f", "best");
                    return YoutubeDL.getInstance().getInfo(request);
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(streamInfo -> {
                    pbLoading.setVisibility(View.GONE);
                    String videoUrl = streamInfo.getUrl();
                    if (TextUtils.isEmpty(videoUrl)) {
                        Toast.makeText(StreamingExampleActivity.this, "failed to get stream url", Toast.LENGTH_LONG).show();
                    } else {
                        setupVideoView(videoUrl);
                    }
                }, e -> {
                    if (BuildConfig.DEBUG) Log.e(TAG, "failed to get stream info", e);
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(StreamingExampleActivity.this, "streaming failed. failed to get stream info", Toast.LENGTH_LONG).show();
                });
        compositeDisposable.add(disposable);
    }

    private void setupVideoView(String videoUrl) {
        videoView.setMedia(Uri.parse(videoUrl));
    }
}
