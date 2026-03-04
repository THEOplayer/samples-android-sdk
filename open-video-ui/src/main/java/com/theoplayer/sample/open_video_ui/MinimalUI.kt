package com.theoplayer.sample.open_video_ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.CurrentTimeDisplay
import com.theoplayer.android.ui.DurationDisplay
import com.theoplayer.android.ui.PlayButton
import com.theoplayer.android.ui.SeekBar
import com.theoplayer.android.ui.UIController
import com.theoplayer.android.ui.rememberPlayer

@Composable
fun MinimalUI(
    modifier: Modifier = Modifier,
    config: THEOplayerConfig,
    source: SourceDescription? = null
) {
    val player = rememberPlayer(config)
    LaunchedEffect(player, source) {
        player.source = source
    }

    UIController(
        modifier = modifier,
        player = player,
        centerChrome = {
            PlayButton(iconModifier = Modifier.size(48.dp))
        },
        bottomChrome = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CurrentTimeDisplay()
                SeekBar(modifier = Modifier.weight(1f))
                DurationDisplay()
            }
        }
    )
}
