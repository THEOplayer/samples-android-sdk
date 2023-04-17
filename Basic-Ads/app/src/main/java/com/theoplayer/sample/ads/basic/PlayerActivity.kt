package com.theoplayer.sample.ads.basic

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.event.ads.AdBeginEvent
import com.theoplayer.android.api.event.ads.AdEndEvent
import com.theoplayer.android.api.event.ads.AdErrorEvent
import com.theoplayer.android.api.event.ads.AdsEventTypes
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.addescription.THEOplayerAdDescription
import com.theoplayer.sample.ads.basic.databinding.ActivityPlayerBinding

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

        // Creating a TypedSource builder that defines the location of a single stream source.
        val typedSource = TypedSource.Builder(getString(R.string.defaultSourceUrl))

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        val sourceDescription = SourceDescription.Builder(typedSource.build())
            .poster(getString(R.string.defaultPosterUrl))

        // VMAP standard defines ads playlist and contains ads time offset definitions. To avoid
        // overlapping, VMAP ads are defined separately.
        if (resources.getBoolean(R.bool.loadVmapAds)) {
            sourceDescription.ads( // Inserting linear pre-roll, mid-roll (15s) and post-roll ads defined with VMAP standard.
                THEOplayerAdDescription.Builder(getString(R.string.defaultVmapAdUrl))
                    .build()
            )
        } else {
            sourceDescription.ads( // Inserting linear pre-roll ad defined with VAST standard.
                THEOplayerAdDescription.Builder(getString(R.string.defaultVastLinearPreRollAdUrl))
                    .timeOffset("start")
                    .build(),  // Inserting nonlinear ad defined with VAST standard.
                THEOplayerAdDescription.Builder(getString(R.string.defaultVastNonLinearAdUrl))
                    .timeOffset("start")
                    .build(),  // Inserting skippable linear mid-roll (15s) ad defined with VAST standard.
                THEOplayerAdDescription.Builder(getString(R.string.defaultVastLinearMidRollAdUrl))
                    .timeOffset("15")
                    .skipOffset("5")
                    .build()
            )
        }

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.source = sourceDescription.build()

        // Adding listeners to THEOplayer basic playback events.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY) {
            Log.i(TAG, "Event: PLAY")
        }
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING) {
            Log.i(TAG, "Event: PLAYING")
        }
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE) {
            Log.i(TAG, "Event: PAUSE")
        }
        theoPlayer.addEventListener(PlayerEventTypes.ENDED) {
            Log.i(TAG, "Event: ENDED")
        }
        theoPlayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
            Log.i(TAG, "Event: ERROR, error=" + event.errorObject)
        }

        // Adding listeners to THEOplayer basic ad events.
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_BEGIN) { event: AdBeginEvent ->
            Log.i(TAG, "Event: AD_BEGIN, ad=" + event.ad)
        }
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_END) { event: AdEndEvent ->
            Log.i(TAG, "Event: AD_END, ad=" + event.ad)
        }
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_ERROR) { event: AdErrorEvent ->
            Log.i(TAG, "Event: AD_ERROR, error=" + event.error)
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

    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
    }
}