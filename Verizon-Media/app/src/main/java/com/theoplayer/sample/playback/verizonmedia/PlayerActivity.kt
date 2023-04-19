package com.theoplayer.sample.playback.verizonmedia

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.event.verizonmedia.*
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.verizonmedia.VerizonMediaAssetType
import com.theoplayer.android.api.source.verizonmedia.VerizonMediaPingConfiguration
import com.theoplayer.android.api.source.verizonmedia.VerizonMediaSource
import com.theoplayer.sample.playback.verizonmedia.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayer: Player
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TheoTheme_Base)
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.player

//         THEOplayerView is created through the layout in this project.
//         You could leverage the code below if you'd be creating the THEOplayerView programmatically

//        val verizonMediaUiConfiguration = VerizonMediaUiConfiguration.Builder()
//            .assetMarkers(true) // optional; defaults to true
//            .adBreakMarkers(true) // optional; defaults to true
//            .contentNotification(true) // optional; defaults to true
//            .adNotification(true) // optional; defaults to true
//            .build()
//        val verizonMediaConfiguration = VerizonMediaConfiguration.Builder()
//            .defaultSkipOffset(15) // optional; defaults to -1 (=unskippable)
//            .skippedAdStrategy(SkippedAdStrategy.PLAY_ALL) // optional; defaults to PLAY_NONE
//            .ui(verizonMediaUiConfiguration)
//            .build()
//        val theoplayerConfig = THEOplayerConfig.Builder()
//            .verizonMediaConfiguration(verizonMediaConfiguration)
//            .build()
//        val theoplayerView = THEOplayerView(this, theoplayerConfig)

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer()
    }

    private fun configureTHEOplayer() {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.settings.isFullScreenOrientationCoupled = true

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
//        val verizonMediaSource = createMultiAssetWidevineStream()
//        val verizonMediaSource = createLiveFairPlayStreamWithAds()
        val verizonMediaSource = createHLSStreamWithAds()
        val sourceDescription =
            SourceDescription.Builder.sourceDescription(verizonMediaSource).build()
        attachEventListeners(theoPlayer)

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.source = sourceDescription
    }

    protected fun attachEventListeners(theoplayer: Player) {
        // Adding listeners to THEOplayer basic playback events.
        theoplayer.addEventListener(PlayerEventTypes.PLAY) { event: PlayEvent? ->
            Log.i(TAG, "Event: PLAY")
        }
        theoplayer.addEventListener(PlayerEventTypes.PLAYING) { event: PlayingEvent? ->
            Log.i(TAG, "Event: PLAYING")
        }
        theoplayer.addEventListener(PlayerEventTypes.PAUSE) { event: PauseEvent? ->
            Log.i(TAG, "Event: PAUSE")
        }
        theoplayer.addEventListener(PlayerEventTypes.ENDED) { event: EndedEvent? ->
            Log.i(TAG, "Event: ENDED")
        }
        theoplayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
            Log.i(TAG, "Event: ERROR, error=" + event.errorObject)
        }

        // Adding listeners to THEOplayer Verizon Media events.
        theoplayer.verizonMedia?.addEventListener(VerizonMediaEventTypes.PREPLAYRESPONSE) { event: VerizonMediaPreplayResponseEvent? ->
            Log.i(TAG, "Event: PREPLAYRESPONSE")
        }
        theoplayer.verizonMedia?.addEventListener(VerizonMediaEventTypes.PINGRESPONSE) { event: VerizonMediaPingResponseEvent? ->
            Log.i(TAG, "Event: PINGRESPONSE")
        }
        theoplayer.verizonMedia?.addEventListener(VerizonMediaEventTypes.PINGERROR) { event: VerizonMediaPingErrorEvent? ->
            Log.i(TAG, "Event: PINGERROR")
        }
        val attachAdBreakEventListeners: EventListener<in VerizonMediaAdBreakListEvent> =
            EventListener { event ->
                Log.i(TAG, "Event: ADDADBREAK")
                val adBreak = event.adBreak
                val ads = event.adBreak.ads
                for (i in 0 until ads!!.length()) {
                    val ad = ads.getItem(i)
                    ad.addEventListener(VerizonMediaAdEventTypes.AD_BEGIN) {
                        Log.i(TAG, "Event: ADBEGIN")
                    }
                    ad.addEventListener(VerizonMediaAdEventTypes.AD_END) {
                        Log.i(TAG, "Event: ADBEGIN")
                    }
                }
                adBreak.addEventListener(VerizonMediaAdBreakEventTypes.ADBREAK_BEGIN) {
                    Log.i(TAG, "Event: ADBREAKBEGIN")
                }
                adBreak.addEventListener(VerizonMediaAdBreakEventTypes.ADBREAK_END) {
                    Log.i(TAG, "Event: ADBREAKEND")
                }
                adBreak.addEventListener(VerizonMediaAdBreakEventTypes.ADBREAK_SKIP) {
                    Log.i(TAG, "Event: ADBREAKSKIP")
                }
                adBreak.addEventListener(VerizonMediaAdBreakEventTypes.UPDATE_ADBREAK) {
                    Log.i(TAG, "Event: UPDATEADBREAK")
                }
            }
        theoplayer.verizonMedia!!.ads.adBreaks.addEventListener(
            VerizonMediaAdBreakListEventTypes.ADD_ADBREAK,
            attachAdBreakEventListeners
        )
        theoplayer.verizonMedia!!.ads.adBreaks.addEventListener(VerizonMediaAdBreakListEventTypes.REMOVE_ADBREAK) {
            Log.i(TAG, "Event: REMOVEADBREAK")
        }
    }

    // In order to work properly and in sync with the activity lifecycle changes (e.g. device
    // is rotated, new activity is started or app is moved to background) we need to call
    // the "onResume", "onPause" and "onDestroy" methods of the THEOplayerView when the matching
    // activity methods are called.
    override fun onPause() {
        super.onPause()
        viewBinding.theoPlayerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        viewBinding.theoPlayerView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding.theoPlayerView.onDestroy()
    }

    /*
     The functions below will create a VerizonMediaSource based
     on the assets available at https://cdn.theoplayer.com/demos/verizon-media/index.html.
     */
    private fun createMultiAssetWidevineStream(): VerizonMediaSource {
        val assetIds = arrayOf(
            "e973a509e67241e3aa368730130a104d",
            "e70a708265b94a3fa6716666994d877d"
        )
        return VerizonMediaSource.Builder(assetIds)
            .assetType(VerizonMediaAssetType.ASSET)
            .contentProtected(true)
            .build()
    }

    private fun createLiveFairPlayStreamWithAds(): VerizonMediaSource {
        val orderedPreplayParameters =
            LinkedHashMap<String, String>()
        orderedPreplayParameters["ad"] = "cleardashnew"
        val pingConfiguration =
            VerizonMediaPingConfiguration.Builder()
                .linearAdData(true) // Defaults to true if VerizonMediaAssetType is "CHANNEL" or "EVENT", otherwise false.
                .adImpressions(false) // Defaults to false
                .freeWheelVideoViews(true) // Defaults to false
                .build()
        return VerizonMediaSource.Builder("3c367669a83b4cdab20cceefac253684")
            .assetType(VerizonMediaAssetType.CHANNEL)
            .orderedParameters(orderedPreplayParameters)
            .ping(pingConfiguration)
            .contentProtected(true)
            .build()
    }

    private fun createHLSStreamWithAds(): VerizonMediaSource {
        val assetIds = arrayOf(
            "41afc04d34ad4cbd855db52402ef210e",
            "c6b61470c27d44c4842346980ec2c7bd",
            "588f9d967643409580aa5dbe136697a1",
            "b1927a5d5bd9404c85fde75c307c63ad",
            "7e9932d922e2459bac1599938f12b272",
            "a4c40e2a8d5b46338b09d7f863049675",
            "bcf7d78c4ff94c969b2668a6edc64278"
        )
        val orderedPreplayParameters =
            LinkedHashMap<String, String>()
        orderedPreplayParameters["ad"] = "adtest"
        orderedPreplayParameters["ad.lib"] = "15_sec_spots"
        return VerizonMediaSource.Builder(assetIds)
            .assetType(VerizonMediaAssetType.ASSET)
            .orderedParameters(orderedPreplayParameters)
            .contentProtected(false)
            .build()
    }

    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
    }
}