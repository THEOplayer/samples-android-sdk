package com.theoplayer.sample.playback.cast

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.cast.framework.CastButtonFactory
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.cast.CastConfiguration
import com.theoplayer.android.api.cast.CastIntegrationFactory
import com.theoplayer.android.api.cast.CastStrategy
import com.theoplayer.android.api.cast.chromecast.Chromecast
import com.theoplayer.android.api.cast.chromecast.ChromecastConnectionCallback
import com.theoplayer.android.api.event.chromecast.ChromecastEventTypes
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.sample.common.SourceManager
import com.theoplayer.sample.playback.cast.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayer: Player
    private lateinit var theoChromecast: Chromecast

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.theoplayer.sample.common.R.style.TheoTheme_Base)
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.player

        // Keep device screen on.
        viewBinding.theoPlayerView.keepScreenOn = true

        // Enable all debug logs from THEOplayer.
        val theoDebugLogger = THEOplayerGlobal.getSharedInstance(this).logger
        theoDebugLogger.enableAllTags()

        // THEOplayer automatically adds all available integrations to the player via the
        // autoIntegrations() configuration. Here, we add the cast integration manually to
        // configure the cast strategy.
        val configuration = CastConfiguration.Builder()
            .castStrategy(CastStrategy.AUTO)
            .build()
        val castIntegration = CastIntegrationFactory.createCastIntegration(
            viewBinding.theoPlayerView, configuration
        )
        theoPlayer.addIntegration(castIntegration)

        theoChromecast = viewBinding.theoPlayerView.cast.chromecast

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer()
        configureChromecast()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.activity_player_menu, menu)
        CastButtonFactory.setUpMediaRouteButton(applicationContext, menu, R.id.castMenuItem)
        return true
    }

    private fun configureTHEOplayer() {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.fullScreenManager.isFullScreenOrientationCoupled = true

        // Allow background playback on the player to prevent Chromecast receiver from
        // pausing when the app is backgrounded.
        viewBinding.theoPlayerView.settings.setAllowBackgroundPlayback(true)

        //  Set autoplay to start video whenever player is visible.
        theoPlayer.isAutoplay = true

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.source = SourceManager.BIG_BUCK_BUNNY_HLS_WITH_CAST_METADATA

        attachEventListeners()
    }

    private fun attachEventListeners() {
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
        theoPlayer.addEventListener(PlayerEventTypes.SEEKING) {
            Log.i(TAG, "Event: SEEKING")
        }
        theoPlayer.addEventListener(PlayerEventTypes.SEEKED) {
            Log.i(TAG, "Event: SEEKED")
        }
        theoPlayer.addEventListener(PlayerEventTypes.LOADEDDATA) {
            Log.i(TAG, "Event: LOADEDDATA")
        }
        theoPlayer.addEventListener(PlayerEventTypes.LOADEDMETADATA) {
            Log.i(TAG, "Event: LOADEDMETADATA")
        }
        theoPlayer.addEventListener(PlayerEventTypes.WAITING) {
            Log.i(TAG, "Event: WAITING")
        }
        theoPlayer.addEventListener(PlayerEventTypes.ENDED) {
            Log.i(TAG, "Event: ENDED")
        }
        theoPlayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
            Log.i(TAG, "Event: ERROR, error=" + event.errorObject)
        }
    }

    private fun configureChromecast() {
        // Adding listeners to THEOplayer cast events.
        theoChromecast.addEventListener(ChromecastEventTypes.STATECHANGE) {
            Log.i(TAG, "Event: CAST_STATECHANGE, state=" + it.state)
        }
        theoChromecast.addEventListener(ChromecastEventTypes.ERROR) {
            Log.i(TAG, "Event: CAST_ERROR, error=" + it.error)
        }

        // Some applications that do not require a MediaRouteButton to control the connection
        // with the Cast Receiver device can use the below APIs instead.
//        theoChromecast.start()
//        theoChromecast.stop()
//        theoChromecast.join()
//        theoChromecast.leave()

        // Some streaming setups require casting a different stream to a Cast Receiver device
        // than the one playing on a Cast Sender device, e.g. different DRM capabilities.
        // Code below shows how to configure such a different stream to cast.
        theoChromecast.setConnectionCallback(object : ChromecastConnectionCallback {
            /**
             * Called after the player has started the connection to the receiver.
             *
             * - At this point we are trying to load the media from the sender to the receiver.
             * - Returning null will behave same as returning the provided SourceDescription.
             *
             * @param sourceDescription The current SourceDescription on the sender device. (**Nullable**)
             * @return The SourceDescription to be loaded on the receiver device. (**Nullable**)
             */
            override fun onStart(sourceDescription: SourceDescription?): SourceDescription? {
                return null
            }

            /**
             * Called after the player has stopped the connection to the receiver.
             *
             * - At this point we are trying to load the media from the receiver to the sender.
             * - Returning null will behave same as returning the provided SourceDescription.
             *
             * @param sourceDescription The current SourceDescription on the receiver device. (**Nullable**)
             * @return The SourceDescription to be loaded on the sender device. (**Nullable**)
             */
            override fun onStop(sourceDescription: SourceDescription?): SourceDescription? {
                return null
            }

            /**
             * Called after the player has joined an already existing connection to the receiver.
             *
             * - At this point it's possible to load a new media from the sender to the receiver.
             * - Returning null will not change the source on the receiver.
             *
             * @param sourceDescription The current SourceDescription on the current sender device. (**Nullable**)
             * @return The SourceDescription to be loaded on the receiver device. (**Nullable**)
             */
            override fun onJoin(sourceDescription: SourceDescription?): SourceDescription? {
                return null
            }

            /**
             * Called after the player has left the connection to the receiver.
             *
             * - At this point we are trying to load the media from the receiver to the sender.
             * - Returning null will behave same as returning the provided SourceDescription.
             *
             * @param sourceDescription The current SourceDescription on the receiver device. (**Nullable**)
             * @return The SourceDescription to be loaded on the sender device. (**Nullable**)
             */
            override fun onLeave(sourceDescription: SourceDescription?): SourceDescription? {
                return null
            }
        })
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
