package com.yausername.youtubedl_android_example;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** Adds the signed-in app user's backend token; it contains no provider or service credentials. */
public final class BearerTokenInterceptor implements Interceptor {
    private final SessionManager sessionManager;

    public BearerTokenInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = sessionManager.getAccessToken();
        if (token == null || token.isEmpty()) return chain.proceed(original);

        Request authenticated = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(authenticated);
    }
}
