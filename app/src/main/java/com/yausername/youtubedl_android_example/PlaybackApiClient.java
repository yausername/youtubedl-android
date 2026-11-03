package com.yausername.youtubedl_android_example;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class PlaybackApiClient {
    // Replace with your own HTTPS backend. Never point this at a third-party VIP page.
    private static final String BASE_URL = "https://api.example.com/";

    private PlaybackApiClient() { }

    public static PlaybackApi create(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BearerTokenInterceptor(sessionManager))
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PlaybackApi.class);
    }
}
