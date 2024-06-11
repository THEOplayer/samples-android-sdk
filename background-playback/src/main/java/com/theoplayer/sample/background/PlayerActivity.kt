package com.theoplayer.sample.background

import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.connector.mediasession.MediaSessionConnector
import com.theoplayer.sample.background.databinding.ActivityPlayerBinding
import com.theoplayer.sample.common.SourceManager

class PlayerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayer: Player

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Do the initial set-up.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)
        theoPlayer = viewBinding.theoPlayerView.player
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)
        viewBinding.theoPlayerView.fullScreenManager.isFullScreenOrientationCoupled = true
        theoPlayer.source = SourceManager.ELEPHANTS_DREAM_HLS
        theoPlayer.isAutoplay = true

        setupForBackgroundPlayback()
    }

    private fun setupForBackgroundPlayback() {
        // Allow background audio playback.
        viewBinding.theoPlayerView.settings.setAllowBackgroundPlayback(true)

        // Create and initialize the media session
        mediaSession = MediaSessionCompat(this, TAG).apply {
            // Do not let MediaButtons restart the player when the app is not visible
            setMediaButtonReceiver(null)
        }

        // Create a MediaSessionConnector and attach the THEOplayer instance.
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.debug = true

        // Pass the player instance
        mediaSessionConnector.player = viewBinding.theoPlayerView.player

        // Set mediaSession to active
        mediaSessionConnector.setActive(true)
    }

    override fun onPause() {
        super.onPause()
        viewBinding.theoPlayerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        viewBinding.theoPlayerView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding.theoPlayerView.onDestroy()
    }

    companion object {
        private val TAG: String = PlayerActivity::class.java.simpleName
    }
}