package com.theoplayer.sample.open_video_ui.nitflex

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.theoplayer.android.ui.SeekBar

@Composable
fun NitflexSeekBar(
    modifier: Modifier = Modifier
) {
    SeekBar(
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
        )
    )
}
