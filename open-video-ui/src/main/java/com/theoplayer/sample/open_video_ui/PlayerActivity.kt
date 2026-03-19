package com.theoplayer.sample.open_video_ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.theme.THEOplayerTheme
import com.theoplayer.sample.common.AppTopBar
import com.theoplayer.sample.common.SourceManager
import com.theoplayer.sample.open_video_ui.nitflex.NitflexUI
import com.theoplayer.sample.open_video_ui.nitflex.theme.NitflexTheme

@Composable
fun PlayerScreen(
    source: SourceDescription,
    title: String,
    theme: PlayerTheme = PlayerTheme.DEFAULT,
    navigateBack: (() -> Unit)? = null
) {
    val config = THEOplayerConfig.Builder().build()
    when (theme) {
        PlayerTheme.DEFAULT -> {
            THEOplayerTheme(useDarkTheme = true) {
                Scaffold(topBar = { AppTopBar(navigateBack = navigateBack) }) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        DefaultUI(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            config = config,
                            source = source,
                            title = title
                        )
                    }
                }
            }
        }

        PlayerTheme.CUSTOM_COLORS -> {
            CustomColorsScreen(
                config = config,
                source = source,
                title = title,
                navigateBack = navigateBack
            )
        }

        PlayerTheme.NITFLEX -> {
            NitflexTheme(useDarkTheme = true) {
                Scaffold(topBar = { AppTopBar(navigateBack = navigateBack) }) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        NitflexUI(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            config = config,
                            source = source,
                            title = title
                        )
                    }
                }
            }
        }

        PlayerTheme.MINIMAL -> {
            THEOplayerTheme(useDarkTheme = true) {
                Scaffold(topBar = { AppTopBar(navigateBack = navigateBack) }) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        MinimalUI(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            config = config,
                            source = source
                        )
                    }
                }
            }
        }

        PlayerTheme.PORTRAIT -> {
            THEOplayerTheme(useDarkTheme = true) {
                Scaffold(topBar = { AppTopBar(navigateBack = navigateBack) }) { padding ->
                    PortraitUI(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        config = config,
                        source = SourceManager.SKATING_PORTRAIT_MP4
                    )
                }
            }
        }

        PlayerTheme.FESTIVE -> {
            THEOplayerTheme(useDarkTheme = true) {
                Scaffold(topBar = { AppTopBar(navigateBack = navigateBack) }) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        FestiveUI(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            config = config,
                            source = source,
                            title = title
                        )
                    }
                }
            }
        }

        PlayerTheme.MODERN -> {
            THEOplayerTheme(useDarkTheme = true) {
                Scaffold(topBar = { AppTopBar(navigateBack = navigateBack) }) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        ModernUI(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            config = config,
                            source = source,
                            title = title
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomColorsScreen(
    config: THEOplayerConfig,
    source: SourceDescription,
    title: String,
    navigateBack: (() -> Unit)? = null
) {
    var selectedPreset by remember { mutableStateOf(ColorPreset.ORANGE) }

    CustomColorTheme(preset = selectedPreset) {
        Scaffold(topBar = { AppTopBar(navigateBack = navigateBack) }) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center
            ) {
                DefaultUI(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    config = config,
                    source = source,
                    title = title
                )
                Text(
                    text = "Accent color",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorPreset.entries.filter { !it.fullTheme }.forEach { preset ->
                        ColorSwatch(
                            color = preset.accentColor,
                            label = preset.label,
                            selected = preset == selectedPreset,
                            onClick = { selectedPreset = preset }
                        )
                    }
                }
                Text(
                    text = "Full theme",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorPreset.entries.filter { it.fullTheme }.forEach { preset ->
                        ColorSwatch(
                            color = preset.accentColor,
                            label = preset.label,
                            selected = preset == selectedPreset,
                            onClick = { selectedPreset = preset }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .then(
                    if (selected) Modifier.border(2.dp, Color.White, CircleShape)
                    else Modifier
                ),
            shape = CircleShape,
            color = color,
            content = {}
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
