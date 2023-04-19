package com.theoplayer.demo.remotejson

import android.annotation.TargetApi
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.PagerAdapter
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.ads.Ad
import com.theoplayer.android.api.event.ads.AdEvent
import com.theoplayer.android.api.event.ads.AdsEventTypes
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.event.track.mediatrack.audio.list.AudioTrackListEventTypes
import com.theoplayer.android.api.event.track.mediatrack.video.list.VideoTrackListEventTypes
import com.theoplayer.android.api.event.track.texttrack.list.AddTrackEvent
import com.theoplayer.android.api.event.track.texttrack.list.TextTrackListEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.timerange.TimeRanges
import com.theoplayer.demo.remotejson.databinding.ActivityPlayerBinding
import java.util.*

class PlayerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayerView: THEOplayerView
    private lateinit var theoPlayer: Player
    private val bufferedMutableLiveData = MutableLiveData<TimeRanges?>()
    private val dateTimeMutableLiveData = MutableLiveData<Date?>()
    private val playedMutableLiveData = MutableLiveData<TimeRanges?>()
    private val seekableMutableLiveData = MutableLiveData<TimeRanges?>()
    private val currentAdsLiveData = MutableLiveData<List<Ad>>()
    private val scheduledAdsLiveData = MutableLiveData<List<Ad>>()
    private var latestTimeUpdateEvent: TimeUpdateEvent? = null
    private var playerState: String? = null
    private val eventLog = StringBuilder()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        // Gathering THEO objects references.
        theoPlayerView = viewBinding.theoPlayerView
        theoPlayer = theoPlayerView.player

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Configuring UI
        val pagerAdapter: PagerAdapter = TabbedPagerAdapter(this)
        viewBinding.viewPager.adapter = pagerAdapter
        viewBinding.viewPager.offscreenPageLimit = 4
        bufferedMutableLiveData.observeForever { updateTimeInfo() }
        dateTimeMutableLiveData.observeForever { updateTimeInfo() }
        playedMutableLiveData.observeForever { updateTimeInfo() }
        seekableMutableLiveData.observeForever { updateTimeInfo() }
        scheduledAdsLiveData.observeForever { updateAdsInfo() }
        currentAdsLiveData.observeForever { updateAdsInfo() }

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer(
            intent.getStringExtra(PLAYER_PARAM__CONFIG_JSON),
            intent.getStringExtra(PLAYER_PARAM__SOURCE_URL)
        )
    }

    private fun configureTHEOplayer(playerConfig: String?, sourceUrl: String?) {
        // Preparing script to be injected into player
        val script = ("var config = " + playerConfig + ";"
                + "config.libraryLocation = 'theoplayer/';"
                + "var source = " + sourceUrl + ";"
                + "console.log(source);"
                + "console.log(config);"
                + "var element = THEOplayer.players[0].element;"
                + "player = new THEOplayer.Player(element, config);"
                + "THEOplayer.players[0].source = source;")
        theoPlayerView.evaluateJavaScript(script) {
            Log.d(TAG, "JavaScript has been evaluated.")
        }
        theoPlayer.isAutoplay = true

        // Adding listeners for tracks related events
        theoPlayer.videoTracks.addEventListener(
            VideoTrackListEventTypes.ADDTRACK,
            onAddVideoTrackEventListener
        )
        theoPlayer.audioTracks.addEventListener(
            AudioTrackListEventTypes.ADDTRACK,
            onAddAudioTrackEventListener
        )
        theoPlayer.textTracks.addEventListener(
            TextTrackListEventTypes.ADDTRACK,
            onAddTextTrackEventListener
        )
        theoPlayer.textTracks.addEventListener(
            TextTrackListEventTypes.ADDTRACK,
            onAddTextTrackEventListener
        )

        // Adding listeners for ads related events
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_BREAK_BEGIN, adEventEventListener)
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_BEGIN, adEventEventListener)
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_BREAK_END, adEventEventListener)
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_END, adEventEventListener)
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_ERROR, adEventEventListener)
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_FIRST_QUARTILE, adEventEventListener)
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_IMPRESSION, adEventEventListener)
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_LOADED, adEventEventListener)
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_MIDPOINT, adEventEventListener)
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_THIRD_QUARTILE, adEventEventListener)

        // Adding listeners for playback related events
        theoPlayer.addEventListener(PlayerEventTypes.TIMEUPDATE, onTimeUpdateEventLister)
        theoPlayer.addEventListener(PlayerEventTypes.PLAY, onPlayEventListener)
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING, onPlayingEventListener)
        theoPlayer.addEventListener(PlayerEventTypes.SEEKING, onSeekingEventListener)
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE, onPauseEventListener)
        theoPlayer.addEventListener(PlayerEventTypes.ENDED, onEndedEventListener)
        theoPlayer.addEventListener(PlayerEventTypes.ERROR, onErrorEventListener)
    }

    private fun formatAllTracks(): String {
        val sb = StringBuilder()

        // getting information about video tracks
        if (theoPlayer.videoTracks.length() > 0) {
            sb.append(String.format(getString(R.string.videoTracksHeader)))
            for (videoTrack in theoPlayer.videoTracks) {
                sb.append(String.format(getString(R.string.id), videoTrack.id))
                sb.append(String.format(getString(R.string.label), videoTrack.label))
                sb.append(String.format(getString(R.string.enabled), videoTrack.isEnabled))
            }
            sb.append(String.format("%n"))
        }

        // getting information about audio tracks
        if (theoPlayer.audioTracks.length() > 0) {
            sb.append(String.format(getString(R.string.audioTracksHeader)))
            for (audioTrack in theoPlayer.audioTracks) {
                sb.append(String.format(getString(R.string.id), audioTrack.id))
                sb.append(String.format(getString(R.string.label), audioTrack.label))
                sb.append(String.format(getString(R.string.enabled), audioTrack.isEnabled))
            }
            sb.append(String.format("%n"))
        }

        // getting information about text tracks
        if (theoPlayer.textTracks.length() > 0) {
            sb.append(String.format(getString(R.string.textTracksHeader)))
            for (textTrack in theoPlayer.textTracks) {
                sb.append(String.format(getString(R.string.id), textTrack.id))
                sb.append(String.format(getString(R.string.label), textTrack.label))
                if (textTrack.activeCues != null && textTrack.activeCues!!.length() > 0) {
                    sb.append(String.format(getString(R.string.activeCuesHeader)))
                    for (cue in textTrack.activeCues!!) {
                        sb.append(String.format(getString(R.string.id), cue.id))
                        sb.append(String.format(getString(R.string.cueStartTime), cue.startTime))
                        sb.append(String.format(getString(R.string.cueEndTime), cue.endTime))
                    }
                }
            }
            sb.append(String.format("%n"))
        }
        return sb.toString()
    }

    private fun formatTimeInfo(event: TimeUpdateEvent?): String {
        val sb = StringBuilder()
        sb.append(String.format(getString(R.string.currentTime), event?.currentTime))
        sb.append(String.format(getString(R.string.duration), theoPlayer.duration))

        // getting current program time from livedata object
        if (dateTimeMutableLiveData.value != null) {
            sb.append(
                String.format(
                    getString(R.string.currentProgramTime),
                    dateTimeMutableLiveData.value
                )
            )
        }

        // getting buffered ranges from livedata object
        if (bufferedMutableLiveData.value != null) {
            sb.append(
                String.format(
                    getString(R.string.bufferedRangesLength), bufferedMutableLiveData.value!!
                        .length()
                )
            )
            for (r in bufferedMutableLiveData.value!!) {
                sb.append(String.format(getString(R.string.rangeFormat), r.start, r.end))
            }
        }

        // getting played ranges from livedata object
        if (playedMutableLiveData.value != null) {
            sb.append(
                String.format(
                    getString(R.string.playedRangesLength), playedMutableLiveData.value!!
                        .length()
                )
            )
            for (r in playedMutableLiveData.value!!) {
                sb.append(String.format(getString(R.string.rangeFormat), r.start, r.end))
            }
        }

        // getting seekable ranges from livedata object
        if (seekableMutableLiveData.value != null) {
            sb.append(
                String.format(
                    getString(R.string.seekableRangesLength), seekableMutableLiveData.value!!
                        .length()
                )
            )
            for (r in seekableMutableLiveData.value!!) {
                sb.append(String.format(getString(R.string.rangeFormat), r.start, r.end))
            }
        }
        return sb.toString()
    }

    private fun formatAdsInfo(): String {
        val sb = StringBuilder()
        val currentAds = currentAdsLiveData.value
        // displaying current ads info
        if (!currentAds.isNullOrEmpty()) {
            sb.append(String.format(getString(R.string.currentAds)))
            for (currentAd in currentAds) {
                sb.append(
                    String.format(
                        getString(R.string.integration),
                        currentAd.integration.toString()
                    )
                )
                sb.append(String.format(getString(R.string.adId), currentAd.id))
                sb.append(String.format(getString(R.string.skipOffset), currentAd.skipOffset))
                if (currentAd.adBreak != null) {
                    sb.append(
                        String.format(
                            getString(R.string.offset), currentAd.adBreak!!
                                .timeOffset
                        )
                    )
                    sb.append(
                        String.format(
                            getString(R.string.maxDuration), currentAd.adBreak!!
                                .maxDuration
                        )
                    )
                    sb.append(
                        String.format(
                            getString(R.string.maxRemainingDuration), currentAd.adBreak!!
                                .maxRemainingDuration
                        )
                    )
                }
            }
        }
        val scheduledAds = scheduledAdsLiveData.value
        // displaying scheduled ads info
        if (!scheduledAds.isNullOrEmpty()) {
            sb.append(String.format(getString(R.string.scheduledAds)))
            for (scheduledAd in scheduledAds) {
                sb.append(
                    String.format(
                        getString(R.string.integration),
                        scheduledAd.integration.toString()
                    )
                )
                sb.append(String.format(getString(R.string.adId), scheduledAd.id))
                sb.append(String.format(getString(R.string.skipOffset), scheduledAd.skipOffset))
                if (scheduledAd.adBreak != null) {
                    sb.append(
                        String.format(
                            getString(R.string.offset), scheduledAd.adBreak!!
                                .timeOffset
                        )
                    )
                    sb.append(
                        String.format(
                            getString(R.string.maxDuration), scheduledAd.adBreak!!
                                .maxDuration
                        )
                    )
                    sb.append(
                        String.format(
                            getString(R.string.maxRemainingDuration), scheduledAd.adBreak!!
                                .maxRemainingDuration
                        )
                    )
                }
            }
        }
        return sb.toString()
    }

    private fun formatStateInfo(): String {
        return String.format(getString(R.string.playerState), playerState)
    }

    private fun formatPreloadInfo(): String {
        return String.format(getString(R.string.preload), theoPlayer.preload.type)
    }

    private fun updateAdsInfo() {
        (findViewById<View>(R.id.ads_output) as TextView).text = formatAdsInfo()
    }

    private fun updateTimeInfo() {
        (findViewById<View>(R.id.time_output) as TextView).text =
            formatTimeInfo(latestTimeUpdateEvent)
    }

    private fun updatePlayerStateInfo() {
        val text =
            String.format("%s%n%s%n%s", formatStateInfo(), formatPreloadInfo(), eventLog.toString())
        (findViewById<View>(R.id.state_output) as TextView).text =
            text
    }

    private val adEventEventListener = { event: AdEvent<*> ->
        val msg = String.format(getString(R.string.event), event.type)
        eventLog.append(String.format("%s%n", msg))
        Log.i(TAG, msg)
        updatePlayerStateInfo()
    }
    private val onAddTextTrackEventListener = { event: AddTrackEvent ->
        val msg = String.format(getString(R.string.event), event.type)
        eventLog.append(String.format("%s%n", msg))
        Log.i(TAG, msg)
        updatePlayerStateInfo()
        (findViewById<View>(R.id.tracks_output) as TextView).text = formatAllTracks()
    }
    private val onAddVideoTrackEventListener = { event: com.theoplayer.android.api.event.track.mediatrack.video.list.AddTrackEvent ->
            val msg = String.format(
                getString(
                    R.string.event
                ), event.type
            )
            eventLog.append(String.format("%s%n", msg))
            Log.i(TAG, msg)
            updatePlayerStateInfo()
            (findViewById<View>(R.id.tracks_output) as TextView).text = formatAllTracks()
        }
    private val onAddAudioTrackEventListener = { event: com.theoplayer.android.api.event.track.mediatrack.audio.list.AddTrackEvent ->
            val msg = String.format(
                getString(
                    R.string.event
                ), event.type
            )
            eventLog.append(String.format("%s%n", msg))
            Log.i(TAG, msg)
            updatePlayerStateInfo()
            (findViewById<View>(R.id.tracks_output) as TextView).text = formatAllTracks()
        }
    private val onTimeUpdateEventLister = { event: TimeUpdateEvent? ->
        // using livedata objects to update information
        theoPlayer.requestCurrentProgramDateTime { date: Date? ->
            dateTimeMutableLiveData.postValue(
                date
            )
        }
        theoPlayer.requestBuffered { tr: TimeRanges? -> bufferedMutableLiveData.postValue(tr) }
        theoPlayer.requestPlayed { tr: TimeRanges? -> playedMutableLiveData.postValue(tr) }
        theoPlayer.requestSeekable { tr: TimeRanges? -> seekableMutableLiveData.postValue(tr) }
        theoPlayer.ads.requestCurrentAds { ads: List<Ad> -> currentAdsLiveData.postValue(ads) }
        theoPlayer.ads.requestScheduledAds { ads: List<Ad> -> scheduledAdsLiveData.postValue(ads) }
        latestTimeUpdateEvent = event
    }
    private val onPlayEventListener: (PlayEvent) -> Unit = { event: PlayEvent ->
        val msg = String.format(getString(R.string.eventWithTimestamp), event.type, event.currentTime)
        eventLog.append(String.format("%s%n", msg))
        updatePlayerStateInfo()
        Log.i(TAG, msg)
    }
    private val onPlayingEventListener: (PlayingEvent) -> Unit = { event: PlayingEvent ->
        playerState = getString(R.string.playerStatePlaying)
        val msg = String.format(getString(R.string.eventWithTimestamp), event.type, event.currentTime)
        eventLog.append(String.format("%s%n", msg))
        updatePlayerStateInfo()
        Log.i(TAG, msg)
    }
    private val onPauseEventListener = { event: PauseEvent ->
        playerState = getString(R.string.playerStatePaused)
        val msg = String.format(getString(R.string.eventWithTimestamp), event.type, event.currentTime)
        eventLog.append(String.format("%s%n", msg))
        Log.i(TAG, msg)
        updatePlayerStateInfo()
    }
    private val onEndedEventListener = { event: EndedEvent ->
        playerState = getString(R.string.playerStateEnded)
        val msg = String.format(getString(R.string.eventWithTimestamp), event.type, event.currentTime)
        eventLog.append(String.format("%s%n", msg))
        Log.i(TAG, msg)
        updatePlayerStateInfo()
    }
    private val onSeekingEventListener = { event: SeekingEvent ->
        playerState = getString(R.string.playerStateSeeking)
        val msg = String.format(getString(R.string.eventWithTimestamp), event.type, event.currentTime)
        eventLog.append(String.format("%s%n", msg))
        Log.i(TAG, msg)
        updatePlayerStateInfo()
    }
    private val onErrorEventListener = { event: ErrorEvent ->
        val msg = String.format(getString(R.string.eventWithError), event.type, event.errorObject)
        eventLog.append(String.format("%s%n", msg))
        Log.i(TAG, msg)
        updatePlayerStateInfo()
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        if (isInPictureInPictureMode) {
            supportActionBar?.hide()
        } else {
            supportActionBar?.show()
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }

    override fun onUserLeaveHint() {
        if (SUPPORTS_PIP) {
            if (!theoPlayer.isPaused) {
                enterPictureInPictureMode(PictureInPictureParams.Builder().build())
            }
        } else {
            val toastMessage = SpannableString.valueOf(getString(R.string.pipNotSupported))
            toastMessage.setSpan(
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0,
                toastMessage.length,
                0
            )
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (!SUPPORTS_PIP || !isInPictureInPictureMode) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(viewBinding.mainConstraintLayout)
            val theoPlayerViewId = viewBinding.theoPlayerView.id
            val pagerViewId = viewBinding.viewPager.id

            // setting layout for portrait without redrawing
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                constraintSet.clear(theoPlayerViewId, ConstraintSet.START)
                constraintSet.clear(theoPlayerViewId, ConstraintSet.END)
                constraintSet.clear(theoPlayerViewId, ConstraintSet.BOTTOM)
                constraintSet.clear(theoPlayerViewId, ConstraintSet.TOP)
                constraintSet.clear(pagerViewId, ConstraintSet.START)
                constraintSet.clear(pagerViewId, ConstraintSet.BOTTOM)
                constraintSet.clear(pagerViewId, ConstraintSet.END)
                constraintSet.clear(pagerViewId, ConstraintSet.TOP)
                constraintSet.connect(
                    theoPlayerViewId,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START
                )
                constraintSet.connect(
                    theoPlayerViewId,
                    ConstraintSet.END,
                    pagerViewId,
                    ConstraintSet.START
                )
                constraintSet.connect(
                    theoPlayerViewId,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
                constraintSet.connect(
                    theoPlayerViewId,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    pagerViewId,
                    ConstraintSet.START,
                    theoPlayerViewId,
                    ConstraintSet.END
                )
                constraintSet.connect(
                    pagerViewId,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
                constraintSet.connect(
                    pagerViewId,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END
                )
                constraintSet.connect(
                    pagerViewId,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
            } else {
                // setting layout for portrait without redrawing
                constraintSet.clear(theoPlayerViewId, ConstraintSet.START)
                constraintSet.clear(theoPlayerViewId, ConstraintSet.END)
                constraintSet.clear(theoPlayerViewId, ConstraintSet.BOTTOM)
                constraintSet.clear(theoPlayerViewId, ConstraintSet.TOP)
                constraintSet.clear(pagerViewId, ConstraintSet.START)
                constraintSet.clear(pagerViewId, ConstraintSet.BOTTOM)
                constraintSet.clear(pagerViewId, ConstraintSet.END)
                constraintSet.clear(pagerViewId, ConstraintSet.TOP)
                constraintSet.setDimensionRatio(theoPlayerViewId, "H,16:9")
                constraintSet.connect(
                    theoPlayerViewId,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START
                )
                constraintSet.connect(
                    theoPlayerViewId,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END
                )
                constraintSet.connect(
                    theoPlayerViewId,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    pagerViewId,
                    ConstraintSet.START,
                    theoPlayerViewId,
                    ConstraintSet.START
                )
                constraintSet.connect(
                    pagerViewId,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
                constraintSet.connect(
                    pagerViewId,
                    ConstraintSet.END,
                    theoPlayerViewId,
                    ConstraintSet.END
                )
                constraintSet.connect(
                    pagerViewId,
                    ConstraintSet.TOP,
                    theoPlayerViewId,
                    ConstraintSet.BOTTOM
                )
            }
            constraintSet.applyTo(viewBinding.mainConstraintLayout)
        }
    }

    // In order to work properly and in sync with the activity lifecycle changes (e.g. device
    // is rotated, new activity is started or app is moved to background) we need to call
    // the "onResume", "onPause" and "onDestroy" methods of the THEOplayerView when the matching
    // activity methods are called.
    override fun onPause() {
        super.onPause()
        if (SUPPORTS_PIP && !isInPictureInPictureMode) {
            viewBinding.theoPlayerView.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (SUPPORTS_PIP && !isInPictureInPictureMode) {
            viewBinding.theoPlayerView.onResume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        theoPlayerView.onDestroy()
    }

    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
        private const val PLAYER_PARAM__CONFIG_JSON = "CONFIG_JSON"
        private const val PLAYER_PARAM__SOURCE_URL = "SOURCE_URL"
        private val SUPPORTS_PIP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        fun play(context: Context, playerConfigJson: String?, sourceJson: String?) {
            val playIntent = Intent(context, PlayerActivity::class.java)
            playIntent.putExtra(PLAYER_PARAM__CONFIG_JSON, playerConfigJson)
            playIntent.putExtra(PLAYER_PARAM__SOURCE_URL, sourceJson)
            context.startActivity(playIntent)
        }
    }
}