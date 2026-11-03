package com.yausername.youtubedl_android_example;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class PlaybackApiClient {
    // Replace with your own HTTPS backend. Do not point this at a third-party VIP page.
    private static final String BASE_URL = "https://api.example.com/";
    private static final PlaybackApi API = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(new OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlaybackApi.class);

    private PlaybackApiClient() { }

    public static PlaybackApi getApi() {
        return API;
    }
}
