package com.theoplayer.demo.remotejson;

import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.viewpager.widget.PagerAdapter;

import com.theoplayer.android.api.THEOplayerView;
import com.theoplayer.android.api.ads.Ad;
import com.theoplayer.android.api.event.EventListener;
import com.theoplayer.android.api.event.ads.AdEvent;
import com.theoplayer.android.api.event.ads.AdsEventTypes;
import com.theoplayer.android.api.event.player.EndedEvent;
import com.theoplayer.android.api.event.player.ErrorEvent;
import com.theoplayer.android.api.event.player.PauseEvent;
import com.theoplayer.android.api.event.player.PlayEvent;
import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.event.player.PlayingEvent;
import com.theoplayer.android.api.event.player.SeekingEvent;
import com.theoplayer.android.api.event.player.TimeUpdateEvent;
import com.theoplayer.android.api.event.track.mediatrack.audio.list.AudioTrackListEventTypes;
import com.theoplayer.android.api.event.track.mediatrack.video.list.AddTrackEvent;
import com.theoplayer.android.api.event.track.mediatrack.video.list.VideoTrackListEventTypes;
import com.theoplayer.android.api.event.track.texttrack.list.TextTrackListEventTypes;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.android.api.player.track.mediatrack.MediaTrack;
import com.theoplayer.android.api.player.track.texttrack.TextTrack;
import com.theoplayer.android.api.player.track.texttrack.cue.TextTrackCue;
import com.theoplayer.android.api.timerange.TimeRange;
import com.theoplayer.android.api.timerange.TimeRanges;
import com.theoplayer.demo.remotejson.databinding.ActivityPlayerBinding;

