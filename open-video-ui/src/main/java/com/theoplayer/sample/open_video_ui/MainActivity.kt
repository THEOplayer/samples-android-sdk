package com.theoplayer.sample.open_video_ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.theoplayer.sample.common.AppTopBar
import com.theoplayer.sample.common.SourceManager
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                MainScreen()
            }
        }
    }
}

private val themeAccentColors = mapOf(
    PlayerTheme.DEFAULT to Color(0xFFFFD600),
    PlayerTheme.CUSTOM_COLORS to Color(0xFF6200EA),
    PlayerTheme.NITFLEX to Color(0xFFE50914),
    PlayerTheme.MINIMAL to Color(0xFF90A4AE),
    PlayerTheme.PORTRAIT to Color(0xFFFFFFFF),
    PlayerTheme.FESTIVE to Color(0xFFFF0000),
    PlayerTheme.MODERN to Color(0xFFFF0000),
)

@Serializable
private object Start

@Serializable
private data class Player(val theme: PlayerTheme)

@Composable
private fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    transitionDurationMillis: Int = 250
) {
    SharedTransitionScope { transitionModifier ->
        NavHost(
            modifier = modifier.then(transitionModifier),
            navController = navController,
            startDestination = Start,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(transitionDurationMillis)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(transitionDurationMillis)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(transitionDurationMillis)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(transitionDurationMillis)
                )
            },
        ) {
            composable<Start> {
                Scaffold(
                    topBar = {
                        AppTopBar(
                            sharedTransitionScope = this@SharedTransitionScope,
                            animatedVisibilityScope = this
                        )
                    }
                ) { padding ->
                    ThemeSelectionScreen(
                        modifier = Modifier.padding(padding),
                        onNavigateToPlayer = { theme ->
                            navController.navigate(
                                route = Player(theme = theme)
                            )
                        }
                    )
                }
            }
            composable<Player> { backStackEntry ->
                val player: Player = backStackEntry.toRoute()
                val source = SourceManager.BIG_BUCK_BUNNY_HLS
                val title = "Big Buck Bunny"
                Scaffold(
                    topBar = {
                        AppTopBar(
                            navigateBack = { navController.popBackStack() },
                            sharedTransitionScope = this@SharedTransitionScope,
                            animatedVisibilityScope = this,
                        )
                    }
                ) { padding ->
                    PlayerScreen(
                        modifier = Modifier.padding(padding),
                        source = source,
                        title = title,
                        theme = player.theme
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSelectionScreen(
    modifier: Modifier = Modifier,
    onNavigateToPlayer: (theme: PlayerTheme) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(PlayerTheme.entries.toList()) { theme ->
            ThemeCard(
                theme = theme,
                onClick = { onNavigateToPlayer(theme) }
            )
        }
    }
}

@Composable
private fun ThemeCard(
    theme: PlayerTheme,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = theme.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
