package com.theoplayer.sample.background

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.PlaybackStateCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.event.player.EndedEvent
import com.theoplayer.android.api.event.player.LoadedMetadataEvent
import com.theoplayer.android.api.event.player.PauseEvent
import com.theoplayer.android.api.event.player.PlayEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.SourceChangeEvent
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.connector.mediasession.MediaSessionConnector
import com.theoplayer.sample.background.audio.AudioBecomingNoisyManager
import com.theoplayer.sample.background.audio.AudioFocusManager
import com.theoplayer.sample.background.databinding.ActivityPlayerBinding
import com.theoplayer.sample.background.media.MediaPlaybackService
import com.theoplayer.sample.common.SourceManager
import java.util.concurrent.atomic.AtomicBoolean
import com.theoplayer.android.api.event.EventListener

private val TAG: String = PlayerActivity::class.java.simpleName

class PlayerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayer: Player
    private val playerView: THEOplayerView
        get() = viewBinding.theoPlayerView

    private var isBound = AtomicBoolean()
    private var binder: MediaPlaybackService.MediaPlaybackBinder? = null
    private var mediaSessionConnector: MediaSessionConnector? = null
    private var audioBecomingNoisyManager = AudioBecomingNoisyManager(this) {
        // Audio is about to become 'noisy' due to a change in audio outputs: pause the player
        theoPlayer.pause()
    }
    private var audioFocusManager: AudioFocusManager? = null
    private var isHostPaused: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            binder = service as MediaPlaybackService.MediaPlaybackBinder

            // Get media session connector from service
            mediaSessionConnector = binder?.mediaSessionConnector?.also {
                applyMediaSessionConfig(it)
            }

            // Pass player context
            binder?.setPlayerContext(this@PlayerActivity.theoPlayer)

            // Apply background audio config
            binder?.setEnablePlaybackControls(true)
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            binder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Do the initial set-up.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)
        theoPlayer = viewBinding.theoPlayerView.player
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)
        viewBinding.theoPlayerView.fullScreenManager.isFullScreenOrientationCoupled = true
        theoPlayer.source = SourceManager.ELEPHANTS_DREAM_HLS_WITH_CAST_METADATA
        theoPlayer.isAutoplay = true

        initializePlayerView()
    }

    private fun bindMediaPlaybackService() {
        // Bind to an existing service, if available
        // A bound service runs only as long as another application component is bound to it.
        // Multiple components can bind to the service at once, but when all of them unbind, the
        // service is destroyed.
        if (!isBound.get()) {
            // Clean-up any existing media session connector
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

    private fun initializePlayerView() {
        // By default, the screen should remain on.
        viewBinding.theoPlayerView.keepScreenOn = true

        addListeners()

        audioFocusManager = AudioFocusManager(this, theoPlayer)

        playerView.settings.setAllowBackgroundPlayback(true)

        // Enable & bind background playback
        bindMediaPlaybackService()
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

            // Pass metadata from source description
            setMediaSessionMetadata(player?.source)
        }
    }

    private val onSourceChange = EventListener<SourceChangeEvent> {
        // Pass updated metadata to the media session connector
        mediaSessionConnector?.setMediaSessionMetadata(theoPlayer.source)

        // Update the notification
        binder?.updateNotification()
    }

    private val onLoadedMetadata = EventListener<LoadedMetadataEvent> {
        // Update the notification
        binder?.updateNotification()
    }

    private val onPlay = EventListener<PlayEvent> {
        bindMediaPlaybackService()
        binder?.updateNotification(PlaybackStateCompat.STATE_PLAYING)
        audioBecomingNoisyManager.setEnabled(true)
        audioFocusManager?.retrieveAudioFocus()
    }

    private val onPause = EventListener<PauseEvent> {
        binder?.updateNotification(PlaybackStateCompat.STATE_PAUSED)
        audioBecomingNoisyManager.setEnabled(false)
    }

    private val onEnded = EventListener<EndedEvent> {
        // Playback has ended, we can abandon audio focus.
        audioFocusManager?.abandonAudioFocus()
    }

    private fun addListeners() {
        theoPlayer.apply {
            addEventListener(PlayerEventTypes.SOURCECHANGE, onSourceChange)
            addEventListener(PlayerEventTypes.LOADEDMETADATA, onLoadedMetadata)
            addEventListener(PlayerEventTypes.PAUSE, onPause)
            addEventListener(PlayerEventTypes.PLAY, onPlay)
            addEventListener(PlayerEventTypes.ENDED, onEnded)
        }
    }

    private fun removeListeners() {
        theoPlayer.apply {
            removeEventListener(PlayerEventTypes.SOURCECHANGE, onSourceChange)
            removeEventListener(PlayerEventTypes.LOADEDMETADATA, onLoadedMetadata)
            removeEventListener(PlayerEventTypes.PAUSE, onPause)
            removeEventListener(PlayerEventTypes.PLAY, onPlay)
            removeEventListener(PlayerEventTypes.ENDED, onEnded)
        }
    }

    override fun onPause() {
        super.onPause()
        isHostPaused = true
        viewBinding.theoPlayerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        isHostPaused = false

        // Since this is a music player, the volume controls should adjust the music volume while
        // in the app.
        volumeControlStream = AudioManager.STREAM_MUSIC

        mediaSessionConnector?.setActive(true)

        viewBinding.theoPlayerView.onResume()

        if (!theoPlayer.isPaused) {
            audioFocusManager?.retrieveAudioFocus()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        removeListeners()

        // Remove service from foreground
        binder?.stopForegroundService()

        // Unbind client from background service so it can stop
        unbindMediaPlaybackService()

        audioFocusManager?.abandonAudioFocus()
        mediaSessionConnector?.destroy()

        viewBinding.theoPlayerView.onDestroy()
    }
}