package com.theoplayer.sample.open_video_ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.theme.THEOplayerTheme
import com.theoplayer.sample.common.SourceManager

class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // Enable all debug logs from THEOplayer.
        val theoDebugLogger = THEOplayerGlobal.getSharedInstance(this).logger
        theoDebugLogger.enableAllTags()

        super.onCreate(savedInstanceState)

        setContent {
            THEOplayerTheme(useDarkTheme = true) {
                Scaffold { padding ->
                    DefaultUI(
                        modifier = Modifier.padding(padding),
                        config = THEOplayerConfig.Builder().build(),
                        source = SourceManager.BIP_BOP_HLS,
                        title = "Big Buck Bunny"
                    )
                }
            }
        }
    }
}
