package com.yausername.youtubedl_android_example;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.yausername.youtubedl_android.BuildConfig;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStreamingExample;
    private Button btnDownloadingExample;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initListeners();

        initLibraries();
    }

    private void initLibraries() {
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, @Nullable String tag) {
                return BuildConfig.DEBUG;
            }
        });

        try {
            YoutubeDL.getInstance().init(getApplication());
        } catch (YoutubeDLException e) {
            Logger.e(e, "failed to initialize youtubedl-android");
        }
    }

    private void initListeners() {
        btnStreamingExample.setOnClickListener(this);
        btnDownloadingExample.setOnClickListener(this);
    }

    private void initViews() {
        btnStreamingExample = findViewById(R.id.btn_streaming_example);
        btnDownloadingExample = findViewById(R.id.btn_downloading_example);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_streaming_example: {
                Intent i = new Intent(MainActivity.this, StreamingExampleActivity.class);
                startActivity(i);
                break;
            }
            case R.id.btn_downloading_example: {
                Intent i = new Intent(MainActivity.this, DownloadingExampleActivity.class);
                startActivity(i);
                break;
            }
        }
    }
}
