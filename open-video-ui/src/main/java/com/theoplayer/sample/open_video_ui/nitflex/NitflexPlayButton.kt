package com.theoplayer.sample.open_video_ui.nitflex

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Pause
import androidx.compose.material.icons.sharp.PlayArrow
import androidx.compose.material.icons.sharp.Replay
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.theoplayer.android.ui.PlayButton

@Composable
fun NitflexPlayButton(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    PlayButton(
        modifier = modifier,
        iconModifier = iconModifier,
        contentPadding = contentPadding,
        play = {
            Icon(
                Icons.Sharp.PlayArrow,
                modifier = iconModifier,
                tint = Color.White,
                contentDescription = "Play"
            )
        },
        pause = {
            Icon(
                Icons.Sharp.Pause,
                modifier = iconModifier,
                tint = Color.White,
                contentDescription = "Pause"
            )
        },
        replay = {
            Icon(
                Icons.Sharp.Replay,
                modifier = iconModifier,
                tint = Color.White,
                contentDescription = "Replay"
            )
        }
    )
}
