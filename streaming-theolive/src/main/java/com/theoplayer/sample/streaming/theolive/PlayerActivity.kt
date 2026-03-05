package com.theoplayer.sample.streaming.theolive

import android.util.Log
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.theolive.DistributionLoadStartEvent
import com.theoplayer.android.api.event.player.theolive.DistributionLoadedEvent
import com.theoplayer.android.api.event.player.theolive.EndpointLoadedEvent
import com.theoplayer.android.api.event.player.theolive.TheoLiveEventTypes
import com.theoplayer.android.api.event.track.mediatrack.video.list.AddTrackEvent
import com.theoplayer.android.api.event.track.mediatrack.video.list.VideoTrackListEventTypes
import com.theoplayer.android.api.millicast.MillicastIntegrationFactory
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.rememberPlayer
import com.theoplayer.android.ui.theme.THEOplayerTheme
import com.theoplayer.sample.common.AppTopBar
import com.theoplayer.sample.common.SourceManager

class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable all debug logs from THEOplayer.
        THEOplayerGlobal.getSharedInstance(this).logger.enableAllTags()

        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val theoplayerView = remember(context) {
                // Creating the player with default parameters.
                THEOplayerView(context, THEOplayerConfig.Builder().build()).apply {
                    // Keep the device screen on.
                    keepScreenOn = true
                }
            }
            val player = rememberPlayer(theoplayerView)
            val theoPlayer = theoplayerView.player

            val infoLines = remember { mutableStateOf(listOf<String>()) }
            val latencyText = remember { mutableStateOf("—") }

            LaunchedEffect(player) {

                // Configuring the player with a SourceDescription object.
                theoPlayer.source = SourceManager.THEOLIVE

                //  Set autoplay to start video whenever player is visible.
                theoPlayer.isAutoplay = true

                // Attach event listeners.
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
                theoPlayer.addEventListener(PlayerEventTypes.TIMEUPDATE) {
                    val latency = theoPlayer.latencyManager.currentLatency
                    latencyText.value = latency.toString() + "s"
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
                theoPlayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
                    Log.i(TAG, "Event: ERROR, error=" + event.errorObject)
                }

                // THEOlive events.
                theoPlayer.theoLive.addEventListener(TheoLiveEventTypes.ENDPOINTLOADED) { event: EndpointLoadedEvent ->
                    Log.i(TAG, "Event: ENDPOINTLOADED")
                    val endpoint = event.getEndpoint()
                    infoLines.value += buildList {
                        add("── ENDPOINTLOADED event ──")
                        add("  hespSrc: ${endpoint.hespSrc}")
                        add("  millicastSrc: ${endpoint.millicastSrc}")
                        add("  hlsSrc: ${endpoint.hlsSrc}")
                        add("  adSrc: ${endpoint.adSrc}")
                        add("  daiAssetKey: ${endpoint.daiAssetKey}")
                        add("  cdn: ${endpoint.cdn}")
                        add("  weight: ${endpoint.weight}")
                        add("  priority: ${endpoint.priority}")
                        add("  targetLatency: ${endpoint.targetLatency}")
                        add("  contentProtection: ${endpoint.contentProtection}")
                    }
                }
                theoPlayer.theoLive.addEventListener(TheoLiveEventTypes.INTENTTOFALLBACK) {
                    Log.i(TAG, "Event: INTENTTOFALLBACK")
                    infoLines.value += "Event: INTENTTOFALLBACK"
                }
                theoPlayer.theoLive.addEventListener(TheoLiveEventTypes.DISTRIBUTIONLOADED) { event: DistributionLoadedEvent ->
                    Log.i(TAG, "Event: DISTRIBUTIONLOADED")
                    val distribution = event.getDistribution()
                    infoLines.value += buildList {
                        add("── DISTRIBUTIONLOADED event ──")
                        add("  id: ${distribution.id}")
                        add("  name: ${distribution.name}")
                    }
                }
                theoPlayer.theoLive.addEventListener(TheoLiveEventTypes.DISTRIBUTIONOFFLINE) {
                    Log.i(TAG, "Event: DISTRIBUTIONOFFLINE")
                    infoLines.value += "Event: DISTRIBUTIONOFFLINE"
                }
                theoPlayer.theoLive.addEventListener(TheoLiveEventTypes.DISTRIBUTIONLOADSTART) { event: DistributionLoadStartEvent ->
                    Log.i(TAG, "Event: DISTRIBUTIONLOADSTART")
                    infoLines.value += buildList {
                        add("── DISTRIBUTIONLOADSTART event ──")
                        add("  id: ${event.getDistributionId()}")
                    }
                }
            }

            THEOplayerTheme(useDarkTheme = true) {
                Scaffold(
                    topBar = { AppTopBar() }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .background(Color(0xFF121212))
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DefaultUI(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f),
                                player = player
                            )
                        }

                        HorizontalDivider(color = Color(0xFF333333))

                        // Info panel.
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "THEOlive API Info",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                                Text(
                                    text = "Latency: ${latencyText.value}",
                                    color = Color(0xFF8AB4F8),
                                    fontSize = 13.sp,
                                )
                            }
                            if (infoLines.value.isEmpty()) {
                                Text(
                                    text = "Waiting for events…",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                )
                            } else {
                                for (line in infoLines.value) {
                                    Text(
                                        text = line,
                                        color = if (line.startsWith("──")) Color(0xFF8AB4F8) else Color(0xFFCCCCCC),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(vertical = 0.5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val TAG: String = PlayerActivity::class.java.simpleName
    }
}
