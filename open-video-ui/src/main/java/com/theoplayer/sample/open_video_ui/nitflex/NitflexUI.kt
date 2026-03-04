package com.theoplayer.sample.open_video_ui.nitflex

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.SkipNext
import androidx.compose.material.icons.sharp.Speed
import androidx.compose.material.icons.sharp.Subtitles
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.CurrentTimeDisplay
import com.theoplayer.android.ui.ErrorDisplay
import com.theoplayer.android.ui.FullscreenButton
import com.theoplayer.android.ui.LanguageMenu
import com.theoplayer.android.ui.LoadingSpinner
import com.theoplayer.android.ui.PlaybackRateMenu
import com.theoplayer.android.ui.Player
import com.theoplayer.android.ui.UIController
import com.theoplayer.android.ui.rememberPlayer

@Composable
fun NitflexUI(
    modifier: Modifier = Modifier,
    config: THEOplayerConfig,
    source: SourceDescription? = null,
    title: String? = null
) {
    val player = rememberPlayer(config)
    LaunchedEffect(player, source) {
        player.source = source
    }

    NitflexUI(modifier = modifier, player = player, title = title)
}

@Composable
fun NitflexUI(
    modifier: Modifier = Modifier,
    player: Player = rememberPlayer(),
    title: String? = null
) {
    ProvideTextStyle(value = TextStyle(color = Color.White)) {
        UIController(
            modifier = modifier,
            player = player,
            centerOverlay = {
                if (player.firstPlay) {
                    LoadingSpinner()
                }
            },
            topChrome = {
                if (player.firstPlay) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        title?.let {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = it,
                                textAlign = TextAlign.Center,
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            },
            centerChrome = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (player.firstPlay) {
                        NitflexSeekButton(
                            seekOffset = -10,
                            iconSize = 48.dp,
                            contentPadding = PaddingValues(8.dp)
                        )
                    }
                    NitflexPlayButton(
                        iconModifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(8.dp)
                    )
                    if (player.firstPlay) {
                        NitflexSeekButton(
                            seekOffset = 10,
                            iconSize = 48.dp,
                            contentPadding = PaddingValues(8.dp)
                        )
                    }
                }
            },
            bottomChrome = {
                if (player.firstPlay) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NitflexSeekBar(modifier = Modifier.weight(1f))
                        CurrentTimeDisplay(showRemaining = true)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            shape = MaterialTheme.shapes.small,
                            onClick = { openMenu { PlaybackRateMenu() } }) {
                            Icon(
                                Icons.Sharp.Speed,
                                tint = Color.White,
                                contentDescription = null
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Speed")
                        }
                        TextButton(
                            shape = MaterialTheme.shapes.small,
                            onClick = { openMenu { LanguageMenu() } }) {
                            Icon(
                                Icons.Sharp.Subtitles,
                                tint = Color.White,
                                contentDescription = null
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Audio/subtitles")
                        }
                        TextButton(
                            shape = MaterialTheme.shapes.small,
                            onClick = { /* TODO */ }) {
                            Icon(
                                Icons.Sharp.SkipNext,
                                tint = Color.White,
                                contentDescription = null
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Next")
                        }
                    }
                }
            },
            errorOverlay = {
                Box(contentAlignment = Alignment.Center) {
                    ErrorDisplay()
                    if (player.fullscreen) {
                        FullscreenButton(modifier = Modifier.align(Alignment.BottomEnd))
                    }
                }
            }
        )
    }
}
