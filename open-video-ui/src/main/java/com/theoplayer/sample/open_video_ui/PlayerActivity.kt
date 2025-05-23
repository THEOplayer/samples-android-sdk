package com.theoplayer.sample.open_video_ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.theme.THEOplayerTheme
import com.theoplayer.sample.common.SourceManager

class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            THEOplayerTheme(useDarkTheme = true) {
                Surface {
                    DefaultUI(
                        config = THEOplayerConfig.Builder().build(),
                        source = SourceManager.BIP_BOP_HLS,
                        title = "Big Buck Bunny"
                    )
                }
            }
        }
    }
}