import java.util.Date;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    private static final String PLAYER_PARAM__CONFIG_JSON = "CONFIG_JSON";
    private static final String PLAYER_PARAM__SOURCE_URL = "SOURCE_URL";

    private static boolean SUPPORTS_PIP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

    private ActivityPlayerBinding viewBinding;
    private THEOplayerView theoPlayerView;
    private Player theoPlayer;

    private MutableLiveData<TimeRanges> bufferedMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Date> dateTimeMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<TimeRanges> playedMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<TimeRanges> seekableMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Ad>> currentAdsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Ad>> scheduledAdsLiveData = new MutableLiveData<>();

    private TimeUpdateEvent latestTimeUpdateEvent;

    private String playerState;
    private StringBuilder eventLog = new StringBuilder();

    public static void play(Context context, String playerConfigJson, String sourceJson) {
        Intent playIntent = new Intent(context, PlayerActivity.class);
        playIntent.putExtra(PLAYER_PARAM__CONFIG_JSON, playerConfigJson);
        playIntent.putExtra(PLAYER_PARAM__SOURCE_URL, sourceJson);
        context.startActivity(playIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);

        // Gathering THEO objects references.
        theoPlayerView = viewBinding.theoPlayerView;
        theoPlayer = theoPlayerView.getPlayer();

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configuring UI
        PagerAdapter pagerAdapter = new TabbedPagerAdapter(this);
        viewBinding.viewPager.setAdapter(pagerAdapter);
        viewBinding.viewPager.setOffscreenPageLimit(4);

        bufferedMutableLiveData.observeForever(timeRanges -> updateTimeInfo());
        dateTimeMutableLiveData.observeForever(date -> updateTimeInfo());
        playedMutableLiveData.observeForever(timeRanges -> updateTimeInfo());
        seekableMutableLiveData.observeForever(timeRanges -> updateTimeInfo());
        scheduledAdsLiveData.observeForever(ads -> updateAdsInfo());
        currentAdsLiveData.observeForever(ads -> updateAdsInfo());

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer(
                getIntent().getStringExtra(PLAYER_PARAM__CONFIG_JSON),
                getIntent().getStringExtra(PLAYER_PARAM__SOURCE_URL)
        );
    }

    private void configureTHEOplayer(String playerConfig, String sourceUrl) {
        // Preparing script to be injected into player
        String script =
                "var config = " + playerConfig + ";"
                        + "config.libraryLocation = 'theoplayer/';"
                        + "var source = " + sourceUrl + ";"
                        + "console.log(source);"
                        + "console.log(config);"
                        + "var element = THEOplayer.players[0].element;"
                        + "player = new THEOplayer.Player(element, config);"
                        + "THEOplayer.players[0].source = source;";

        theoPlayerView.evaluateJavaScript(script, value -> Log.d(TAG, "JavaScript has been evaluated."));

        theoPlayer.setAutoplay(true);

        // Adding listeners for tracks related events
        theoPlayer.getVideoTracks().addEventListener(VideoTrackListEventTypes.ADDTRACK, onAddVideoTrackEventListener);
        theoPlayer.getAudioTracks().addEventListener(AudioTrackListEventTypes.ADDTRACK, onAddAudioTrackEventListener);
        theoPlayer.getTextTracks().addEventListener(TextTrackListEventTypes.ADDTRACK, onAddTextTrackEventListener);
        theoPlayer.getTextTracks().addEventListener(TextTrackListEventTypes.ADDTRACK, onAddTextTrackEventListener);

        // Adding listeners for ads related events
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_BREAK_BEGIN, adEventEventListener);
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_BEGIN, adEventEventListener);
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_BREAK_END, adEventEventListener);
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_END, adEventEventListener);
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_ERROR, adEventEventListener);
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_FIRST_QUARTILE, adEventEventListener);
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_IMPRESSION, adEventEventListener);
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_LOADED, adEventEventListener);
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_MIDPOINT, adEventEventListener);
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_THIRD_QUARTILE, adEventEventListener);

        // Adding listeners for playback related events
        theoPlayer.addEventListener(PlayerEventTypes.TIMEUPDATE, onTimeUpdateEventLister);
        theoPlayer.addEventListener(PlayerEventTypes.PLAY, onPlayEventListener);
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING, onPlayingEventListener);
        theoPlayer.addEventListener(PlayerEventTypes.SEEKING, onSeekingEventListener);
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE, onPauseEventListener);
        theoPlayer.addEventListener(PlayerEventTypes.ENDED, onEndedEventListener);
        theoPlayer.addEventListener(PlayerEventTypes.ERROR, onErrorEventListener);
    }

    private String formatAllTracks() {
        StringBuilder sb = new StringBuilder();

        // getting information about video tracks
        if (theoPlayer.getVideoTracks() != null && theoPlayer.getVideoTracks().length() > 0) {
            sb.append(String.format(getString(R.string.videoTracksHeader)));
            for (MediaTrack videoTrack : theoPlayer.getVideoTracks()) {
                sb.append(String.format(getString(R.string.id), videoTrack.getId()));
                sb.append(String.format(getString(R.string.label), videoTrack.getLabel()));
                sb.append(String.format(getString(R.string.enabled), videoTrack.isEnabled()));
            }
            sb.append(String.format("%n"));
        }

        // getting information about audio tracks
        if (theoPlayer.getAudioTracks() != null && theoPlayer.getAudioTracks().length() > 0) {
            sb.append(String.format(getString(R.string.audioTracksHeader)));
            for (MediaTrack audioTrack : theoPlayer.getAudioTracks()) {
                sb.append(String.format(getString(R.string.id), audioTrack.getId()));
                sb.append(String.format(getString(R.string.label), audioTrack.getLabel()));
                sb.append(String.format(getString(R.string.enabled), audioTrack.isEnabled()));
            }
            sb.append(String.format("%n"));
        }

        // getting information about text tracks
        if (theoPlayer.getTextTracks() != null && theoPlayer.getTextTracks().length() > 0) {
            sb.append(String.format(getString(R.string.textTracksHeader)));
            for (TextTrack textTrack : theoPlayer.getTextTracks()) {
                sb.append(String.format(getString(R.string.id), textTrack.getId()));
                sb.append(String.format(getString(R.string.label), textTrack.getLabel()));
                if (textTrack.getActiveCues() != null && textTrack.getActiveCues().length() > 0) {
                    sb.append(String.format(getString(R.string.activeCuesHeader)));
                    for (TextTrackCue cue : textTrack.getActiveCues()) {
                        sb.append(String.format(getString(R.string.id), cue.getId()));
                        sb.append(String.format(getString(R.string.cueStartTime), cue.getStartTime()));
                        sb.append(String.format(getString(R.string.cueEndTime), cue.getEndTime()));
                    }
                }
            }
            sb.append(String.format("%n"));
        }

        return sb.toString();
    }

    private String formatTimeInfo(TimeUpdateEvent event) {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(getString(R.string.currentTime), event.getCurrentTime()));
        sb.append(String.format(getString(R.string.duration), theoPlayer.getDuration()));

        // getting current program time from livedata object
        if (dateTimeMutableLiveData.getValue() != null) {
            sb.append(String.format(getString(R.string.currentProgramTime), dateTimeMutableLiveData.getValue()));
        }

        // getting buffered ranges from livedata object
        if (bufferedMutableLiveData.getValue() != null) {
            sb.append(String.format(getString(R.string.bufferedRangesLength), bufferedMutableLiveData.getValue().length()));
            for (TimeRange r : bufferedMutableLiveData.getValue()) {
                sb.append(String.format(getString(R.string.rangeFormat), r.getStart(), r.getEnd()));
            }
        }

        // getting played ranges from livedata object
        if (playedMutableLiveData.getValue() != null) {
            sb.append(String.format(getString(R.string.playedRangesLength), playedMutableLiveData.getValue().length()));
            for (TimeRange r : playedMutableLiveData.getValue()) {
                sb.append(String.format(getString(R.string.rangeFormat), r.getStart(), r.getEnd()));
            }
        }

        // getting seekable ranges from livedata object
        if (seekableMutableLiveData.getValue() != null) {
            sb.append(String.format(getString(R.string.seekableRangesLength), seekableMutableLiveData.getValue().length()));
            for (TimeRange r : seekableMutableLiveData.getValue()) {
                sb.append(String.format(getString(R.string.rangeFormat), r.getStart(), r.getEnd()));
            }
        }
        return sb.toString();
    }

    private String formatAdsInfo() {
        StringBuilder sb = new StringBuilder();

        List<Ad> currentAds = currentAdsLiveData.getValue();
        // displaying current ads info
        if (currentAds != null && currentAds.size() > 0) {
            sb.append(String.format(getString(R.string.currentAds)));
            for (Ad currentAd : currentAds) {
                sb.append(String.format(getString(R.string.integration), currentAd.getIntegration().toString()));
                if (currentAd.getId() != null)
                    sb.append(String.format(getString(R.string.adId), currentAd.getId()));
                sb.append(String.format(getString(R.string.skipOffset), currentAd.getSkipOffset()));
                if (currentAd.getAdBreak() != null) {
                    sb.append(String.format(getString(R.string.offset), currentAd.getAdBreak().getTimeOffset()));
                    sb.append(String.format(getString(R.string.maxDuration), currentAd.getAdBreak().getMaxDuration()));
                    sb.append(String.format(getString(R.string.maxRemainingDuration), currentAd.getAdBreak().getMaxRemainingDuration()));
                }
            }
        }

        List<Ad> scheduledAds = scheduledAdsLiveData.getValue();
        // displaying scheduled ads info
        if (scheduledAds != null && scheduledAds.size() > 0) {
            sb.append(String.format(getString(R.string.scheduledAds)));
            for (Ad scheduledAd : scheduledAds) {
                sb.append(String.format(getString(R.string.integration), scheduledAd.getIntegration().toString()));
                if (scheduledAd.getId() != null)
                    sb.append(String.format(getString(R.string.adId), scheduledAd.getId()));
                sb.append(String.format(getString(R.string.skipOffset), scheduledAd.getSkipOffset()));
                if (scheduledAd.getAdBreak() != null) {
                    sb.append(String.format(getString(R.string.offset), scheduledAd.getAdBreak().getTimeOffset()));
                    sb.append(String.format(getString(R.string.maxDuration), scheduledAd.getAdBreak().getMaxDuration()));
                    sb.append(String.format(getString(R.string.maxRemainingDuration), scheduledAd.getAdBreak().getMaxRemainingDuration()));
                }
            }
        }

        return sb.toString();
    }

    private String formatStateInfo() {
        return String.format(getString(R.string.playerState), playerState);
    }

    private String formatPreloadInfo() {
        return String.format(getString(R.string.preload), theoPlayer.getPreload().getType());
    }

    private void updateAdsInfo() {
        ((TextView) findViewById(R.id.ads_output)).setText(formatAdsInfo());
    }

    private void updateTimeInfo() {
        ((TextView) findViewById(R.id.time_output)).setText(formatTimeInfo(latestTimeUpdateEvent));
    }

    private void updatePlayerStateInfo() {
        String text = String.format("%s%n%s%n%s", formatStateInfo(), formatPreloadInfo(), eventLog.toString());
        ((TextView) findViewById(R.id.state_output)).setText(text);
    }


    private final EventListener<AdEvent> adEventEventListener = event -> {
        String msg = String.format(getString(R.string.event), event.getType());
        eventLog.append(String.format("%s%n", msg));
        Log.i(TAG, msg);
        updatePlayerStateInfo();
    };

    private final EventListener<com.theoplayer.android.api.event.track.texttrack.list.AddTrackEvent> onAddTextTrackEventListener = event -> {
        String msg = String.format(getString(R.string.event), event.getType());
        eventLog.append(String.format("%s%n", msg));
        Log.i(TAG, msg);
        updatePlayerStateInfo();
        ((TextView) findViewById(R.id.tracks_output)).setText(formatAllTracks());
    };

    private final EventListener<AddTrackEvent> onAddVideoTrackEventListener = event -> {
        String msg = String.format(getString(R.string.event), event.getType());
        eventLog.append(String.format("%s%n", msg));
        Log.i(TAG, msg);
        updatePlayerStateInfo();
        ((TextView) findViewById(R.id.tracks_output)).setText(formatAllTracks());
    };

    private final EventListener<com.theoplayer.android.api.event.track.mediatrack.audio.list.AddTrackEvent> onAddAudioTrackEventListener = event -> {
        String msg = String.format(getString(R.string.event), event.getType());
        eventLog.append(String.format("%s%n", msg));
        Log.i(TAG, msg);
        updatePlayerStateInfo();
        ((TextView) findViewById(R.id.tracks_output)).setText(formatAllTracks());
    };

    private final EventListener<TimeUpdateEvent> onTimeUpdateEventLister = event -> {
        // using livedata objects to update information
        theoPlayer.requestCurrentProgramDateTime(date -> dateTimeMutableLiveData.postValue(date));
        theoPlayer.requestBuffered(tr -> bufferedMutableLiveData.postValue(tr));
        theoPlayer.requestPlayed(tr -> playedMutableLiveData.postValue(tr));
        theoPlayer.requestSeekable(tr -> seekableMutableLiveData.postValue(tr));
        theoPlayer.getAds().requestCurrentAds(ads -> currentAdsLiveData.postValue(ads));
        theoPlayer.getAds().requestScheduledAds(ads -> scheduledAdsLiveData.postValue(ads));

        latestTimeUpdateEvent = event;
    };

    private final EventListener<PlayEvent> onPlayEventListener = event -> {
        String msg = String.format(getString(R.string.eventWithTimestamp), event.getType(), event.getCurrentTime());
        eventLog.append(String.format("%s%n", msg));
        updatePlayerStateInfo();
        Log.i(TAG, msg);
    };

    private final EventListener<PlayingEvent> onPlayingEventListener = event -> {
        playerState = getString(R.string.playerStatePlaying);
        String msg = String.format(getString(R.string.eventWithTimestamp), event.getType(), event.getCurrentTime());
        eventLog.append(String.format("%s%n", msg));
        updatePlayerStateInfo();
        Log.i(TAG, msg);
    };

    private final EventListener<PauseEvent> onPauseEventListener = event -> {
        playerState = getString(R.string.playerStatePaused);
        String msg = String.format(getString(R.string.eventWithTimestamp), event.getType(), event.getCurrentTime());
        eventLog.append(String.format("%s%n", msg));
        Log.i(TAG, msg);
        updatePlayerStateInfo();
    };

    private final EventListener<EndedEvent> onEndedEventListener = event -> {
        playerState = getString(R.string.playerStateEnded);
        String msg = String.format(getString(R.string.eventWithTimestamp), event.getType(), event.getCurrentTime());
        eventLog.append(String.format("%s%n", msg));
        Log.i(TAG, msg);
        updatePlayerStateInfo();
    };

    private final EventListener<SeekingEvent> onSeekingEventListener = event -> {
        playerState = getString(R.string.playerStateSeeking);
        String msg = String.format(getString(R.string.eventWithTimestamp), event.getType(), event.getCurrentTime());
        eventLog.append(String.format("%s%n", msg));
        Log.i(TAG, msg);
        updatePlayerStateInfo();
    };

    private final EventListener<ErrorEvent> onErrorEventListener = event -> {
        String msg = String.format(getString(R.string.eventWithError), event.getType(), event.getErrorObject());
        eventLog.append(String.format("%s%n", msg));
        Log.i(TAG, msg);
        updatePlayerStateInfo();
    };

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (isInPictureInPictureMode) {
            getSupportActionBar().hide();
        } else {
            getSupportActionBar().show();
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }

    @Override
    protected void onUserLeaveHint() {
        if (SUPPORTS_PIP) {
            if (!theoPlayer.isPaused()) {
                enterPictureInPictureMode(new PictureInPictureParams.Builder().build());
            }
        } else {
            SpannableString toastMessage = SpannableString.valueOf(getString(R.string.pipNotSupported));
            toastMessage.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, toastMessage.length(), 0);
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (!SUPPORTS_PIP || !isInPictureInPictureMode()) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(viewBinding.mainConstraintLayout);

            int theoPlayerViewId = viewBinding.theoPlayerView.getId();
            int pagerViewId = viewBinding.viewPager.getId();

            // setting layout for portrait without redrawing
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                constraintSet.clear(theoPlayerViewId, ConstraintSet.START);
                constraintSet.clear(theoPlayerViewId, ConstraintSet.END);
                constraintSet.clear(theoPlayerViewId, ConstraintSet.BOTTOM);
                constraintSet.clear(theoPlayerViewId, ConstraintSet.TOP);

                constraintSet.clear(pagerViewId, ConstraintSet.START);
                constraintSet.clear(pagerViewId, ConstraintSet.BOTTOM);
                constraintSet.clear(pagerViewId, ConstraintSet.END);
                constraintSet.clear(pagerViewId, ConstraintSet.TOP);

                constraintSet.connect(theoPlayerViewId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.connect(theoPlayerViewId, ConstraintSet.END, pagerViewId, ConstraintSet.START);
                constraintSet.connect(theoPlayerViewId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                constraintSet.connect(theoPlayerViewId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);

                constraintSet.connect(pagerViewId, ConstraintSet.START, theoPlayerViewId, ConstraintSet.END);
                constraintSet.connect(pagerViewId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                constraintSet.connect(pagerViewId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(pagerViewId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);

            } else {
                // setting layout for portrait without redrawing
                constraintSet.clear(theoPlayerViewId, ConstraintSet.START);
                constraintSet.clear(theoPlayerViewId, ConstraintSet.END);
                constraintSet.clear(theoPlayerViewId, ConstraintSet.BOTTOM);
                constraintSet.clear(theoPlayerViewId, ConstraintSet.TOP);

                constraintSet.clear(pagerViewId, ConstraintSet.START);
                constraintSet.clear(pagerViewId, ConstraintSet.BOTTOM);
                constraintSet.clear(pagerViewId, ConstraintSet.END);
                constraintSet.clear(pagerViewId, ConstraintSet.TOP);

                constraintSet.setDimensionRatio(theoPlayerViewId, "H,16:9");

                constraintSet.connect(theoPlayerViewId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.connect(theoPlayerViewId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(theoPlayerViewId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);

                constraintSet.connect(pagerViewId, ConstraintSet.START, theoPlayerViewId, ConstraintSet.START);
                constraintSet.connect(pagerViewId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                constraintSet.connect(pagerViewId, ConstraintSet.END, theoPlayerViewId, ConstraintSet.END);
                constraintSet.connect(pagerViewId, ConstraintSet.TOP, theoPlayerViewId, ConstraintSet.BOTTOM);
            }
            constraintSet.applyTo(viewBinding.mainConstraintLayout);
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
            viewBinding.theoPlayerView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        theoPlayerView.onDestroy();
    }

}

