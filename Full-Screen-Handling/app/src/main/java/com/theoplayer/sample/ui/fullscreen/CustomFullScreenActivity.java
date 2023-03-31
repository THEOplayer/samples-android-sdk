package com.theoplayer.sample.ui.fullscreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.fullscreen.FullScreenActivity;
import com.theoplayer.android.api.fullscreen.FullScreenManager;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.sample.ui.fullscreen.databinding.ActivityFullscreenBinding;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class CustomFullScreenActivity extends FullScreenActivity {

    private AppCompatDelegate appCompatDelegate;
    private ActivityFullscreenBinding viewBinding;
    private Player theoPlayer;
    private FullScreenManager theoFullScreenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Adding support for extended AppCompat features.
        // It allows to use styles and themes defined for material components.
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

        // Inflating custom view and obtaining an instance of the binding class.
        viewBinding = ActivityFullscreenBinding.inflate(LayoutInflater.from(this), null, false);
        getDelegate().addContentView(viewBinding.getRoot(), new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        // Gathering THEO objects references.
        theoPlayer = getTHEOplayerView().getPlayer();
        theoFullScreenManager = getTHEOplayerView().getFullScreenManager();

        // Configuring UI behavior.
        adjustPlayPauseButtonIcon();
        viewBinding.playPauseButton.setOnClickListener((button) -> onPlayPauseClick());
        viewBinding.exitFullScreenButton.setOnClickListener((button) -> onFullScreenExit());

        // Set default orientation
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);

        // Configuring THEOplayer.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY, (event) -> adjustPlayPauseButtonIcon());
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE, (event) -> adjustPlayPauseButtonIcon());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    @NonNull
    public AppCompatDelegate getDelegate() {
        if (appCompatDelegate == null) {
            appCompatDelegate = AppCompatDelegate.create(this, null);
        }
        return appCompatDelegate;
    }

    private void onFullScreenExit() {
        theoFullScreenManager.exitFullScreen();
    }

    private void onPlayPauseClick() {
        if (theoPlayer.isPaused()) {
            theoPlayer.play();
        } else {
            theoPlayer.pause();
        }
    }

    private void adjustPlayPauseButtonIcon() {
        if (theoPlayer.isPaused()) {
            viewBinding.playPauseButton.setIconResource(R.drawable.ic_play);
        } else {
            viewBinding.playPauseButton.setIconResource(R.drawable.ic_pause);
        }
    }

}
