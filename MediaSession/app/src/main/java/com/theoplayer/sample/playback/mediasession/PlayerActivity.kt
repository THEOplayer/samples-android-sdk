package com.theoplayer.sample.playback.mediasession

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.slider.Slider
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.SourceType
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.metadata.MetadataDescription
import com.theoplayer.android.connector.mediasession.MediaSessionConnector
import com.theoplayer.sample.playback.mediasession.databinding.ActivityPlayerBinding
import com.theoplayer.sample.playback.mediasession.ui.PlayerViewModel

open class PlayerActivity : AppCompatActivity() {
    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
    }

    private lateinit var viewModel: PlayerViewModel
    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var player: Player
    private lateinit var theoPlayerView: THEOplayerView
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var transportControls: MediaControllerCompat.TransportControls

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TheoTheme_Base)
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding and model classes.
        viewModel = ViewModelProvider(this)[PlayerViewModel::class.java]
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)
        viewBinding.let {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }

        // Gathering THEO objects references.
        theoPlayerView = viewBinding.theoPlayerView
        player = theoPlayerView.player

        // Create and initialize the media session
        mediaSession = MediaSessionCompat(this, TAG).apply {
            // Do not let MediaButtons restart the player when the app is not visible
            setMediaButtonReceiver(null)
        }

        // Create a MediaSessionConnector and attach the THEOplayer instance.
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.debug = true
        mediaSessionConnector.player = player

        // Create a MediaControllerCompat. It allows an app to interact with an ongoing media
        // session.
        mediaController = MediaControllerCompat(this, mediaSession)
        MediaControllerCompat.setMediaController(this, mediaController)
        transportControls = mediaController.transportControls
        mediaController.registerCallback(object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
                when (state.state) {
                    PlaybackStateCompat.STATE_PLAYING -> viewModel.markPlaying()
                    PlaybackStateCompat.STATE_BUFFERING -> viewModel.markBuffering()
                    PlaybackStateCompat.STATE_ERROR -> viewModel.setError(state.errorMessage?.toString())
                    PlaybackStateCompat.STATE_FAST_FORWARDING -> {}
                    PlaybackStateCompat.STATE_NONE -> {}
                    PlaybackStateCompat.STATE_PAUSED -> viewModel.markPaused()
                    PlaybackStateCompat.STATE_REWINDING -> {}
                    PlaybackStateCompat.STATE_SKIPPING_TO_NEXT -> {}
                    PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS -> {}
                    PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM -> {}
                    PlaybackStateCompat.STATE_STOPPED -> {}
                    PlaybackStateCompat.STATE_CONNECTING -> {}
                }
                viewModel.setCurrentTime(state.position)
            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat) {
                val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                if (duration > 0) {
                    viewModel.setDuration(duration)
                }
                viewModel.setMetadata(
                    metadata.description.title?.toString(),
                    metadata.description.subtitle?.toString()
                )
            }
        })

        configureUI()
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer()
    }

    private fun configureTHEOplayer() {
        player.isAutoplay = false

        val metadataFields = hashMapOf<String, Any>(
            "title" to "Elephants Dream",
            "author" to "Orange Open Movie Project",
            "displaySubtitle" to "demonstrating THEOplayer media session connector",
            "mediaId" to "stream01",
            "mediaUri" to "https://theoplayer.com",
            "displayIconUri" to "",
            "album" to "THEOplayer test streams",
            "artist" to "Orange Open Movie Project",
        )
        player.source = SourceDescription.Builder(
            TypedSource.Builder("https://cdn.theoplayer.com/video/elephants-dream/playlistCorrectionENG.m3u8")
                .type(SourceType.HLSX)
                .build()
        ).metadata(MetadataDescription(metadataFields))
            .build()
    }

    private fun configureUI() {
        viewBinding.playerClickableOverlay.setOnClickListener { viewModel.toggleUI() }
        viewBinding.playPauseButton.setOnClickListener {
            when (mediaController.playbackState.state) {
                PlaybackStateCompat.STATE_PAUSED -> {
                    transportControls.play()
                }
                PlaybackStateCompat.STATE_PLAYING -> {
                    transportControls.pause()
                }
                else -> { /* ignore */
                }
            }
        }
        viewBinding.skipForwardButton.setOnClickListener {
            transportControls.fastForward()
        }
        viewBinding.skipBackwardButton.setOnClickListener {
            transportControls.rewind()
        }
        viewBinding.progressSlider.setLabelFormatter(viewModel::formatTimeValue)
        viewBinding.progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                viewModel.markSeeking()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                viewModel.markSought()
                transportControls.seekTo((1e03 * slider.value).toLong())
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        var uiVisibilityFlags = View.SYSTEM_UI_FLAG_VISIBLE
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // With landscape orientation window (activity) display mode is changed to full screen
            // with status, navigation and action bars hidden.
            // Note that status and navigation bars are still available on swiping screen edge.
            supportActionBar!!.hide()
            uiVisibilityFlags = (uiVisibilityFlags
                    or View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            // With portrait orientation window (activity) display mode is changed back to default
            // with status, navigation and action bars shown.
            supportActionBar!!.show()
        }
        window.decorView.systemUiVisibility = uiVisibilityFlags
    }

    // In order to work properly and in sync with the activity lifecycle changes (e.g. device
    // is rotated, new activity is started or app is moved to background) we need to call
    // the "onResume", "onPause" and "onDestroy" methods of the THEOplayerView when the matching
    // activity methods are called.
    override fun onPause() {
        theoPlayerView.onPause()
        mediaSessionConnector.setActive(false)
        super.onPause()
    }

    override fun onResume() {
        theoPlayerView.onResume()
        mediaSessionConnector.setActive(true)
        super.onResume()
    }

    override fun onDestroy() {
        mediaSessionConnector.destroy()
        theoPlayerView.onDestroy()
        super.onDestroy()
    }
}