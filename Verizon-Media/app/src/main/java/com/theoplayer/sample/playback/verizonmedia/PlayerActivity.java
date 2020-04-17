package com.theoplayer.sample.playback.verizonmedia;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.theoplayer.android.api.THEOplayerConfig;
import com.theoplayer.android.api.THEOplayerView;
import com.theoplayer.android.api.event.EventListener;
import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.event.verizonmedia.VerizonMediaAdBreakEventTypes;
import com.theoplayer.android.api.event.verizonmedia.VerizonMediaAdBreakListEvent;
import com.theoplayer.android.api.event.verizonmedia.VerizonMediaAdBreakListEventTypes;
import com.theoplayer.android.api.event.verizonmedia.VerizonMediaAdEventTypes;
import com.theoplayer.android.api.event.verizonmedia.VerizonMediaEventTypes;
import com.theoplayer.android.api.event.verizonmedia.VerizonMediaPreplayResponseEvent;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.TypedSource;
import com.theoplayer.android.api.source.verizonmedia.VerizonMediaAssetType;
import com.theoplayer.android.api.source.verizonmedia.VerizonMediaPingConfiguration;
import com.theoplayer.android.api.source.verizonmedia.VerizonMediaSource;
import com.theoplayer.android.api.verizonmedia.SkippedAdStrategy;
import com.theoplayer.android.api.verizonmedia.VerizonMediaConfiguration;
import com.theoplayer.android.api.verizonmedia.VerizonMediaUiConfiguration;
import com.theoplayer.android.api.verizonmedia.ads.VerizonMediaAd;
import com.theoplayer.android.api.verizonmedia.ads.VerizonMediaAdBreak;
import com.theoplayer.android.api.verizonmedia.ads.VerizonMediaAdList;
import com.theoplayer.android.api.verizonmedia.reponses.VerizonMediaPreplayResponseType;
import com.theoplayer.sample.playback.verizonmedia.databinding.ActivityPlayerBinding;

import java.util.HashMap;
import java.util.Map;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    private ActivityPlayerBinding viewBinding;
    private Player theoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TheoTheme_Base);
        super.onCreate(savedInstanceState);

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.getPlayer();

