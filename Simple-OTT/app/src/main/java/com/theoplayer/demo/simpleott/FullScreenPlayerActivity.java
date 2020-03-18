package com.theoplayer.demo.simpleott;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.demo.simpleott.databinding.ActivityPlayerBinding;

public class FullScreenPlayerActivity extends AppCompatActivity {

    private static final String TAG = FullScreenPlayerActivity.class.getSimpleName();

    private static final String PLAYER_PARAM__SOURCE_URL = "SOURCE_URL";

    private ActivityPlayerBinding viewBinding;
    private Player theoPlayer;

    public static void play(Context context, String sourceUrl) {
        Intent playIntent = new Intent(context, FullScreenPlayerActivity.class);
        playIntent.putExtra(PLAYER_PARAM__SOURCE_URL, sourceUrl);
        context.startActivity(playIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.getPlayer();

        // Configuring THEOplayer playback with parameters from intent.
        configureTHEOplayer(
                getIntent().getStringExtra(PLAYER_PARAM__SOURCE_URL)
        );
    }

    private void configureTHEOplayer(String sourceUrl) {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.getSettings().setFullScreenOrientationCoupled(true);

        // Creating a SourceDescription that contains the settings to be applied as a new
        // THEOplayer source.
        SourceDescription sourceDescription = SourceDescriptionUtil.getBySourceUrl(sourceUrl);

        theoPlayer.setSource(null);
        theoPlayer.setAutoplay(true);

        // Configuring THEOplayer with defined SourceDescription object to be played automatically.
        theoPlayer.setSource(sourceDescription);

        // Adding listeners to THEOplayer basic playback events.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY, event -> Log.i(TAG, "Event: PLAY"));
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING, event -> Log.i(TAG, "Event: PLAYING"));
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE, event -> Log.i(TAG, "Event: PAUSE"));
        theoPlayer.addEventListener(PlayerEventTypes.ENDED, event -> Log.i(TAG, "Event: ENDED"));
        theoPlayer.addEventListener(PlayerEventTypes.ERROR, event -> Log.i(TAG, "Event: ERROR, error=" + event.getError()));
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
        try {
            viewBinding.theoPlayerView.onResume();
        } catch (Exception exception) {
            Log.i(TAG, "", exception);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewBinding.theoPlayerView.onDestroy();
    }

}

