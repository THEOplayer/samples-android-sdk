package com.theoplayer.sample.open_video_ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
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
                var colorScheme by remember { mutableStateOf<ColorScheme?>(null) }
                MaterialTheme(
                    colorScheme = colorScheme ?: MaterialTheme.colorScheme
                ) {
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
                            modifier = Modifier
                                .padding(padding)
                                .fillMaxSize(),
                            source = source,
                            title = title,
                            theme = player.theme,
                            onColorSchemeChange = { colorScheme = it }
                        )
                    }
                }
            }
        }
    }
}
