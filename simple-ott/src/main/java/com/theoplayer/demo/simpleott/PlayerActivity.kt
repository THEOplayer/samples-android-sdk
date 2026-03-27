package com.theoplayer.demo.simpleott

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.metadata.ChromecastMetadataDescription
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.rememberPlayer
import com.theoplayer.android.ui.theme.THEOplayerTheme
import com.theoplayer.demo.simpleott.model.StreamSource
import com.theoplayer.sample.common.AppTopBar

class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        THEOplayerGlobal.getSharedInstance(this).logger.enableAllTags()
        super.onCreate(savedInstanceState)

        val sourceUrl = intent.getStringExtra(PLAYER_PARAM__SOURCE_URL) ?: ""
        val title = intent.getStringExtra(PLAYER_PARAM__TITLE) ?: ""
        val description = intent.getStringExtra(PLAYER_PARAM__DESCRIPTION) ?: ""

        setContent {
            val theoplayerView = remember {
                THEOplayerView(this, THEOplayerConfig.Builder().build()).apply {
                    keepScreenOn = true
                }
            }
            val player = rememberPlayer(theoplayerView)
            val theoPlayer = theoplayerView.player

            var currentSourceUrl by remember { mutableStateOf(sourceUrl) }
            var currentTitle by remember { mutableStateOf(title) }
            var currentDescription by remember { mutableStateOf(description) }

            // One-time setup: event listeners
            LaunchedEffect(player) {
                theoplayerView.fullScreenManager.isFullScreenOrientationCoupled = true

                theoPlayer.addEventListener(PlayerEventTypes.SOURCECHANGE) {
                    Log.i(TAG, "Event: SOURCECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.CURRENTSOURCECHANGE) {
                    Log.i(TAG, "Event: CURRENTSOURCECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.LOADEDDATA) {
                    Log.i(TAG, "Event: LOADEDDATA")
                }
                theoPlayer.addEventListener(PlayerEventTypes.LOADEDMETADATA) {
                    Log.i(TAG, "Event: LOADEDMETADATA")
                }
                theoPlayer.addEventListener(PlayerEventTypes.DURATIONCHANGE) {
                    Log.i(TAG, "Event: DURATIONCHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.PLAY) {
                    Log.i(TAG, "Event: PLAY")
                }
                theoPlayer.addEventListener(PlayerEventTypes.PLAYING) {
                    Log.i(TAG, "Event: PLAYING")
                }
                theoPlayer.addEventListener(PlayerEventTypes.PAUSE) {
                    Log.i(TAG, "Event: PAUSE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.SEEKING) {
                    Log.i(TAG, "Event: SEEKING")
                }
                theoPlayer.addEventListener(PlayerEventTypes.SEEKED) {
                    Log.i(TAG, "Event: SEEKED")
                }
                theoPlayer.addEventListener(PlayerEventTypes.WAITING) {
                    Log.i(TAG, "Event: WAITING")
                }
                theoPlayer.addEventListener(PlayerEventTypes.READYSTATECHANGE) {
                    Log.i(TAG, "Event: READYSTATECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.PRESENTATIONMODECHANGE) {
                    Log.i(TAG, "Event: PRESENTATIONMODECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.VOLUMECHANGE) {
                    Log.i(TAG, "Event: VOLUMECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.ENDED) {
                    Log.i(TAG, "Event: ENDED")
                }
                theoPlayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
                    Log.i(TAG, "Event: ERROR, error=" + event.errorObject)
                }
            }

            // Set source whenever it changes
            LaunchedEffect(currentSourceUrl) {
                theoPlayer.source = SourceDescription.Builder(
                    TypedSource.Builder(currentSourceUrl).build()
                )
                    .metadata(
                        ChromecastMetadataDescription.Builder()
                            .title(currentTitle)
                            .build()
                    )
                    .build()
                theoPlayer.isAutoplay = true
            }

            val relatedSources = remember(currentSourceUrl) {
                MainActivity.ON_DEMAND_SOURCES.filter { it.source != currentSourceUrl }
            }

            THEOplayerTheme(useDarkTheme = true) {
                Scaffold(
                    topBar = { AppTopBar() }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        DefaultUI(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            player = player
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            item {
                                Text(
                                    text = currentTitle,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            if (currentDescription.isNotEmpty()) {
                                item {
                                    Text(
                                        text = currentDescription,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                }
                            }

                            item {
                                Text(
                                    text = stringResource(R.string.relatedContent),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(relatedSources) { source ->
                                RelatedContentItem(source) {
                                    currentSourceUrl = source.source
                                    currentTitle = source.title
                                    currentDescription = source.description
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
        private const val PLAYER_PARAM__SOURCE_URL = "SOURCE_URL"
        private const val PLAYER_PARAM__TITLE = "TITLE"
        private const val PLAYER_PARAM__DESCRIPTION = "DESCRIPTION"

        fun play(context: Context, sourceUrl: String, title: String, description: String = "") {
            val playIntent = Intent(context, PlayerActivity::class.java)
            playIntent.putExtra(PLAYER_PARAM__SOURCE_URL, sourceUrl)
            playIntent.putExtra(PLAYER_PARAM__TITLE, title)
            playIntent.putExtra(PLAYER_PARAM__DESCRIPTION, description)
            context.startActivity(playIntent)
        }
    }
}

@Composable
private fun RelatedContentItem(source: StreamSource, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(source.imageResId),
                contentDescription = source.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = source.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Text(
                    text = source.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}
