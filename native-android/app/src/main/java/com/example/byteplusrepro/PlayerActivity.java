package com.example.byteplusrepro;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.app.PictureInPictureUiState;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.TTVideoEngineInterface;
import com.ss.ttvideoengine.VideoEngineSimpleCallback;
import com.ss.ttvideoengine.source.DirectUrlSource;
import org.json.JSONObject;

public class PlayerActivity extends Activity {
    TTVideoEngine engine;
    private FrameLayout root;
    private SurfaceView videoView;
    private Button button;
    private MediaSession mediaSession;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocus;
    private boolean resumeOnStart;
    private int videoWidth = 9;
    private int videoHeight = 16;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        engine = new TTVideoEngine(getApplicationContext(), TTVideoEngine.PLAYER_TYPE_OWN);
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        videoView = new SurfaceView(this);
        root.addView(videoView, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));
        button = new Button(this);
        button.setText("Play / Pause");
        FrameLayout.LayoutParams controls = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        controls.bottomMargin = 64;
        root.addView(button, controls);
        setContentView(root);
        button.setOnClickListener(view -> togglePlayback());
        root.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> layoutVideo());
        videoView.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> updatePip());
        engine.setSurfaceHolder(videoView.getHolder());
        engine.setDisplayMode(videoView, TTVideoEngineInterface.IMAGE_LAYOUT_ASPECT_FIT);

        audioManager = getSystemService(AudioManager.class);
        audioFocus = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build())
            .setOnAudioFocusChangeListener(change -> {
                if (change < 0) { resumeOnStart = false; pausePlayback(); }
            }).build();
        mediaSession = new MediaSession(this, "BytePlusRepro");
        mediaSession.setMetadata(new MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, "BytePlus PiP Repro").build());
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override public void onPlay() { playPlayback(); }
            @Override public void onPause() { pausePlayback(); }
        });
        mediaSession.setActive(true);
        engine.setVideoEngineSimpleCallback(new VideoEngineSimpleCallback() {
            @Override public void onPlaybackStateChanged(TTVideoEngine player, int state) {
                runOnUiThread(() -> updatePlayback());
            }
            @Override public void onVideoSizeChanged(TTVideoEngine player, int width, int height) {
                if (width <= 0 || height <= 0) return;
                runOnUiThread(() -> { videoWidth = width; videoHeight = height; layoutVideo(); });
            }
            @Override public void onRenderStart(TTVideoEngine player) {
                Log.i("BytePlusRepro", "first-frame");
            }
            @Override public void onCompletion(TTVideoEngine player) {
                runOnUiThread(() -> pausePlayback());
            }
            @Override public void onError(com.ss.ttvideoengine.utils.Error error) {
                runOnUiThread(() -> {
                    pausePlayback();
                    Toast.makeText(PlayerActivity.this, "Playback failed. Check license and local video configuration.", Toast.LENGTH_LONG).show();
                    Log.e("BytePlusRepro", "playback-error code=" + error.code);
                });
            }
        });
        try {
            JSONObject video = ((ReproApplication) getApplication()).config.getJSONObject("video");
            String url = video.getString("url");
            engine.setStrategySource(new DirectUrlSource.Builder().setVid(video.getString("videoId"))
                .addItem(new DirectUrlSource.UrlItem.Builder().setUrl(url)
                    .setCacheKey(TTVideoEngine.computeMD5(url)).setPlayAuth(video.optString("auth", "")).build())
                .build());
        } catch (Exception error) {
            throw new IllegalStateException("Set videoId, url and auth in local.config.json");
        }
        updatePlayback();
    }

    void togglePlayback() {
        if (engine.isShouldPlay()) pausePlayback(); else playPlayback();
    }

    private void playPlayback() {
        if (audioManager.requestAudioFocus(audioFocus) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            engine.play();
            updatePlayback();
        }
    }

    private void pausePlayback() {
        engine.pause();
        audioManager.abandonAudioFocusRequest(audioFocus);
        updatePlayback();
    }

    private void updatePlayback() {
        boolean playing = engine.isShouldPlay();
        mediaSession.setPlaybackState(new PlaybackState.Builder()
            .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PLAY_PAUSE)
            .setState(playing ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED,
                engine.getCurrentPlaybackTime(), playing ? 1 : 0).build());
        if (playing) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        updatePip();
    }

    private void layoutVideo() {
        if (root.getWidth() == 0 || root.getHeight() == 0) return;
        float scale = Math.min((float) root.getWidth() / videoWidth, (float) root.getHeight() / videoHeight);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) videoView.getLayoutParams();
        int width = Math.round(videoWidth * scale), height = Math.round(videoHeight * scale);
        if (params.width != width || params.height != height) {
            params.width = width;
            params.height = height;
            videoView.setLayoutParams(params);
        }
    }

    private boolean supportsPip() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
    }

    private PictureInPictureParams pipParams() {
        Rational ratio = new Rational(videoWidth, videoHeight);
        if (ratio.floatValue() < 1f / 2.39f) ratio = new Rational(100, 239);
        if (ratio.floatValue() > 2.39f) ratio = new Rational(239, 100);
        PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder()
            .setAspectRatio(ratio);
        Rect bounds = new Rect();
        if (videoView.getGlobalVisibleRect(bounds)) builder.setSourceRectHint(bounds);
        if (Build.VERSION.SDK_INT >= 31) {
            builder.setAutoEnterEnabled(engine.isShouldPlay()).setSeamlessResizeEnabled(true);
        }
        return builder.build();
    }

    private void updatePip() {
        if (supportsPip()) setPictureInPictureParams(pipParams());
    }

    @Override public void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT < 31 && supportsPip() && engine.isShouldPlay()) {
            enterPictureInPictureMode(pipParams());
        }
    }

    @Override public void onPictureInPictureUiStateChanged(PictureInPictureUiState state) {
        super.onPictureInPictureUiStateChanged(state);
        if (Build.VERSION.SDK_INT >= 35 && state.isTransitioningToPip()) button.setVisibility(View.GONE);
    }

    @Override public void onPictureInPictureModeChanged(boolean pip, Configuration configuration) {
        super.onPictureInPictureModeChanged(pip, configuration);
        button.setVisibility(pip ? View.GONE : View.VISIBLE);
        Log.i("BytePlusRepro", "pip=" + pip + " position=" + engine.getCurrentPlaybackTime());
    }

    // PiPでもonPauseは呼ばれるため、画面が見えなくなるonStopでのみ止める。
    @Override public void onStop() {
        resumeOnStart = engine.isShouldPlay();
        pausePlayback();
        super.onStop();
    }

    @Override public void onStart() {
        super.onStart();
        if (resumeOnStart) { resumeOnStart = false; playPlayback(); }
    }

    @Override public void onDestroy() {
        engine.setVideoEngineSimpleCallback(null);
        engine.releaseAsync();
        mediaSession.release();
        audioManager.abandonAudioFocusRequest(audioFocus);
        super.onDestroy();
    }
}