//         THEOplayerView is created through the layout in this project.
//         You could leverage the code below if you'd be creating the THEOplayerView programmatically
//        VerizonMediaUiConfiguration verizonMediaUiConfiguration = new VerizonMediaUiConfiguration.Builder()
//                .assetMarkers(true) // optional; defaults to true
//                .adBreakMarkers(true) // optional; defaults to true
//                .contentNotification(true) // optional; defaults to true
//                .adNotification(true) // optional; defaults to true
//                .build();
//        VerizonMediaConfiguration verizonMediaConfiguration = new VerizonMediaConfiguration.Builder()
//                .defaultSkipOffset(15) // optional; defaults to -1 (=unskippable)
//                .skippedAdStrategy(SkippedAdStrategy.PLAY_ALL) // optional; defaults to PLAY_NONE
//                .ui(verizonMediaUiConfiguration)
//                .build();
//        THEOplayerConfig theoplayerConfig = new THEOplayerConfig.Builder()
//                .verizonMediaConfiguration(verizonMediaConfiguration)
//                .build();
//        THEOplayerView theoplayerView = new THEOplayerView(this, theoplayerConfig);

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar);

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer();
    }

    private void configureTHEOplayer() {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.getSettings().setFullScreenOrientationCoupled(true);

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
//        VerizonMediaSource verizonMediaSource = createMultiAssetWidevineStream();
//        VerizonMediaSource verizonMediaSource = createLiveFairPlayStreamWithAds();
        VerizonMediaSource verizonMediaSource = createHLSStreamWithAds();
        SourceDescription sourceDescription = SourceDescription.Builder.sourceDescription(verizonMediaSource).build();

        attachEventListeners(theoPlayer);

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.setSource(sourceDescription);
    }

    protected void attachEventListeners(Player theoplayer) {
        // Adding listeners to THEOplayer basic playback events.
        theoplayer.addEventListener(PlayerEventTypes.PLAY, event -> Log.i(TAG, "Event: PLAY"));
        theoplayer.addEventListener(PlayerEventTypes.PLAYING, event -> Log.i(TAG, "Event: PLAYING"));
        theoplayer.addEventListener(PlayerEventTypes.PAUSE, event -> Log.i(TAG, "Event: PAUSE"));
        theoplayer.addEventListener(PlayerEventTypes.ENDED, event -> Log.i(TAG, "Event: ENDED"));
        theoplayer.addEventListener(PlayerEventTypes.ERROR, event -> Log.i(TAG, "Event: ERROR, error=" + event.getError()));

        // Adding listeners to THEOplayer Verizon Media events.
        theoplayer.getVerizonMedia().addEventListener(VerizonMediaEventTypes.PREPLAYRESPONSE, event -> Log.i(TAG, "Event: PREPLAYRESPONSE"));
        theoplayer.getVerizonMedia().addEventListener(VerizonMediaEventTypes.PINGRESPONSE, event -> Log.i(TAG, "Event: PINGRESPONSE"));
        theoplayer.getVerizonMedia().addEventListener(VerizonMediaEventTypes.PINGERROR, event -> Log.i(TAG, "Event: PINGERROR"));
        EventListener<? super VerizonMediaAdBreakListEvent> attachAdBreakEventListeners = new EventListener<VerizonMediaAdBreakListEvent>() {
            @Override
            public void handleEvent(VerizonMediaAdBreakListEvent event) {
                Log.i(TAG, "Event: ADDADBREAK");
                VerizonMediaAdBreak adBreak = event.getAdBreak();
                VerizonMediaAdList ads = event.getAdBreak().getAds();
                for (int i = 0; i < ads.length(); i++) {
                    VerizonMediaAd ad = ads.getItem(i);
                    ad.addEventListener(VerizonMediaAdEventTypes.AD_BEGIN, event2 -> Log.i(TAG, "Event: ADBEGIN"));
                    ad.addEventListener(VerizonMediaAdEventTypes.AD_END, event2 -> Log.i(TAG, "Event: ADBEGIN"));
                }
                adBreak.addEventListener(VerizonMediaAdBreakEventTypes.ADBREAK_BEGIN, event2 -> Log.i(TAG, "Event: ADBREAKBEGIN"));
                adBreak.addEventListener(VerizonMediaAdBreakEventTypes.ADBREAK_END, event2 -> Log.i(TAG, "Event: ADBREAKEND"));
                adBreak.addEventListener(VerizonMediaAdBreakEventTypes.ADBREAK_SKIP, event2 -> Log.i(TAG, "Event: ADBREAKSKIP"));
                adBreak.addEventListener(VerizonMediaAdBreakEventTypes.UPDATE_ADBREAK, event2 -> Log.i(TAG, "Event: UPDATEADBREAK"));
            }
        };
        theoplayer.getVerizonMedia().getAds().getAdBreaks().addEventListener(VerizonMediaAdBreakListEventTypes.ADD_ADBREAK, attachAdBreakEventListeners);
        theoplayer.getVerizonMedia().getAds().getAdBreaks().addEventListener(VerizonMediaAdBreakListEventTypes.REMOVE_ADBREAK, event -> Log.i(TAG, "Event: REMOVEADBREAK"));
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

    /*
     The functions below will create a VerizonMediaSource based
     on the assets available at https://cdn.theoplayer.com/demos/verizon-media/index.html.
     */

    private VerizonMediaSource createMultiAssetWidevineStream() {
        String[] assetIds = {"e973a509e67241e3aa368730130a104d", "e70a708265b94a3fa6716666994d877d"};
        VerizonMediaSource verizonMediaSource = new VerizonMediaSource.Builder(assetIds)
                .assetType(VerizonMediaAssetType.ASSET)
                .contentProtected(true)
                .build();
        return verizonMediaSource;
    }

    private VerizonMediaSource createLiveFairPlayStreamWithAds() {
        Map<String, String> preplayParameters = new HashMap<>();
        preplayParameters.put("ad", "cleardashnew");
        VerizonMediaPingConfiguration pingConfiguration = new VerizonMediaPingConfiguration.Builder()
                .linearAdData(true) // Defaults to true if VerizonMediaAssetType is "CHANNEL" or "EVENT", otherwise false.
                .adImpressions(false) // Defaults to false
                .freeWheelVideoViews(true) // Defaults to false
                .build();
        VerizonMediaSource verizonMediaSource = new VerizonMediaSource.Builder("3c367669a83b4cdab20cceefac253684")
                .assetType(VerizonMediaAssetType.CHANNEL)
                .parameters(preplayParameters)
                .ping(pingConfiguration)
                .contentProtected(true)
                .build();
        return verizonMediaSource;
    }

    private VerizonMediaSource createHLSStreamWithAds() {
        String[] assetIds = {
                "41afc04d34ad4cbd855db52402ef210e",
                "c6b61470c27d44c4842346980ec2c7bd",
                "588f9d967643409580aa5dbe136697a1",
                "b1927a5d5bd9404c85fde75c307c63ad",
                "7e9932d922e2459bac1599938f12b272",
                "a4c40e2a8d5b46338b09d7f863049675",
                "bcf7d78c4ff94c969b2668a6edc64278"
        };
        Map<String, String> preplayParameters = new HashMap<>();
        preplayParameters.put("ad", "adtest");
        preplayParameters.put("ad.lib", "15_sec_spots");
        VerizonMediaSource verizonMediaSource = new VerizonMediaSource.Builder(assetIds)
                .assetType(VerizonMediaAssetType.ASSET)
                .parameters(preplayParameters)
                .contentProtected(false)
                .build();
        return verizonMediaSource;
    }

}
