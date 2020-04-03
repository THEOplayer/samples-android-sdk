package com.theoplayer.sample.ui.custom;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.slider.Slider;
import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.android.api.player.ReadyState;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.TypedSource;
import com.theoplayer.sample.ui.custom.databinding.ActivityPlayerBinding;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    private ActivityPlayerBinding viewBinding;
    private PlayerViewModel viewModel;
    private Player theoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TheoTheme_Base);
        super.onCreate(savedInstanceState);

        // Inflating view and obtaining an instance of the binding and model classes.
        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);
        viewBinding.setLifecycleOwner(this);
        viewBinding.setViewModel(viewModel);

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.getPlayer();

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar);

        // Configure UI behavior and default values.
        configureUI();

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer();
    }

    private void configureUI() {
        // Listening to player overlay click events to toggle player controls visibility.
        viewBinding.playerClickableOverlay.setOnClickListener(view -> viewModel.toggleUI());

        // Listening to play/pause button click events to play/pause stream playback.
        viewBinding.playPauseButton.setOnClickListener(view -> {
            if (theoPlayer.isPaused()) {
                theoPlayer.play();
            } else {
                theoPlayer.pause();
            }
        });

        // Listening to skipForward button click events to move stream forward by given tine interval.
        viewBinding.skipForwardButton.setOnClickListener(view -> {
            int skipForwardInSeconds = getResources().getInteger(R.integer.skipForwardInSeconds);
            theoPlayer.requestCurrentTime(currentTime -> theoPlayer.setCurrentTime(currentTime + skipForwardInSeconds));
        });

        // Listening to skipBackward button click events to move stream backward by given tine interval.
        viewBinding.skipBackwardButton.setOnClickListener(view -> {
            int skipBackwardInSeconds = getResources().getInteger(R.integer.skipBackwardInSeconds);
            theoPlayer.requestCurrentTime(currentTime -> theoPlayer.setCurrentTime(currentTime - skipBackwardInSeconds));
        });

        // Listening to slider seeking events to change stream position to selected time interval
        // and to display stream progress while seeking.
        viewBinding.progressSlider.setLabelFormatter(viewModel::formatTimeValue);
        viewBinding.progressSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                viewModel.markSeeking();
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                viewModel.markSought();
                theoPlayer.setCurrentTime(slider.getValue());
            }
        });
    }

    private void configureTHEOplayer() {
        // Creating a TypedSource builder that defines the location of a single stream source.
        TypedSource.Builder typedSource = TypedSource.Builder
                .typedSource(getString(R.string.defaultSourceUrl));

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        SourceDescription.Builder sourceDescription = SourceDescription.Builder
                .sourceDescription(typedSource.build())
                .poster(getString(R.string.defaultPosterUrl));

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.setSource(sourceDescription.build());

        // Listening to 'sourcechange' event which indicates resetting UI and displaying only big
        // play button that loads defined source.
        theoPlayer.addEventListener(PlayerEventTypes.SOURCECHANGE, event -> {
            Log.i(TAG, "Event: SOURCECHANGE");
            viewModel.resetUI();
        });

        // Listening to 'play' event which indicates the intent of playing source. Depending on
        // actual state, source will be loaded first and/or played.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY, event -> {
            Log.i(TAG, "Event: PLAY");
            viewModel.markBuffering();
        });

        // Listening to 'duration` event which indicates that the source is loaded to the point
        // that its duration is known.
        theoPlayer.addEventListener(PlayerEventTypes.DURATIONCHANGE, event -> {
            Log.i(TAG, "Event: DURATIONCHANGE, " + event.getDuration());
            viewModel.setDuration(event.getDuration());
        });

        // Listening to 'playing' event which indicates that source is being played.
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING, event -> {
            Log.i(TAG, "Event: PLAYING");
            viewModel.markPlaying();
        });

        // Listening to 'pause' event which indicates that the source was paused.
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE, event -> {
            Log.i(TAG, "Event: PAUSE");
            viewModel.markPaused();
        });

        // Listening to 'readystatechange' event which indicates the ability of playing the source.
        // This is the most general way of getting stream state. There are more specific events like
        // 'canplay', 'canplaythrough', 'waiting', 'seeking', 'seeked' that allows to design more
        // advanced flows.
        theoPlayer.addEventListener(PlayerEventTypes.READYSTATECHANGE, event -> {
            Log.i(TAG, "Event: READYSTATECHANGE, readyState=" + event.getReadyState());
            if (event.getReadyState() != ReadyState.HAVE_ENOUGH_DATA) {
                viewModel.markBuffering();
            }
        });

        // Listening to 'timeupdate' event which indicates source playback position change.
        theoPlayer.addEventListener(PlayerEventTypes.TIMEUPDATE, event -> {
            Log.i(TAG, "Event: TIMEUPDATE, currentTime=" + event.getCurrentTime());
            viewModel.setCurrentTime(event.getCurrentTime());
        });

        // Listening to 'error' event which indicates that something went wrong.
        theoPlayer.addEventListener(PlayerEventTypes.ERROR, event ->
                Log.i(TAG, "Event: ERROR, error=" + event.getError())
        );
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int uiVisibilityFlags = View.SYSTEM_UI_FLAG_VISIBLE;

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // With landscape orientation window (activity) display mode is changed to full screen
            // with status, navigation and action bars hidden.
            // Note that status and navigation bars are still available on swiping screen edge.
            getSupportActionBar().hide();

            uiVisibilityFlags = uiVisibilityFlags
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                uiVisibilityFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
        } else {
            // With portrait orientation window (activity) display mode is changed back to default
            // with status, navigation and action bars shown.
            getSupportActionBar().show();
        }
        getWindow().getDecorView().setSystemUiVisibility(uiVisibilityFlags);
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