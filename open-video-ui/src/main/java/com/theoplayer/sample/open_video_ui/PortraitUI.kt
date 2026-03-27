package com.theoplayer.sample.open_video_ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.CurrentTimeDisplay
import com.theoplayer.android.ui.ErrorDisplay
import com.theoplayer.android.ui.LoadingSpinner
import com.theoplayer.android.ui.MuteButton
import com.theoplayer.android.ui.PlayButton
import com.theoplayer.android.ui.SeekBar
import com.theoplayer.android.ui.UIController
import com.theoplayer.android.ui.rememberPlayer

@Composable
fun PortraitUI(
    modifier: Modifier = Modifier,
    config: THEOplayerConfig,
    source: SourceDescription? = null
) {
    val player = rememberPlayer(config)
    LaunchedEffect(player, source) {
        player.source = source
    }

    ProvideTextStyle(value = TextStyle(color = Color.White)) {
        UIController(
            modifier = modifier,
            player = player,
            centerOverlay = {
                LoadingSpinner()
            },
            topChrome = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    MuteButton()
                }
            },
            centerChrome = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    PlayButton(iconModifier = Modifier.size(64.dp))
                }
            },
            bottomChrome = {
                // Side buttons aligned to the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                    ) {
                        IconButton(onClick = { /* like() */ }) {
                            Icon(
                                Icons.Filled.ThumbUp,
                                contentDescription = "Like",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(onClick = { /* comment() */ }) {
                            Icon(
                                Icons.AutoMirrored.Filled.Comment,
                                contentDescription = "Comment",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(onClick = { /* share() */ }) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                // Seekbar + time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SeekBar(
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                        )
                    )
                    CurrentTimeDisplay(showRemaining = true)
                }
            },
            errorOverlay = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorDisplay()
                }
            }
        )
    }
}
