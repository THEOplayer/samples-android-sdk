package com.theoplayer.sample.ui.fullscreen;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.fullscreen.FullScreenChangeListener;
import com.theoplayer.android.api.fullscreen.FullScreenManager;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.TypedSource;
import com.theoplayer.sample.ui.fullscreen.databinding.ActivityPlayerBinding;

import static com.theoplayer.android.api.source.SourceDescription.Builder.sourceDescription;
import static com.theoplayer.android.api.source.TypedSource.Builder.typedSource;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    private ActivityPlayerBinding viewBinding;
    private Player theoPlayer;
    private FullScreenManager theoFullScreenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TheoTheme_Base);
        super.onCreate(savedInstanceState);

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.getPlayer();
        theoFullScreenManager = viewBinding.theoPlayerView.getFullScreenManager();

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar);

        // Configure UI behavior and default values.
        viewBinding.fullScreenButton.setOnClickListener(view -> theoFullScreenManager.requestFullScreen());

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer();
    }

    private void configureTHEOplayer() {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.getSettings().setFullScreenOrientationCoupled(true);

        // Setting custom full screen activity which allows to change behavior
        // and/or look of the full screen activity.
        theoFullScreenManager.setFullscreenActivity(CustomFullScreenActivity.class);

        // Creating a TypedSource builder that defines the location of a single stream source.
        TypedSource.Builder typedSource = typedSource(getString(R.string.defaultSourceUrl));

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        SourceDescription.Builder sourceDescription = sourceDescription(typedSource.build())
                .poster(getString(R.string.defaultPosterUrl));

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.setSource(sourceDescription.build());

        // Adding listeners to THEOplayer basic playback events.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY, event -> Log.i(TAG, "Event: PLAY"));
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING, event -> Log.i(TAG, "Event: PLAYING"));
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE, event -> Log.i(TAG, "Event: PAUSE"));
        theoPlayer.addEventListener(PlayerEventTypes.ENDED, event -> Log.i(TAG, "Event: ENDED"));
        theoPlayer.addEventListener(PlayerEventTypes.ERROR, event -> Log.i(TAG, "Event: ERROR, error=" + event.getError()));

        // Adding listeners to THEOplayer basic full screen changes events.
        theoFullScreenManager.addFullScreenChangeListener(new FullScreenChangeListener() {

            @Override
            public void onEnterFullScreen() {
                Log.i(TAG, "Event: FULL_SCREEN_ENTERED");
            }

            @Override
            public void onExitFullScreen() {
                Log.i(TAG, "Event: FULL_SCREEN_EXITED");
            }

        });
    }


    // In order to work properly and in sync with the activity lifecycle changes (e.g. device
    // is rotated, new activity is started or app is moved to background) we need to call
    // the "onResume", "onPause" and "onDestroy" methods of the THEOplayerView when the matching
    // activity methods are called.

    @Override
    protected void onPause() {
        super.onPause();
        viewBinding.theoPlayerView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewBinding.theoPlayerView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewBinding.theoPlayerView.onDestroy();
    }

}
