package com.theoplayer.sample.ui.fullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.fullscreen.FullScreenActivity
import com.theoplayer.android.api.fullscreen.FullScreenManager
import com.theoplayer.android.api.player.Player
import com.theoplayer.sample.ui.fullscreen.databinding.ActivityFullscreenBinding

class CustomFullScreenActivity : FullScreenActivity() {
    private lateinit var viewBinding: ActivityFullscreenBinding
    private lateinit var theoPlayer: Player
    private lateinit var theoFullScreenManager: FullScreenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        // Adding support for extended AppCompat features.
        // It allows to use styles and themes defined for material components.
        delegate.installViewFactory()
        delegate.onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)

        // Inflating custom view and obtaining an instance of the binding class.
        viewBinding = ActivityFullscreenBinding.inflate(LayoutInflater.from(this), null, false)
        delegate.addContentView(
            viewBinding.root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Gathering THEO objects references.
        theoPlayer = theOplayerView!!.player
        theoFullScreenManager = theOplayerView!!.fullScreenManager

        // Configuring UI behavior.
        adjustPlayPauseButtonIcon()
        viewBinding.playPauseButton.setOnClickListener { onPlayPauseClick() }
        viewBinding.exitFullScreenButton.setOnClickListener { onFullScreenExit() }

        // Set default orientation
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);

        // Configuring THEOplayer.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY) { adjustPlayPauseButtonIcon() }
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE) { adjustPlayPauseButtonIcon() }
    }

    private fun onFullScreenExit() {
        theoFullScreenManager.exitFullScreen()
    }

    private fun onPlayPauseClick() {
        if (theoPlayer.isPaused) {
            theoPlayer.play()
        } else {
            theoPlayer.pause()
        }
    }

    private fun adjustPlayPauseButtonIcon() {
        if (theoPlayer.isPaused) {
            viewBinding.playPauseButton.setIconResource(R.drawable.ic_play)
        } else {
            viewBinding.playPauseButton.setIconResource(R.drawable.ic_pause)
        }
    }
}