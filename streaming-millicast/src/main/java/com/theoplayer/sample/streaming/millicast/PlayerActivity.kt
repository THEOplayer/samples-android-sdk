package com.theoplayer.sample.streaming.millicast

import android.util.Log
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.millicast.MillicastIntegrationFactory
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.rememberPlayer
import com.theoplayer.android.ui.theme.THEOplayerTheme
import com.theoplayer.sample.common.AppTopBar
import com.theoplayer.sample.common.SourceManager

class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable all debug logs from THEOplayer.
        THEOplayerGlobal.getSharedInstance(this).logger.enableAllTags()

        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val theoplayerView = remember(context) {
                // Creating the player with default parameters.
                THEOplayerView(context, THEOplayerConfig.Builder().build()).apply {
                    // Keep the device screen on.
                    keepScreenOn = true

                    // THEOplayer automatically adds all available integrations to the player.
                    // Alternatively, you can set autoIntegrations(false) on your player configuration
                    // and add them manually.
                    val millicastIntegration = MillicastIntegrationFactory.createMillicastIntegration()
                    player.addIntegration(millicastIntegration)
                }
            }

            val player = rememberPlayer(theoplayerView)
            val theoPlayer = theoplayerView.player


            LaunchedEffect(player) {

                // Enable background playback.
                theoplayerView.settings.setAllowBackgroundPlayback(true)

                // Configuring the player with a SourceDescription object.
                theoPlayer.source = SourceManager.MILLICAST

                //  Set autoplay to start video whenever player is visible.
                theoPlayer.isAutoplay = true

                // Attach event listeners.
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
                theoPlayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
                    Log.i(TAG, "Event: ERROR, error=" + event.errorObject)
                }
            }

            THEOplayerTheme(useDarkTheme = true) {
                Scaffold(
                    topBar = { AppTopBar() }
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
