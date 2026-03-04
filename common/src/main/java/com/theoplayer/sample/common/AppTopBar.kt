package com.theoplayer.sample.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.theoplayer.android.api.THEOplayerGlobal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String = stringResource(R.string.appDisplayName),
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        actions = {
            actions()
            Text(text = "v" + THEOplayerGlobal.getVersion())
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(R.color.dolbyPurple),
            titleContentColor = Color.White,
        ),
    )
}
