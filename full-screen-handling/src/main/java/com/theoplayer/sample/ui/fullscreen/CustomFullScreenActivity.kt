package com.theoplayer.sample.ui.fullscreen

import android.os.Bundle
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.fullscreen.FullScreenActivity
import com.theoplayer.android.api.fullscreen.FullScreenManager
import com.theoplayer.android.api.player.Player

class CustomFullScreenActivity : FullScreenActivity() {
    private lateinit var theoPlayer: Player
    private lateinit var theoFullScreenManager: FullScreenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gathering THEO objects references.
        theoPlayer = theOplayerView!!.player
        theoFullScreenManager = theOplayerView!!.fullScreenManager

        // Add custom overlay.
        addContentView(
            ComposeView(this).apply {
                setContent {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        FullScreenOverlay(
                            player = theoPlayer,
                            fullScreenManager = theoFullScreenManager
                        )
                    }
                }
            },
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }
}

@Composable
fun FullScreenOverlay(
    player: Player,
    fullScreenManager: FullScreenManager
) {
    val spaceMargin = dimensionResource(com.theoplayer.sample.common.R.dimen.spaceMargin)
    Row(
        modifier = Modifier.padding(spaceMargin),
        horizontalArrangement = Arrangement.spacedBy(spaceMargin)
    ) {
        PlayButton(player = player)
        ExitFullscreenButton(fullScreenManager = fullScreenManager)
    }
}

@Composable
fun PlayButton(
    modifier: Modifier = Modifier,
    player: Player
) {
    val paused = rememberPaused(player)
    FilledIconButton(
        modifier = modifier,
        onClick = {
            if (player.isPaused) {
                player.play()
            } else {
                player.pause()
            }
        }
    ) {
        Icon(
            painter = painterResource(
                if (paused) {
                    com.theoplayer.sample.common.R.drawable.ic_play
                } else {
                    com.theoplayer.sample.common.R.drawable.ic_pause
                }
            ),
            contentDescription = if (paused) "Play" else "Pause"
        )
    }
}

@Composable
fun ExitFullscreenButton(
    modifier: Modifier = Modifier,
    fullScreenManager: FullScreenManager
) {
    FilledIconButton(
        modifier = modifier,
        onClick = {
            fullScreenManager.exitFullScreen()
        }
    ) {
        Icon(
            painter = painterResource(com.theoplayer.sample.common.R.drawable.ic_fullscreen_exit),
            contentDescription = "Exit fullscreen"
        )
    }
}

/**
 * Returns whether the player is [paused][Player.isPaused].
 * Updates automatically.
 */
@Composable
fun rememberPaused(player: Player): Boolean {
    var paused by remember { mutableStateOf(player.isPaused) }
    DisposableEffect(player) {
        val listener = EventListener<PlayerEvent<*>> { paused = player.isPaused }
        player.addEventListener(PlayerEventTypes.PLAY, listener)
        player.addEventListener(PlayerEventTypes.PAUSE, listener)
        onDispose {
            player.removeEventListener(PlayerEventTypes.PLAY, listener)
            player.removeEventListener(PlayerEventTypes.PAUSE, listener)
        }
    }
    return paused
}
