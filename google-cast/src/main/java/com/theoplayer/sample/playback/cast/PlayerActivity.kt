package com.theoplayer.sample.playback.cast

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.cast.CastConfiguration
import com.theoplayer.android.api.cast.CastIntegrationFactory
import com.theoplayer.android.api.cast.CastStrategy
import com.theoplayer.android.api.cast.chromecast.ChromecastConnectionCallback
import com.theoplayer.android.api.event.chromecast.ChromecastEventTypes
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.rememberPlayer
import com.theoplayer.android.ui.theme.THEOplayerTheme
import com.theoplayer.sample.common.AppTopBar
import com.theoplayer.sample.common.SourceManager

class PlayerActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        // Check if Cast API is available on the device (Google Play Services is available, CastContext can be initialized, and device is not an Android TV).
         fun isCastAvailable(context: Context): Boolean {
            when (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)) {
                ConnectionResult.SUCCESS -> {
                    try {
                        CastContext.getSharedInstance(context)
                        return true
                    } catch (_: Exception) {
                        return false
                    }
                }
                else -> return false
            }
        }

        // Initialize Chromecast immediately, for automatic receiver discovery to work correctly.
        if (isCastAvailable(applicationContext)) {
            CastContext.getSharedInstance(this)
        }

        // Enable all debug logs from THEOplayer.
        THEOplayerGlobal.getSharedInstance(this).logger.enableAllTags()

        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val theoplayerView = remember(context) {
                THEOplayerView(context, THEOplayerConfig.Builder().build()).apply {
                    keepScreenOn = true

                    // Allow background playback on the player to prevent Chromecast receiver from
                    // pausing when the app is backgrounded.
                    settings.setAllowBackgroundPlayback(true)

                    // THEOplayer automatically adds all available integrations to the player.
                    // Alternatively, you can set autoIntegrations(false) on your player configuration
                    // and add them manually.
                    val configuration = CastConfiguration.Builder()
                        .castStrategy(CastStrategy.MANUAL)
                        .build()
                    val castIntegration = CastIntegrationFactory.createCastIntegration(
                        this, configuration
                    )
                    player.addIntegration(castIntegration)
                }
            }

            val player = rememberPlayer(theoplayerView)
            val theoPlayer = theoplayerView.player
            val theoChromecast = theoplayerView.cast.chromecast

            LaunchedEffect(player) {
                // Coupling the orientation of the device with the fullscreen state.
                // The player will go fullscreen when the device is rotated to landscape
                // and will also exit fullscreen when the device is rotated back to portrait.
                theoplayerView.fullScreenManager.isFullScreenOrientationCoupled = true

                // Configuring THEOplayer with defined SourceDescription object.
                theoPlayer.source = SourceManager.BIG_BUCK_BUNNY_HLS_WITH_CAST_METADATA

                // Set autoplay to start video whenever player is visible.
                theoPlayer.isAutoplay = true

                // Attach player event listeners.
                theoPlayer.addEventListener(PlayerEventTypes.SOURCECHANGE) {
                    Log.i(TAG, "Event: SOURCECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.CURRENTSOURCECHANGE) {
                    Log.i(TAG, "Event: CURRENTSOURCECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.LOADEDDATA) {
                    Log.i(TAG, "Event: LOADEDDATA")
                }
                theoPlayer.addEventListener(PlayerEventTypes.LOADEDMETADATA) {
                    Log.i(TAG, "Event: LOADEDMETADATA")
                }
                theoPlayer.addEventListener(PlayerEventTypes.DURATIONCHANGE) {
                    Log.i(TAG, "Event: DURATIONCHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.TIMEUPDATE) {
//                    Log.i(TAG, "Event: TIMEUPDATE")
                }
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
                theoPlayer.addEventListener(PlayerEventTypes.WAITING) {
                    Log.i(TAG, "Event: WAITING")
                }
                theoPlayer.addEventListener(PlayerEventTypes.READYSTATECHANGE) {
                    Log.i(TAG, "Event: READYSTATECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.PRESENTATIONMODECHANGE) {
                    Log.i(TAG, "Event: PRESENTATIONMODECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.VOLUMECHANGE) {
                    Log.i(TAG, "Event: VOLUMECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.ENDED) {
                    Log.i(TAG, "Event: ENDED")
                }
                theoPlayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
                    Log.i(TAG, "Event: ERROR, error=" + event.errorObject)
                }

                // Attach Chromecast event listeners.
                theoChromecast.addEventListener(ChromecastEventTypes.STATECHANGE) {
                    Log.i(TAG, "Event: CAST_STATECHANGE, state=" + it.state)
                }
                theoChromecast.addEventListener(ChromecastEventTypes.ERROR) {
                    Log.i(TAG, "Event: CAST_ERROR, error=" + it.error)
                }

                // Some applications that do not require a MediaRouteButton to control the connection
                // with the Cast Receiver device can use the below APIs instead.
//                theoChromecast.start()
//                theoChromecast.stop()
//                theoChromecast.join()
//                theoChromecast.leave()

                // Some streaming setups require casting a different stream to a Cast Receiver device
                // than the one playing on a Cast Sender device, e.g. different DRM capabilities.
                // Code below shows how to configure such a different stream to cast.
                theoChromecast.setConnectionCallback(object : ChromecastConnectionCallback {
                    override fun onStart(sourceDescription: SourceDescription?): SourceDescription? {
                        return null
                    }

                    override fun onStop(sourceDescription: SourceDescription?): SourceDescription? {
                        return null
                    }

                    override fun onJoin(sourceDescription: SourceDescription?): SourceDescription? {
                        return null
                    }

                    override fun onLeave(sourceDescription: SourceDescription?): SourceDescription? {
                        return null
                    }
                })
            }

            THEOplayerTheme(useDarkTheme = true) {
                Scaffold(
                    topBar = {
                        AppTopBar(actions = {
                            AndroidView(
                                // This is a custom MediaRouterButton that is used to control the
                                // connection with the Cast Receiver device. Open Video UI already
                                // provides a default MediaRouterButton implementation, but you can
                                // also create your own custom button by using the
                                // CastButtonFactory.setUpMediaRouteButton() method.
                                factory = { _ ->
                                    MediaRouteButton(context).apply {
                                        if (isCastAvailable(applicationContext)) {
                                            CastButtonFactory.setUpMediaRouteButton(context, this)
                                        }
                                    }
                                }
                            )
                        })
                    }
                ) { padding ->
                    DefaultUI(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        player = player
                    )
                }
            }
        }
    }

    companion object {
        private val TAG: String = PlayerActivity::class.java.simpleName
    }
}
