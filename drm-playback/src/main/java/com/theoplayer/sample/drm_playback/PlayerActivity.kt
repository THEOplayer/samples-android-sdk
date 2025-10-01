package com.theoplayer.sample.drm_playback

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.contentprotection.KeySystemId
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.drm.DRMConfiguration
import com.theoplayer.android.api.source.drm.KeySystemConfiguration
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.rememberPlayer
import com.theoplayer.android.ui.theme.THEOplayerTheme
import com.theoplayer.sample.common.SourceManager
import com.theoplayer.sample.drm_playback.integration.axinom.AxinomWidevineContentProtectionIntegrationFactory

class PlayerActivity : ComponentActivity() {

    var tpv by mutableStateOf<THEOplayerView?>(null)
    private lateinit var theoPlayer: Player

    // Configuring THEOplayer playback with default parameters.
    val theoplayerConfigBuilder = THEOplayerConfig.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {

        // Register the DRM integration to the global THEOplayer object.
        THEOplayerGlobal.getSharedInstance(this).registerContentProtectionIntegration(
            "axinom",
            KeySystemId.WIDEVINE,
            AxinomWidevineContentProtectionIntegrationFactory()
        )

        // Enable all debug logs from THEOplayer.
        val theoDebugLogger = THEOplayerGlobal.getSharedInstance(this).logger
        theoDebugLogger.enableAllTags()

        tpv = createTHEOplayerView(theoplayerConfigBuilder.build())

        // Gathering THEOplayer reference.
        theoPlayer = tpv?.player!!

        super.onCreate(savedInstanceState)

        tpv?.apply {
            // Attach event listeners.
            attachEventListeners()

            // Set source on the player.
            theoPlayer.source = SourceManager.CUSTOM_AXINOM_DRM

            // Set autoplay to start video whenever the player is visible.
            theoPlayer.isAutoplay = true
        }

        setContent {
            THEOplayerTheme(useDarkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    tpv?.let { tpv ->
                        DefaultUI(
                            player = rememberPlayer(theoplayerView = tpv),
                        )
                    }
                }
            }
        }
    }

    private fun createTHEOplayerView(playerConfig: THEOplayerConfig): THEOplayerView {
        val tpv = THEOplayerView(this, playerConfig)
        tpv.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return tpv
    }

    private fun attachEventListeners() {
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
        theoPlayer.addEventListener(PlayerEventTypes.CONTENTPROTECTIONSUCCESS) {
            Log.i(TAG, "Event: CONTENTPROTECTIONSUCCESS")
        }
        theoPlayer.addEventListener(PlayerEventTypes.CONTENTPROTECTIONERROR) {
            Log.i(TAG, "Event: CONTENTPROTECTIONERROR")
        }
    }
}