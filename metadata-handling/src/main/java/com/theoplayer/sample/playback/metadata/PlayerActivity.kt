package com.theoplayer.sample.playback.metadata

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.TimeUpdateEvent
import com.theoplayer.android.api.event.track.texttrack.EnterCueEvent
import com.theoplayer.android.api.event.track.texttrack.TextTrackEventTypes
import com.theoplayer.android.api.event.track.texttrack.list.AddTrackEvent
import com.theoplayer.android.api.event.track.texttrack.list.TextTrackListEventTypes
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.rememberPlayer
import com.theoplayer.android.ui.theme.THEOplayerTheme
import com.theoplayer.sample.common.AppTopBar
import com.theoplayer.sample.common.SourceManager

private data class MetadataOption(
    val nameResId: Int,
    val source: SourceDescription
)

private val METADATA_OPTIONS = listOf(
    MetadataOption(R.string.hlsRadioWithID3MetadataName, SourceManager.HLS_RADIO_WITH_ID3_METADATA),
    MetadataOption(R.string.hlsWithProgramDateTimeMetadataName, SourceManager.HLS_WITH_PROGRAM_DATE_TIME),
    MetadataOption(R.string.hlsWithDateRangeMetadataName, SourceManager.HLS_WITH_DATE_RANGE),
    MetadataOption(R.string.dashWithEmsgMetadataName, SourceManager.DASH_WITH_EMSG),
    MetadataOption(R.string.dashWithScte35MetadataName, SourceManager.DASH_WITH_SCTE35),
)

class PlayerActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable all debug logs from THEOplayer.
        THEOplayerGlobal.getSharedInstance(this).logger.enableAllTags()

        super.onCreate(savedInstanceState)

        val initialMetadataId = intent.getIntExtra(PLAYER_PARAM__METADATA_ID, 0)
        val initialIndex = METADATA_OPTIONS.indexOfFirst { it.nameResId == initialMetadataId }.coerceAtLeast(0)

        setContent {
            val context = LocalContext.current
            val metadataEntries = remember { mutableStateListOf<String>() }
            var selectedIndex by remember { mutableIntStateOf(initialIndex) }
            var dropdownExpanded by remember { mutableStateOf(false) }
            val selectedOption = METADATA_OPTIONS[selectedIndex]

            val theoplayerView = remember(context) {
                THEOplayerView(context, THEOplayerConfig.Builder().build()).apply {
                    keepScreenOn = true
                }
            }

            val player = rememberPlayer(theoplayerView)
            val theoPlayer = theoplayerView.player

            LaunchedEffect(selectedIndex) {
                metadataEntries.clear()
                theoPlayer.source = selectedOption.source
                theoPlayer.isAutoplay = true
            }

            // Attach metadata and player listeners once.
            LaunchedEffect(player) {
                var lastCueStartTime = Double.NaN

                // Text track listener for metadata.
                theoPlayer.textTracks.addEventListener(TextTrackListEventTypes.ADDTRACK) { event: AddTrackEvent ->
                    val track = event.track
                    Log.i(TAG, "Event: ADDTRACK, type=${track.type}, label=${track.label}")

                    track.addEventListener(TextTrackEventTypes.ENTERCUE) { cueEvent: EnterCueEvent ->
                        val cueContent = cueEvent.cue.content ?: return@addEventListener
                        val startTime = cueEvent.cue.startTime
                        val text: String? = try {
                            cueContent.getJSONObject("content").getString("text")
                        } catch (_: Exception) {
                            try { cueContent.toString() } catch (_: Exception) { null }
                        }
                        if (!text.isNullOrEmpty()) {
                            Log.i(TAG, "Event: ENTERCUE, type=${track.type}, content=$text")
                            runOnUiThread {
                                // Group cues with the same startTime into one entry.
                                val lastIndex = metadataEntries.lastIndex
                                if (lastIndex >= 0 && lastCueStartTime == startTime) {
                                    metadataEntries[lastIndex] = metadataEntries[lastIndex] + "\n" + text
                                } else {
                                    metadataEntries.add(text)
                                }
                                lastCueStartTime = startTime
                            }
                        }
                    }
                }

                // Listening to 'timeupdate' events that are triggered every time EXT-X-PROGRAM-DATE-TIME
                // is updated.
                theoPlayer.addEventListener(PlayerEventTypes.TIMEUPDATE) { event: TimeUpdateEvent ->
                    if (METADATA_OPTIONS[selectedIndex].nameResId != R.string.hlsWithProgramDateTimeMetadataName) return@addEventListener
                    Log.i(TAG, "Event: TIMEUPDATE, currentTime=${event.currentTime}, programDateTime=${theoPlayer.currentProgramDateTime}")
                    val dateTime = theoPlayer.currentProgramDateTime?.toString()
                    if (!dateTime.isNullOrEmpty()) {
                        runOnUiThread { metadataEntries.add(dateTime) }
                    }
                }

                // Attach player event listeners.
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

            THEOplayerTheme(useDarkTheme = true) {
                Scaffold(
                    topBar = { AppTopBar() }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            TextField(
                                value = stringResource(selectedOption.nameResId),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.metadataNameLabel)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                METADATA_OPTIONS.forEachIndexed { index, option ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(option.nameResId)) },
                                        onClick = {
                                            selectedIndex = index
                                            dropdownExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }

                        // Player
                        DefaultUI(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            player = player
                        )

                        // Metadata label
                        Text(
                            text = stringResource(R.string.metadataContentLabel),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(12.dp)
                        )

                        // Metadata content
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            val listState = rememberLazyListState()

                            LaunchedEffect(metadataEntries.size) {
                                if (metadataEntries.isNotEmpty()) {
                                    listState.animateScrollToItem(metadataEntries.lastIndex)
                                }
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(metadataEntries) { index, entry ->
                                    if (index > 0) {
                                        HorizontalDivider()
                                    }
                                    Text(
                                        text = entry,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(8.dp)
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
        private val TAG = PlayerActivity::class.java.simpleName
        private const val PLAYER_PARAM__METADATA_ID = "METADATA_ID"

        @JvmStatic
        fun play(context: Context, metadataId: Int) {
            val playIntent = Intent(context, PlayerActivity::class.java)
            playIntent.putExtra(PLAYER_PARAM__METADATA_ID, metadataId)
            context.startActivity(playIntent)
        }
    }
}
