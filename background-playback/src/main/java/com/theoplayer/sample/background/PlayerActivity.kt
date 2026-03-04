package com.theoplayer.sample.background

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.PlaybackStateCompat
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
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.EndedEvent
import com.theoplayer.android.api.event.player.LoadedMetadataEvent
import com.theoplayer.android.api.event.player.PauseEvent
import com.theoplayer.android.api.event.player.PlayEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.SourceChangeEvent
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.connector.mediasession.MediaSessionConnector
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.rememberPlayer
import com.theoplayer.android.ui.theme.THEOplayerTheme
import com.theoplayer.sample.background.audio.AudioBecomingNoisyManager
import com.theoplayer.sample.background.audio.AudioFocusManager
import com.theoplayer.sample.background.media.MediaPlaybackService
import com.theoplayer.sample.common.AppTopBar
import com.theoplayer.sample.common.SourceManager
import java.util.concurrent.atomic.AtomicBoolean

class PlayerActivity : ComponentActivity() {

    private var theoplayerView: THEOplayerView? = null
    private var theoPlayer: Player? = null

    private var isBound = AtomicBoolean()
    private var binder: MediaPlaybackService.MediaPlaybackBinder? = null
    private var mediaSessionConnector: MediaSessionConnector? = null
    private var audioBecomingNoisyManager: AudioBecomingNoisyManager? = null
    private var audioFocusManager: AudioFocusManager? = null
    private var isHostPaused = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            binder = service as MediaPlaybackService.MediaPlaybackBinder

            // Get media session connector from service.
            mediaSessionConnector = binder?.mediaSessionConnector?.also {
                applyMediaSessionConfig(it)
            }

            // Pass player context.
            theoPlayer?.let { binder?.setPlayerContext(it) }

            // Apply background audio config.
            binder?.setEnablePlaybackControls(true)
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            binder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable all debug logs from THEOplayer.
        THEOplayerGlobal.getSharedInstance(this).logger.enableAllTags()

        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val tpv = remember(context) {
                THEOplayerView(context, THEOplayerConfig.Builder().build()).apply {
                    keepScreenOn = true
                }.also {
                    this@PlayerActivity.theoplayerView = it
                    this@PlayerActivity.theoPlayer = it.player
                }
            }
            val player = rememberPlayer(tpv)

            LaunchedEffect(player) {
                initializePlayer()
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

    private fun initializePlayer() {
        val player = theoPlayer ?: return

        // Configuring the player with a SourceDescription object.
        player.source = SourceManager.BIG_BUCK_BUNNY_HLS_WITH_CAST_METADATA

        // Set autoplay to start video whenever player is visible.
        player.isAutoplay = true

        // Set up fullscreen orientation coupling.
        theoplayerView?.fullScreenManager?.isFullScreenOrientationCoupled = true

        // Set up audio managers.
        audioBecomingNoisyManager = AudioBecomingNoisyManager(this) {
            // Audio is about to become 'noisy' due to a change in audio outputs: pause the player.
            player.pause()
        }
        audioFocusManager = AudioFocusManager(this, player)

        // Enable background playback.
        theoplayerView?.settings?.setAllowBackgroundPlayback(true)

        // Attach event listeners.
        addListeners()

        // Bind to the media playback service.
        bindMediaPlaybackService()
    }

    // region Event Listeners

    private val onSourceChange = EventListener<SourceChangeEvent> {
        // Pass updated metadata to the media session connector.
        mediaSessionConnector?.setMediaSessionMetadata(theoPlayer?.source)
        // Update the notification.
        binder?.updateNotification()
    }

    private val onLoadedMetadata = EventListener<LoadedMetadataEvent> {
        // Update the notification.
        binder?.updateNotification()
    }

    private val onPlay = EventListener<PlayEvent> {
        bindMediaPlaybackService()
        binder?.updateNotification(PlaybackStateCompat.STATE_PLAYING)
        audioBecomingNoisyManager?.setEnabled(true)
        audioFocusManager?.retrieveAudioFocus()
    }

    private val onPause = EventListener<PauseEvent> {
        binder?.updateNotification(PlaybackStateCompat.STATE_PAUSED)
        audioBecomingNoisyManager?.setEnabled(false)
    }

    private val onEnded = EventListener<EndedEvent> {
        // Playback has ended, we can abandon audio focus.
        audioFocusManager?.abandonAudioFocus()
    }

    private fun addListeners() {
        val player = theoPlayer ?: return
        player.addEventListener(PlayerEventTypes.SOURCECHANGE, onSourceChange)
        player.addEventListener(PlayerEventTypes.LOADEDMETADATA, onLoadedMetadata)
        player.addEventListener(PlayerEventTypes.PLAY, onPlay)
        player.addEventListener(PlayerEventTypes.PAUSE, onPause)
        player.addEventListener(PlayerEventTypes.ENDED, onEnded)
    }

    private fun removeListeners() {
        val player = theoPlayer ?: return
        player.removeEventListener(PlayerEventTypes.SOURCECHANGE, onSourceChange)
        player.removeEventListener(PlayerEventTypes.LOADEDMETADATA, onLoadedMetadata)
        player.removeEventListener(PlayerEventTypes.PLAY, onPlay)
        player.removeEventListener(PlayerEventTypes.PAUSE, onPause)
        player.removeEventListener(PlayerEventTypes.ENDED, onEnded)
    }

    // Media Playback Service

    private fun bindMediaPlaybackService() {
        // Bind to an existing service, if available
        // A bound service runs only as long as another application component is bound to it.
        // Multiple components can bind to the service at once, but when all of them unbind, the
        // service is destroyed.
        if (!isBound.get()) {
            // Clean up any existing media session connector.
            mediaSessionConnector?.destroy()

            isBound.set(
                bindService(
                    Intent(this, MediaPlaybackService::class.java),
                    connection,
                    Context.BIND_AUTO_CREATE
                )
            )
        }
    }

    private fun unbindMediaPlaybackService() {
        // This client is done interacting with the service: unbind.
        // When there are no clients bound to the service, the system destroys the service.
        if (binder?.isBinderAlive == true) {
            if (isBound.getAndSet(false)) {
                unbindService(connection)
            }
        }
        binder = null
    }

    private fun applyMediaSessionConfig(connector: MediaSessionConnector?) {
        connector?.apply {
            debug = true
            player = theoPlayer
            // Set mediaSession active and ready to receive media button events, but not if the player
            // is backgrounded.
            setActive(!isHostPaused)
            skipForwardInterval = 5.0
            skipBackwardsInterval = 5.0
            setMediaSessionMetadata(player?.source)
        }
    }

    // Lifecycle

    override fun onPause() {
        super.onPause()
        isHostPaused = true
        theoplayerView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        isHostPaused = false

        // Since this is a music player, the volume controls should adjust the music volume while
        // in the app.
        volumeControlStream = AudioManager.STREAM_MUSIC

        mediaSessionConnector?.setActive(true)

        theoplayerView?.onResume()

        if (theoPlayer?.isPaused == false) {
            audioFocusManager?.retrieveAudioFocus()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        removeListeners()

        // Remove service from foreground.
        binder?.stopForegroundService()

        // Unbind client from background service so it can stop.
        unbindMediaPlaybackService()

        audioFocusManager?.abandonAudioFocus()
        mediaSessionConnector?.destroy()

        theoplayerView?.onDestroy()
    }

    companion object {
        private val TAG: String = PlayerActivity::class.java.simpleName
    }
}
