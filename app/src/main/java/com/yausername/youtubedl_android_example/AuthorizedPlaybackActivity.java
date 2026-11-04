package com.yausername.youtubedl_android_example;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Plays authorized content returned by the application's entitlement-checking backend. */
public class AuthorizedPlaybackActivity extends AppCompatActivity {
    private EditText videoIdInput;
    private Button playButton;
    private ProgressBar progressBar;
    private TextView statusView;
    private PlayerView playerView;
    private ExoPlayer player;
    private PlaybackApi playbackApi;
    @Nullable private Call<PlaybackResponse> playbackCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorized_playback);

        videoIdInput = findViewById(R.id.authorized_video_id);
        playButton = findViewById(R.id.authorized_play_button);
        progressBar = findViewById(R.id.authorized_progress);
        statusView = findViewById(R.id.authorized_status);
        playerView = findViewById(R.id.authorized_player_view);
        playbackApi = PlaybackApiClient.create(getApplicationContext());

        // The backend should return a short-lived signed URL or provider-approved playback
        // configuration. Do not forward the app's bearer token to a CDN/media host.
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                showStatus(getString(R.string.authorized_player_error));
            }
        });
        playButton.setOnClickListener(view -> requestAuthorizedPlayback());
    }

    private void requestAuthorizedPlayback() {
        String videoId = videoIdInput.getText().toString().trim();
        if (TextUtils.isEmpty(videoId)) {
            videoIdInput.setError(getString(R.string.authorized_video_id_error));
            return;
        }
        if (new SessionManager(this).getAccessToken() == null) {
            showStatus(getString(R.string.authorized_login_required));
            return;
        }

        if (playbackCall != null) playbackCall.cancel();
        setLoading(true);
        showStatus(getString(R.string.authorized_requesting));
        playbackCall = playbackApi.requestPlayback(videoId);
        playbackCall.enqueue(new Callback<PlaybackResponse>() {
            @Override
            public void onResponse(Call<PlaybackResponse> call, Response<PlaybackResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);
                if (response.code() == 401 || response.code() == 403) {
                    showStatus(getString(R.string.authorized_not_permitted));
                    return;
                }
                PlaybackResponse body = response.body();
                if (!response.isSuccessful() || body == null || body.code != 0
                        || body.data == null || TextUtils.isEmpty(body.data.playUrl)) {
                    showStatus(getString(R.string.authorized_bad_response));
                    return;
                }
                playAuthorizedUrl(body.data);
            }

            @Override
            public void onFailure(Call<PlaybackResponse> call, Throwable error) {
                if (isFinishing() || isDestroyed() || call.isCanceled()) return;
                setLoading(false);
                showStatus(getString(R.string.authorized_network_error));
            }
        });
    }

    private void playAuthorizedUrl(PlaybackResponse.PlaybackData data) {
        Uri uri = Uri.parse(data.playUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || TextUtils.isEmpty(uri.getHost())) {
            showStatus(getString(R.string.authorized_invalid_url));
            return;
        }

        MediaItem.Builder item = new MediaItem.Builder().setUri(uri);
        if (!TextUtils.isEmpty(data.mimeType)) item.setMimeType(data.mimeType);
        // DRM must use the content provider's official license integration.
        player.setMediaItem(item.build());
        player.prepare();
        player.play();
        showStatus(getString(R.string.authorized_playing));
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        playButton.setEnabled(!loading);
    }

    private void showStatus(String message) {
        statusView.setText(message);
    }

    @Override
    protected void onStop() {
        if (player != null) player.pause();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (playbackCall != null) playbackCall.cancel();
        if (playerView != null) playerView.setPlayer(null);
        if (player != null) player.release();
        super.onDestroy();
    }
}
