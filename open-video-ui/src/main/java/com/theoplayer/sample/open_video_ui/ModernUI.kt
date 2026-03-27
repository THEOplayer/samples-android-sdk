package com.theoplayer.sample.open_video_ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.CurrentTimeDisplay
import com.theoplayer.android.ui.DurationDisplay
import com.theoplayer.android.ui.ErrorDisplay
import com.theoplayer.android.ui.FullscreenButton
import com.theoplayer.android.ui.LanguageMenu
import com.theoplayer.android.ui.LoadingSpinner
import com.theoplayer.android.ui.PlayButton
import com.theoplayer.android.ui.SeekBar
import com.theoplayer.android.ui.SeekButton
import com.theoplayer.android.ui.UIController
import com.theoplayer.android.ui.rememberPlayer

private val TubeRed = Color(0xFFFF0000)
private val PillBackground = Color(0x59000000)
private val PillShape = RoundedCornerShape(50)

@Composable
fun ModernUI(
    modifier: Modifier = Modifier,
    config: THEOplayerConfig,
    source: SourceDescription? = null,
    title: String? = null
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
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    title?.let {
                        Text(
                            text = it,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            centerChrome = {
                // Center pill: rewind, play/pause, forward
                Surface(
                    shape = PillShape,
                    color = PillBackground
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        SeekButton(seekOffset = -10) {
                            Icon(
                                Icons.Filled.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        PlayButton(iconModifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        SeekButton(seekOffset = 10) {
                            Icon(
                                Icons.Filled.Forward10,
                                contentDescription = "Forward 10s",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            },
            bottomChrome = {
                // Seekbar
                SeekBar(
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = TubeRed,
                        activeTrackColor = TubeRed,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                    )
                )
                // Bottom row: time on left, pill buttons on right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time pill
                    Surface(
                        shape = PillShape,
                        color = PillBackground
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CurrentTimeDisplay()
                            Text(text = " / ")
                            DurationDisplay()
                        }
                    }
                    // Controls pill
                    Surface(
                        shape = PillShape,
                        color = PillBackground
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { openMenu { LanguageMenu() } }) {
                                Icon(
                                    Icons.Filled.ClosedCaption,
                                    contentDescription = "Subtitles",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            FullscreenButton()
                        }
                    }
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
