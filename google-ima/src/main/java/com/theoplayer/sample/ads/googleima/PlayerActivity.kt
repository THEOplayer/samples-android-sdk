package com.theoplayer.sample.ads.googleima

import android.os.Bundle
import android.util.Log
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
import com.theoplayer.android.api.event.ads.AdsEventTypes
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
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
                THEOplayerView(context, THEOplayerConfig.Builder().build()).apply {
                    keepScreenOn = true
                }
            }
            val player = rememberPlayer(theoplayerView)
            val theoPlayer = theoplayerView.player

            LaunchedEffect(player) {
                // Coupling the orientation of the device with the fullscreen state.
                theoplayerView.fullScreenManager.isFullScreenOrientationCoupled = true

                // THEOplayer automatically adds all available integrations to the player.
                // Alternatively, you can set autoIntegrations(false) on your player configuration
                // and add them manually.
//                val googleImaIntegration = GoogleImaIntegrationFactory.createGoogleImaIntegration(theoplayerView)
//                theoPlayer.addIntegration(googleImaIntegration)

                // Set a source with ads on the player.
                theoPlayer.source = SourceManager.HLS_WITH_VMAP

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

                // Attach ad event listeners.
                theoPlayer.ads.addEventListener(AdsEventTypes.AD_BEGIN) {
                    Log.i(TAG, "Event: AD_BEGIN")
                }
                theoPlayer.ads.addEventListener(AdsEventTypes.AD_END) {
                    Log.i(TAG, "Event: AD_END")
                }
                theoPlayer.ads.addEventListener(AdsEventTypes.AD_ERROR) {
                    Log.i(TAG, "Event: AD_ERROR")
                }
                theoPlayer.ads.addEventListener(AdsEventTypes.AD_LOADED) {
                    Log.i(TAG, "Event: AD_LOADED")
                }
                theoPlayer.ads.addEventListener(AdsEventTypes.AD_BREAK_BEGIN) {
                    Log.i(TAG, "Event: AD_BREAK_BEGIN")
                }
                theoPlayer.ads.addEventListener(AdsEventTypes.AD_BREAK_END) {
                    Log.i(TAG, "Event: AD_BREAK_END")
                }
                theoPlayer.ads.addEventListener(AdsEventTypes.AD_SKIP) {
                    Log.i(TAG, "Event: AD_SKIP")
                }
                theoPlayer.ads.addEventListener(AdsEventTypes.AD_TAPPED) {
                    Log.i(TAG, "Event: AD_TAPPED")
                }
                theoPlayer.ads.addEventListener(AdsEventTypes.AD_IMPRESSION) {
                    Log.i(TAG, "Event: AD_IMPRESSION")
                }
                theoPlayer.ads.addEventListener(AdsEventTypes.AD_FIRST_QUARTILE) {
                    Log.i(TAG, "Event: AD_FIRST_QUARTILE")
                }
                theoPlayer.ads.addEventListener(AdsEventTypes.AD_MIDPOINT) {
                    Log.i(TAG, "Event: AD_MIDPOINT")
                }
                theoPlayer.ads.addEventListener(AdsEventTypes.AD_THIRD_QUARTILE) {
                    Log.i(TAG, "Event: AD_THIRD_QUARTILE")
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
