package com.theoplayer.sample.ads.custom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.theoplayer.android.api.event.ads.AdsEventTypes;
import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.TypedSource;
import com.theoplayer.android.api.source.addescription.GoogleImaAdDescription;
import com.theoplayer.sample.ads.custom.databinding.ActivityPlayerBinding;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    private static final String PLAYER_PARAM__SOURCE_URL = "SOURCE_URL";
    private static final String PLAYER_PARAM__AD_URL = "AD_URL";
    private static final String PLAYER_PARAM__AD_TIME_OFFSET = "AD_TIME_OFFSET";

    private ActivityPlayerBinding viewBinding;
    private Player theoPlayer;

    public static void play(Context context, String sourceUrl, String adUrl, String adTimeOffset) {
        Intent playIntent = new Intent(context, PlayerActivity.class);
        playIntent.putExtra(PLAYER_PARAM__SOURCE_URL, sourceUrl);
        playIntent.putExtra(PLAYER_PARAM__AD_URL, adUrl);
        playIntent.putExtra(PLAYER_PARAM__AD_TIME_OFFSET, adTimeOffset);
        context.startActivity(playIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.getPlayer();

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configuring THEOplayer playback with parameters from intent.
        configureTHEOplayer(
                getIntent().getStringExtra(PLAYER_PARAM__SOURCE_URL),
                getIntent().getStringExtra(PLAYER_PARAM__AD_URL),
                getIntent().getStringExtra(PLAYER_PARAM__AD_TIME_OFFSET)
        );
    }

    private void configureTHEOplayer(String sourceUrl, String adUrl, String adTimeOffset) {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.getSettings().setFullScreenOrientationCoupled(true);

        // Creating a TypedSource builder that defines the location of a single stream source.
        TypedSource.Builder typedSource = new TypedSource.Builder(sourceUrl);

        // Creating a GoogleImaAdDescription builder that defines the location of an ad and its
        // time offset.
        GoogleImaAdDescription.Builder adDescription = new GoogleImaAdDescription.Builder(adUrl)
                .timeOffset(adTimeOffset);

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source including ads configuration.
        SourceDescription.Builder sourceDescription = new SourceDescription.Builder(typedSource.build())
                .ads(adDescription.build());

        // Configuring THEOplayer with defined SourceDescription object to be played automatically.
        theoPlayer.setSource(sourceDescription.build());
        theoPlayer.setAutoplay(true);

        // Adding listeners to THEOplayer basic playback events.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY, event -> Log.i(TAG, "Event: PLAY"));
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING, event -> Log.i(TAG, "Event: PLAYING"));
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE, event -> Log.i(TAG, "Event: PAUSE"));
        theoPlayer.addEventListener(PlayerEventTypes.ENDED, event -> Log.i(TAG, "Event: ENDED"));
        theoPlayer.addEventListener(PlayerEventTypes.ERROR, event -> Log.i(TAG, "Event: ERROR, error=" + event.getErrorObject()));

        // Adding listeners to THEOplayer basic ad events.
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_BEGIN, event -> Log.i(TAG, "Event: AD_BEGIN, ad=" + event.getAd()));
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_END, event -> Log.i(TAG, "Event: AD_END, ad=" + event.getAd()));
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_ERROR, event -> Log.i(TAG, "Event: AD_ERROR, error=" + event.getError()));
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
