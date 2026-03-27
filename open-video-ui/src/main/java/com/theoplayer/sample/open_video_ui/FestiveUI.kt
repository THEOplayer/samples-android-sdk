package com.theoplayer.sample.open_video_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.CurrentTimeDisplay
import com.theoplayer.android.ui.DurationDisplay
import com.theoplayer.android.ui.ErrorDisplay
import com.theoplayer.android.ui.FullscreenButton
import com.theoplayer.android.ui.LoadingSpinner
import com.theoplayer.android.ui.MuteButton
import com.theoplayer.android.ui.PlayButton
import com.theoplayer.android.ui.SeekBar
import com.theoplayer.android.ui.UIController
import com.theoplayer.android.ui.rememberPlayer

private val FestiveWhite = Color.White
private val FestiveRed = Color(0xFFFF0000)
private val FestiveGreen = Color(0xFF006400)
private val FestiveGreenTransparent = Color(0x00006400)
private val CandyStripeWhite = Color(0x66FFFFFF)

@Composable
fun FestiveUI(
    modifier: Modifier = Modifier,
    config: THEOplayerConfig,
    source: SourceDescription? = null,
    title: String? = null
) {
    val player = rememberPlayer(config)
    LaunchedEffect(player, source) {
        player.source = source
    }

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
                    .padding(start = 12.dp, top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                title?.let {
                    Text(
                        text = it,
                        color = FestiveWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                }
                MuteButton()
            }
        },
        centerChrome = {
            PlayButton(iconModifier = Modifier.size(48.dp))
        },
        bottomChrome = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(FestiveGreenTransparent, FestiveGreen)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FestiveSeekBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CurrentTimeDisplay()
                            Text(text = " / ", color = FestiveWhite)
                            DurationDisplay()
                        }
                        FullscreenButton()
                    }
                }
            }
        },
        errorOverlay = {
            Box(contentAlignment = Alignment.Center) {
                ErrorDisplay()
            }
        }
    )
}

@Composable
fun FestiveSeekBar(modifier: Modifier = Modifier) {
    SeekBar(
        modifier = modifier
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                val stripeWidth = 4.dp.toPx()
                val step = stripeWidth * 2
                val count = ((size.width + size.height) / step).toInt() + 1
                for (i in 0..count) {
                    val x = i * step
                    drawLine(
                        color = CandyStripeWhite,
                        start = Offset(x, 0f),
                        end = Offset(x - size.height, size.height),
                        strokeWidth = stripeWidth,
                        blendMode = BlendMode.SrcAtop
                    )
                }
            },
        colors = SliderDefaults.colors(
            thumbColor = FestiveRed,
            activeTrackColor = FestiveRed,
            inactiveTrackColor = FestiveGreen.copy(0.2F),
        )
    )
}
