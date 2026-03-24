package com.theoplayer.sample.ui.pip

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.text.style.AlignmentSpan
import android.util.Log
import android.util.Rational
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.rememberPlayer
import com.theoplayer.android.ui.theme.THEOplayerTheme
import com.theoplayer.sample.common.AppTopBar
import com.theoplayer.sample.common.SourceManager

class PlayerActivity : ComponentActivity() {

    private var theoplayerView: THEOplayerView? = null
    private var isInPipMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable all debug logs from THEOplayer.
        THEOplayerGlobal.getSharedInstance(this).logger.enableAllTags()

        super.onCreate(savedInstanceState)

        addOnPictureInPictureModeChangedListener { info: PictureInPictureModeChangedInfo ->
            isInPipMode = info.isInPictureInPictureMode
        }

        // On API 31+, tell the system to auto enter PiP when the user navigates away.
        // This gives a smoother animation than manually calling enterPictureInPictureMode().
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .setAutoEnterEnabled(true)
                .build()
            setPictureInPictureParams(params)
        }

        setContent {
            val context = LocalContext.current
            val theoplayerView = remember(context) {
                THEOplayerView(context, THEOplayerConfig.Builder().build()).apply {
                    // Keep the device screen on.
                    keepScreenOn = true
                }.also { this@PlayerActivity.theoplayerView = it }
            }

            val player = rememberPlayer(theoplayerView)

            val theoPlayer = theoplayerView.player

            LaunchedEffect(player) {
                // Coupling the orientation of the device with the fullscreen state.
                theoplayerView.fullScreenManager.isFullScreenOrientationCoupled = true

                // Enable background playback.
                theoplayerView.settings.setAllowBackgroundPlayback(true)

                // Configuring THEOplayer with a source.
                theoPlayer.source = SourceManager.BIP_BOP_HLS

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
            }

            THEOplayerTheme(useDarkTheme = true) {
                Scaffold(
                    topBar = {
                        if (!isInPipMode) AppTopBar(
                            actions = {
                                IconButton(onClick = { tryEnterPictureInPictureMode() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_pip),
                                        contentDescription = "Picture in Picture",
                                        tint = Color.White
                                    )
                                }
                            }
                        )
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

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        tryEnterPictureInPictureMode()
    }

    private fun tryEnterPictureInPictureMode() {
        if (SUPPORTS_PIP) {
            // Hide toolbar early for a smooth PiP transition.
            isInPipMode = true
            // On API 31+, auto enter handles the transition when navigating away,
            // but we still call enterPictureInPictureMode for explicit triggers (e.g. PiP button)
            // and as a fallback on API < 31.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !isInPictureInPictureMode) {
                enterPictureInPictureMode(
                    PictureInPictureParams.Builder()
                        .setAspectRatio(Rational(16, 9))
                        .build()
                )
            }
        } else {
            val toastMessage = buildSpannedString {
                inSpans(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)) {
                    append(getString(R.string.pipNotSupported))
                }
            }
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private val TAG: String = PlayerActivity::class.java.simpleName
        private val SUPPORTS_PIP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
}
