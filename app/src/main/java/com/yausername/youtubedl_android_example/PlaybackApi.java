package com.yausername.youtubedl_android_example;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/** Calls only the app's own backend; the server must enforce authentication and entitlement. */
public interface PlaybackApi {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/videos/{videoId}/playback")
    Call<PlaybackResponse> requestPlayback(@Path("videoId") String videoId);
}
