package com.yausername.youtubedl_android_example;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class App extends Application {

    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();

        configureRxJavaErrorHandler();

        Completable.fromAction(this::initLibraries).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
            Toast.makeText(this, "initialization success", Toast.LENGTH_SHORT);
        }, (e) -> {
            Logger.e(e, "failed to initialize youtubedl-android");
            Toast.makeText(this, "initialization failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT);
        });
    }

    private void configureRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(e -> {

            if (e instanceof UndeliverableException) {
                // As UndeliverableException is a wrapper, get the cause of it to get the "real" exception
                e = e.getCause();
            }

            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }

            Log.e(TAG, "Undeliverable exception received, not sure what to do", e);
        });
    }

    private void initLibraries() throws YoutubeDLException {
        YoutubeDL.getInstance().init(this);
        FFmpeg.getInstance().init(this);
    }

}
