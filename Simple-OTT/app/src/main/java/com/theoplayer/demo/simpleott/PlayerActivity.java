package com.theoplayer.demo.simpleott;

import android.annotation.TargetApi;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.TypedSource;
import com.theoplayer.android.api.source.metadata.ChromecastMetadataDescription;
import com.theoplayer.demo.simpleott.databinding.ActivityPlayerBinding;
import com.theoplayer.demo.simpleott.model.StreamSource;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    private static final String PLAYER_PARAM__SOURCE = "SOURCE";
    private static final String PLAYER_PARAM__TITLE = "TITLE";

    private static boolean SUPPORTS_PIP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

    private ActivityPlayerBinding viewBinding;
    private Player theoPlayer;

    /**
     * Allows to start playback of given <code>streamSource</code>.
     * <p/>
     * There's no need to configure THEOplayer source with any caching task. THEOplayer will find
     * automatically caching task for played source if any exists.
     *
     * @param context - The current context.
     * @param streamSource - The stream source to be played.
     */
    public static void play(Context context, StreamSource streamSource) {
        Intent playIntent = new Intent(context, PlayerActivity.class);
        playIntent.putExtra(PLAYER_PARAM__SOURCE, streamSource.getSource());
        playIntent.putExtra(PLAYER_PARAM__TITLE, streamSource.getTitle());
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
                getIntent().getStringExtra(PLAYER_PARAM__SOURCE),
                getIntent().getStringExtra(PLAYER_PARAM__TITLE)
        );
    }

    private void configureTHEOplayer(String source, String title) {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.getSettings().setFullScreenOrientationCoupled(true);

        // Creating a TypedSource builder that defines the location of a single stream source.
        TypedSource.Builder typedSource = TypedSource.Builder.typedSource(source);

        // Creating a ChromecastMetadataDescription builder that defines stream metadata to be
        // displayed on cast sender and receiver while casting.
        ChromecastMetadataDescription.Builder chromecastMetadata = ChromecastMetadataDescription.Builder
                .chromecastMetadata()
                .title(title);

        // Creating a SourceDescription that contains the tab_settings to be applied as a new
        // THEOplayer source.
        SourceDescription.Builder sourceDescription = SourceDescription.Builder
                .sourceDescription(typedSource.build())
                .metadata(chromecastMetadata.build());

        theoPlayer.setAutoplay(true);

        // Configuring THEOplayer with defined SourceDescription object to be played automatically.
        theoPlayer.setSource(sourceDescription.build());

        // Adding listeners to THEOplayer basic playback events.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY, event -> Log.i(TAG, "Event: PLAY"));
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING, event -> Log.i(TAG, "Event: PLAYING"));
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE, event -> Log.i(TAG, "Event: PAUSE"));
        theoPlayer.addEventListener(PlayerEventTypes.ENDED, event -> Log.i(TAG, "Event: ENDED"));
        theoPlayer.addEventListener(PlayerEventTypes.ERROR, event -> Log.i(TAG, "Event: ERROR, error=" + event.getErrorObject()));
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        viewBinding.theoPlayerView.getSettings().setFullScreenOrientationCoupled(!isInPictureInPictureMode);
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }

    @Override
    protected void onUserLeaveHint() {
        if (SUPPORTS_PIP) {
            if (!theoPlayer.isPaused()) {
                enterPictureInPictureMode(new PictureInPictureParams.Builder().build());
            }
        } else {
            ToastUtils.toastMessage(this, R.string.pipNotSupported);
        }
    }

    // In order to work properly and in sync with the activity lifecycle changes (e.g. device
    // is rotated, new activity is started or app is moved to background) we need to call
    // the "onResume", "onPause" and "onDestroy" methods of the THEOplayerView when the matching
    // activity methods are called.

    @Override
    protected void onPause() {
        super.onPause();
        if (SUPPORTS_PIP && !isInPictureInPictureMode()) {
            viewBinding.theoPlayerView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SUPPORTS_PIP && !isInPictureInPictureMode()) {
            try {
                viewBinding.theoPlayerView.onResume();
            } catch (Exception exception) {
                Log.i(TAG, "", exception);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewBinding.theoPlayerView.onDestroy();
    }

}

