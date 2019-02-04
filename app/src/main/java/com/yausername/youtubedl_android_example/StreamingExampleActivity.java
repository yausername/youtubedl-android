package com.yausername.youtubedl_android_example;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.orhanobut.logger.Logger;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.mapper.VideoFormat;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import org.apache.commons.lang3.StringUtils;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class StreamingExampleActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStartStream;
    private EditText etUrl;
    private VideoView videoView;
    private TextView tvStreamStatus;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

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
        tvStreamStatus = findViewById(R.id.tv_status);
    }

    private void initListeners() {
        btnStartStream.setOnClickListener(this);
        videoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                videoView.start();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_streaming: {
                startStream();
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void startStream() {
        String url = etUrl.getText().toString();
        if (StringUtils.isBlank(url)) {
            etUrl.setError(getString(R.string.url_error));
            return;
        }

        tvStreamStatus.setText("Fetching Stream Info");
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().getInfo(url))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(streamInfo -> {
                    String videoUrl = getVideoUrl(streamInfo);
                    if(StringUtils.isBlank(videoUrl)){
                        tvStreamStatus.setText("Failed to fetch Stream Info");
                        Toast.makeText(StreamingExampleActivity.this, "failed to get stream url", Toast.LENGTH_LONG).show();
                    }else{
                        tvStreamStatus.setText("Streaming Now");
                        setupVideoView(videoUrl);
                    }
                }, e -> {
                    tvStreamStatus.setText("Failed to fetch Stream Info");
                    Toast.makeText(StreamingExampleActivity.this, "streaming failed. failed to get stream info", Toast.LENGTH_LONG).show();
                    Logger.e(e, "failed to get stream info");
                });
        compositeDisposable.add(disposable);
    }

    private void setupVideoView(String videoUrl) {
        videoView.setVideoURI(Uri.parse(videoUrl));
    }

    private String getVideoUrl(VideoInfo streamInfo) {
        String videoUrl = null;
        if(null == streamInfo || null == streamInfo.formats){
            Toast.makeText(StreamingExampleActivity.this, "failed to get stream url", Toast.LENGTH_LONG).show();
            return null;
        }
        for(VideoFormat f: streamInfo.formats){
            if("mp4".equals(f.ext)){
                videoUrl = f.url;
                break;
            }
        }
        return videoUrl;
    }
}
