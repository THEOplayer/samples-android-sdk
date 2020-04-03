package com.theoplayer.sample.playback.googlecast;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.theoplayer.android.api.cast.chromecast.Chromecast;
import com.theoplayer.android.api.cast.chromecast.PlayerCastState;
import com.theoplayer.android.api.event.chromecast.ChromecastEventTypes;
import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.TypedSource;
import com.theoplayer.android.api.source.metadata.ChromecastMetadataDescription;
import com.theoplayer.sample.playback.googlecast.databinding.ActivityPlayerBinding;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    private ActivityPlayerBinding viewBinding;
    private Player theoPlayer;
    private Chromecast theoChromecast;
    private CastContext castContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TheoTheme_Base);
        super.onCreate(savedInstanceState);

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.getPlayer();
        theoChromecast = viewBinding.theoPlayerView.getCast().getChromecast();
        castContext = CastContext.getSharedInstance(this);

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar);

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_player_menu, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.castMenuItem);
        return true;
    }

    private void configureTHEOplayer() {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.getSettings().setFullScreenOrientationCoupled(true);

        // Creating a TypedSource builder that defines the location of a single stream source.
        TypedSource.Builder typedSource = TypedSource.Builder
                .typedSource(getString(R.string.defaultSourceUrl));

        // Creating a ChromecastMetadataDescription builder that defines stream metadata to be
        // displayed on cast sender and receiver while casting.
        ChromecastMetadataDescription.Builder chromecastMetadata = ChromecastMetadataDescription.Builder
                .chromecastMetadata()
                .title(getString(R.string.defaultTitle))
                .images(getString(R.string.defaultPosterUrl));

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        SourceDescription.Builder sourceDescription = SourceDescription.Builder
                .sourceDescription(typedSource.build())
                .poster(getString(R.string.defaultPosterUrl))
                .metadata(chromecastMetadata.build());

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.setSource(sourceDescription.build());

        // Joining global cast session events with single THEOplayer instance cast session manager.
        castContext.addCastStateListener(newCastState -> {
            switch (newCastState) {
                case CastState.CONNECTED:
                    if (!theoPlayer.isPaused()) {
                        theoChromecast.start();
                    }
                    break;
                case CastState.NOT_CONNECTED:
                case CastState.NO_DEVICES_AVAILABLE:
                    if (theoChromecast.getState() == PlayerCastState.CONNECTED) {
                        theoChromecast.stop();
                        theoPlayer.play();
                    }
                    break;
            }
        });

        // Some streaming setups requires casting a different stream to a Cast Receiver device
        // than the one playing on a Cast Sender device, e.g. different DRM capabilities.
        // Code below shows how to configure such different stream to cast.
        //
        // SourceDescription.Builder otherSourceDescription = SourceDescription.Builder
        //        .sourceDescription(getString(R.string.defaultSourceUrl));
        // theoChromecast.setSource(otherSourceDescription.build());

        // Adding listeners to THEOplayer basic playback events.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY, event -> {
            Log.i(TAG, "Event: PLAY");
            // Start casting when global cast session is established but source is not being casted yet.
            if (castContext.getCastState() == CastState.CONNECTED && theoChromecast.getState() != PlayerCastState.CONNECTED) {
                theoChromecast.start();
            }
        });
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING, event -> Log.i(TAG, "Event: PLAYING"));
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE, event -> Log.i(TAG, "Event: PAUSE"));
        theoPlayer.addEventListener(PlayerEventTypes.ENDED, event -> Log.i(TAG, "Event: ENDED"));
        theoPlayer.addEventListener(PlayerEventTypes.ERROR, event -> Log.i(TAG, "Event: ERROR, error=" + event.getError()));

        // Adding listeners to THEOplayer cast events.
        theoChromecast.addEventListener(ChromecastEventTypes.STATECHANGE, event -> Log.i(TAG, "Event: CAST_STATECHANGE, state=" + event.getState()));
        theoChromecast.addEventListener(ChromecastEventTypes.ERROR, event -> Log.i(TAG, "Event: CAST_ERROR, error=" + event.getError()));
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
