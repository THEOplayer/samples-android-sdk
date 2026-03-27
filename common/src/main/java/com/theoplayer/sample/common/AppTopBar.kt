package com.theoplayer.sample.common

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.theoplayer.android.api.THEOplayerGlobal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.appDisplayName),
    actions: @Composable RowScope.() -> Unit = {},
    navigateBack: (() -> Unit)? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    TopAppBar(
        modifier = modifier.optionalSharedElement(
            key = "topBar",
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        ),
        title = {
            Text(
                modifier = Modifier.optionalSharedBounds(
                    key = "topBarTitle",
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                text = title
            )
        },
        actions = {
            actions()
            Text(
                modifier = Modifier.optionalSharedBounds(
                    key = "topBarVersion",
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                text = "v" + THEOplayerGlobal.getVersion()
            )
        },
        navigationIcon = {
            if (navigateBack != null) {
                IconButton(
                    modifier = Modifier
                        .optionalSharedElement(
                            key = "topBarBackButton",
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    onClick = navigateBack
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            } else {
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .optionalSharedBounds(
                            key = "topBarBackButton",
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(R.color.dolbyPurple),
            titleContentColor = Color.White,
        ),
    )
}

@Composable
private fun Modifier.optionalSharedElement(
    key: String,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
): Modifier {
    sharedTransitionScope ?: return this
    animatedVisibilityScope ?: return this
    return sharedTransitionScope.run {
        sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@Composable
private fun Modifier.optionalSharedBounds(
    key: String,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
): Modifier {
    sharedTransitionScope ?: return this
    animatedVisibilityScope ?: return this
    return sharedTransitionScope.run {
        sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}
