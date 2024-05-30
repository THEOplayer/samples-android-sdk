package com.theoplayer.sample.playback.background

import android.content.ComponentName
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.slider.Slider
import com.theoplayer.sample.playback.background.databinding.ActivityMainBinding
import com.theoplayer.sample.playback.background.ui.PlayerViewModel

private const val TAG = "MediaPlayback"

open class PlayerActivity : AppCompatActivity() {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var viewModel: PlayerViewModel
    private lateinit var viewBinding: ActivityMainBinding
    private var currentPlaybackState: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TheoTheme_Base)
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding and model classes.
        viewModel = ViewModelProvider(this)[PlayerViewModel::class.java]
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewBinding.let {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }

        setSupportActionBar(viewBinding.toolbarLayout.toolbar)

        // Create MediaBrowserServiceCompat
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlaybackService::class.java),
            connectionCallbacks,
            null // optional Bundle
        )
    }

    public override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    public override fun onResume() {
        super.onResume()

        // Since this is a music player, the volume controls should adjust the music volume while
        // in the app.
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    public override fun onStop() {
        super.onStop()
        // (see "stay in sync with the MediaSession")
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    private val subscriptionCallback = object : SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
            Log.d(TAG, "SubscriptionCallback\$onChildrenLoaded")
            if (children.isNotEmpty()) {
                val mediaController = MediaControllerCompat.getMediaController(this@PlayerActivity)

                // Set-up initial media asset
                if (mediaController.playbackState?.state == null) {
                    mediaController.transportControls.prepareFromMediaId(children[0].mediaId, null)
                }
            }
        }

        override fun onError(parentId: String) {
            Log.d(TAG, "SubscriptionCallback\$onError")
        }
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Log.d(TAG, "MediaBrowserCompat\$onConnected")

            // Get the token for the MediaSession
            mediaBrowser.sessionToken.also { token ->

                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(this@PlayerActivity, token)

                // Save the controller
                MediaControllerCompat.setMediaController(this@PlayerActivity, mediaController)
            }

            mediaBrowser.subscribe("parentId", subscriptionCallback)

            // Finish building the UI
            buildTransportControls()

            // Reparent THEOplayerView
            val view = MediaPlaybackService.instance.getTHEOplayerView()
            view.parent?.let {
                (it as ViewGroup).removeView(view)
            }
            val playerContainer = viewBinding.playerContainer
            playerContainer.removeAllViews()
            playerContainer.addView(view)
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
            Log.d(TAG, "MediaBrowserCompat\$onConnectionSuspended")
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
            Log.d(TAG, "MediaBrowserCompat\$onConnectionFailed")
        }
    }

    // The UI should display the current state of the media session, as described by its
    // PlaybackState and Metadata. When you create the transport controls, you can grab the current
    // state of the session, display it in your UI, and enable and disable transport controls based
    // on the state and its available actions.
    private var controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            if (state.state != currentPlaybackState) {
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
                currentPlaybackState = state.state
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

        override fun onSessionDestroyed() {
            Log.d(TAG, "MediaControllerCompat.Callback\$onSessionDestroyed")

            // When this happens, the session cannot become functional again within the lifetime
            // of the MediaBrowserService. Although functions related to MediaBrowser might
            // continue to work, a user cannot view or control playback from a destroyed media
            // session, which will likely diminish the value of your application.
            // Therefore, when the session is destroyed, you must disconnect from the
            // MediaBrowserService.
            mediaBrowser.disconnect()
            // maybe schedule a reconnection using a new MediaBrowser instance
        }
    }

    private fun buildTransportControls() {
        val mediaController = MediaControllerCompat.getMediaController(this@PlayerActivity)

        viewBinding.playPauseButton.setOnClickListener {
            when (mediaController.playbackState?.state) {
                PlaybackStateCompat.STATE_PAUSED -> {
                    mediaController.transportControls.play()
                }
                PlaybackStateCompat.STATE_PLAYING -> {
                    mediaController.transportControls.pause()
                }
                else -> { /* ignore */ }
            }
        }
        viewBinding.skipForwardButton.setOnClickListener {
            mediaController.transportControls.skipToNext()
        }
        viewBinding.skipBackwardButton.setOnClickListener {
            mediaController.transportControls.skipToPrevious()
        }
        viewBinding.progressSlider.setLabelFormatter(viewModel::formatTimeValue)
        viewBinding.progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                viewModel.markSeeking()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                viewModel.markSought()
                mediaController.transportControls.seekTo((1e03 * slider.value).toLong())
            }
        })

        // Display the initial state
        if (mediaController.metadata != null) {
            controllerCallback.onMetadataChanged(mediaController.metadata)
        }
        if (mediaController.playbackState != null) {
            controllerCallback.onPlaybackStateChanged(mediaController.playbackState)
        }

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback)
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
}
