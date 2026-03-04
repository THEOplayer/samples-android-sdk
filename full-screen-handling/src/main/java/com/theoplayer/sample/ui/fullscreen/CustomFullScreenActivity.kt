package com.theoplayer.sample.ui.fullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.fullscreen.FullScreenActivity
import com.theoplayer.android.api.fullscreen.FullScreenManager
import com.theoplayer.android.api.player.Player

class CustomFullScreenActivity : FullScreenActivity() {
    private lateinit var playPauseButton: MaterialButton
    private lateinit var theoPlayer: Player
    private lateinit var theoFullScreenManager: FullScreenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Adding support for extended AppCompat features.
        // It allows to use styles and themes defined for material components.
        delegate.installViewFactory()
        delegate.onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)

        // Inflating custom view.
        val view = LayoutInflater.from(this).inflate(R.layout.activity_fullscreen, null, false)
        delegate.addContentView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Gathering THEO objects references.
        theoPlayer = theOplayerView!!.player
        theoFullScreenManager = theOplayerView!!.fullScreenManager

        // Configuring UI behavior.
        playPauseButton = view.findViewById(R.id.playPauseButton)
        val exitFullScreenButton = view.findViewById<MaterialButton>(R.id.exitFullScreenButton)

        adjustPlayPauseButtonIcon()
        playPauseButton.setOnClickListener { onPlayPauseClick() }
        exitFullScreenButton.setOnClickListener { onFullScreenExit() }

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
            playPauseButton.setIconResource(com.theoplayer.sample.common.R.drawable.ic_play)
        } else {
            playPauseButton.setIconResource(com.theoplayer.sample.common.R.drawable.ic_pause)
        }
    }
}
