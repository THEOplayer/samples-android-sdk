package com.theoplayer.sample.ui.pip

import android.app.PendingIntent
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.text.style.AlignmentSpan
import android.util.Log
import android.util.Rational
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.app.PictureInPictureParamsCompat
import androidx.core.content.ContextCompat
import androidx.core.pip.PictureInPictureDelegate
import androidx.core.pip.PictureInPictureDelegate.OnPictureInPictureEventListener
import androidx.core.pip.VideoPlaybackPictureInPicture
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

class PlayerActivity : ComponentActivity(), OnPictureInPictureEventListener {

    private lateinit var theoplayerView: THEOplayerView
    private lateinit var pip: VideoPlaybackPictureInPicture
    private var isInPipMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable all debug logs from THEOplayer.
        THEOplayerGlobal.getSharedInstance(this).logger.enableAllTags()

        super.onCreate(savedInstanceState)

        theoplayerView = THEOplayerView(this, THEOplayerConfig.Builder().build()).apply {
            // Keep the device screen on.
            keepScreenOn = true
        }
        pip = VideoPlaybackPictureInPicture(this).apply {
            setPlayerView(theoplayerView)
            setAspectRatio(Rational(16, 9))

            // On API 31+, tell the system to auto enter PiP when the user navigates away.
            // This gives a smoother animation than manually calling enterPictureInPictureMode().
            setEnabled(true)

            addOnPictureInPictureEventListener(
                ContextCompat.getMainExecutor(this@PlayerActivity),
                this@PlayerActivity
            )
        }

        if (SUPPORTS_PIP) {
            // Keep PIP aspect ratio in sync with player.
            updatePipAspectRatio()
            theoplayerView.player.addEventListener(PlayerEventTypes.RESIZE) { updatePipAspectRatio() }

            // Add play/pause PIP actions, and keep in sync with player.
            updatePipActions()
            theoplayerView.player.addEventListener(PlayerEventTypes.PLAY) { updatePipActions() }
            theoplayerView.player.addEventListener(PlayerEventTypes.PAUSE) { updatePipActions() }
        }

        setContent {
            val player = rememberPlayer(theoplayerView)
            val theoPlayer = theoplayerView.player

            // Broadcast receiver is only used if app is in PiP mode.
            if (isInPipMode) {
                PlayerBroadcastReceiver()
            }

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

    private fun tryEnterPictureInPictureMode() {
        if (SUPPORTS_PIP) {
            // Hide toolbar early for a smooth PiP transition.
            isInPipMode = true
            // On API 31+, auto enter handles the transition when navigating away,
            // but we still call enterPictureInPictureMode for explicit triggers (e.g. PiP button)
            // and as a fallback on API < 31.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !isInPictureInPictureMode) {
                enterPictureInPictureMode(
                    PictureInPictureParamsCompat.Builder().build()
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

    override fun onPictureInPictureEvent(
        event: PictureInPictureDelegate.Event,
        config: Configuration?
    ) {
        when (event) {
            PictureInPictureDelegate.Event.ENTER_ANIMATION_START,
            PictureInPictureDelegate.Event.ENTERED -> {
                isInPipMode = true
            }

            PictureInPictureDelegate.Event.EXITED -> {
                isInPipMode = false
            }

            else -> {}
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePipAspectRatio() {
        val player = theoplayerView.player
        val videoWidth = player.videoWidth
        val videoHeight = player.videoHeight
        if (videoWidth > 0 && videoHeight > 0) {
            pip.setAspectRatio(Rational(videoWidth, videoHeight))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePipActions() {
        val player = theoplayerView.player
        val isPaused = player.isPaused || player.isEnded
        pip.setActions(
            listOf(
                if (isPaused) {
                    createRemoteAction(
                        R.drawable.play_arrow,
                        R.string.playAction,
                        EXTRA_CONTROL_PLAY
                    )
                } else {
                    createRemoteAction(
                        R.drawable.pause,
                        R.string.pauseAction,
                        EXTRA_CONTROL_PAUSE
                    )
                }
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createRemoteAction(
        @DrawableRes iconResId: Int,
        @StringRes titleResId: Int,
        controlType: Int,
    ): RemoteAction = RemoteAction(
        Icon.createWithResource(this, iconResId),
        getString(titleResId),
        getString(titleResId),
        PendingIntent.getBroadcast(
            this,
            controlType,
            Intent(ACTION_PLAYER_CONTROL).apply {
                setPackage(packageName)
                putExtra(EXTRA_CONTROL_TYPE, controlType)
            },
            PendingIntent.FLAG_IMMUTABLE,
        )
    )

    @Composable
    fun PlayerBroadcastReceiver() {
        val context = LocalContext.current
        val theoplayer = theoplayerView.player
        DisposableEffect(theoplayer) {
            val broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action != ACTION_PLAYER_CONTROL) return
                    when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                        EXTRA_CONTROL_PLAY -> theoplayer.play()
                        EXTRA_CONTROL_PAUSE -> theoplayer.pause()
                    }
                }
            }
            ContextCompat.registerReceiver(
                context,
                broadcastReceiver,
                IntentFilter(ACTION_PLAYER_CONTROL),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            onDispose { context.unregisterReceiver(broadcastReceiver) }
        }
    }

    companion object {
        private val TAG: String = PlayerActivity::class.java.simpleName
        private val SUPPORTS_PIP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

        // Intent extras for broadcast controls from Picture-in-Picture mode.
        private const val ACTION_PLAYER_CONTROL = "player_control"
        private const val EXTRA_CONTROL_TYPE = "control_type"
        private const val EXTRA_CONTROL_PLAY = 1
        private const val EXTRA_CONTROL_PAUSE = 2
    }
}
