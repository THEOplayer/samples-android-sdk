package com.theoplayer.sample.ads.custom

import android.content.Context
import android.content.Intent
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
import com.theoplayer.android.api.source.addescription.GoogleImaAdDescription
import com.theoplayer.sample.ads.custom.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayer: Player
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.player

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Configuring THEOplayer playback with parameters from intent.
        configureTHEOplayer(
            intent.getStringExtra(PLAYER_PARAM__SOURCE_URL)!!,
            intent.getStringExtra(PLAYER_PARAM__AD_URL)!!,
            intent.getStringExtra(PLAYER_PARAM__AD_TIME_OFFSET)!!
        )
    }

    private fun configureTHEOplayer(sourceUrl: String, adUrl: String, adTimeOffset: String) {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.settings.isFullScreenOrientationCoupled = true

        // Creating a TypedSource builder that defines the location of a single stream source.
        val typedSource = TypedSource.Builder(sourceUrl)

        // Creating a GoogleImaAdDescription builder that defines the location of an ad and its
        // time offset.
        val adDescription = GoogleImaAdDescription.Builder(adUrl)
            .timeOffset(adTimeOffset)

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source including ads configuration.
        val sourceDescription = SourceDescription.Builder(typedSource.build())
            .ads(adDescription.build())

        // Configuring THEOplayer with defined SourceDescription object to be played automatically.
        theoPlayer.source = sourceDescription.build()
        theoPlayer.isAutoplay = true

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
        private const val PLAYER_PARAM__SOURCE_URL = "SOURCE_URL"
        private const val PLAYER_PARAM__AD_URL = "AD_URL"
        private const val PLAYER_PARAM__AD_TIME_OFFSET = "AD_TIME_OFFSET"
        @JvmStatic
        fun play(context: Context, sourceUrl: String?, adUrl: String?, adTimeOffset: String?) {
            val playIntent = Intent(context, PlayerActivity::class.java)
            playIntent.putExtra(PLAYER_PARAM__SOURCE_URL, sourceUrl)
            playIntent.putExtra(PLAYER_PARAM__AD_URL, adUrl)
            playIntent.putExtra(PLAYER_PARAM__AD_TIME_OFFSET, adTimeOffset)
            context.startActivity(playIntent)
        }
    }
}