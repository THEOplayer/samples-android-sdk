package com.theoplayer.sample.playback.metadata

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.TimeUpdateEvent
import com.theoplayer.android.api.event.track.texttrack.AddCueEvent
import com.theoplayer.android.api.event.track.texttrack.EnterCueEvent
import com.theoplayer.android.api.event.track.texttrack.TextTrackEventTypes
import com.theoplayer.android.api.event.track.texttrack.list.AddTrackEvent
import com.theoplayer.android.api.event.track.texttrack.list.TextTrackListEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.texttrack.TextTrackType
import com.theoplayer.android.api.player.track.texttrack.cue.DateRangeCue
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.sample.playback.metadata.databinding.ActivityPlayerBinding
import org.json.JSONException
import java.io.ByteArrayOutputStream

class PlayerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayer: Player
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.player

        // Enable all debug logs from THEOplayer.
        val theoDebugLogger = THEOplayerGlobal.getSharedInstance(this).logger
        theoDebugLogger.enableAllTags()

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        when (intent.getIntExtra(PLAYER_PARAM__METADATA_ID, 0)) {
            R.string.hlsWithID3MetadataName -> handleHlsWithID3Metadata()
            R.string.hlsWithProgramDateTimeMetadataName -> handleHlsWithProgramDateTimeMetadata()
            R.string.hlsWithDateRangeMetadataName -> handleHlsWithDateRangeMetadata()
            R.string.dashWithEmsgMetadataName -> handleDashWithEmsgMetadata()
            R.string.dashWithEventStreamMetadataName -> handleDashWithEventStreamMetadata()
            else -> {
                val toastMessage =
                    SpannableString.valueOf(this.getString(R.string.missingMetadataConfiguration))
                toastMessage.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                    0,
                    toastMessage.length,
                    0
                )
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Demonstrates THEOplayer configuration that allows to handle ID3 metadata from HLS stream.
     */
    private fun handleHlsWithID3Metadata() {
        viewBinding.headerTextView.text = getString(R.string.hlsWithID3MetadataHeader)

        // Configuring THEOplayer with appropriate stream source.
        configureTHEOplayer(
            TypedSource.Builder(getString(R.string.hlsWithID3MetadataSourceUrl))
        )

        // Listening to 'addtrack' events to find text track of type 'id3'.
        theoPlayer.textTracks.addEventListener(TextTrackListEventTypes.ADDTRACK) { event: AddTrackEvent ->
            if (event.track.type == TextTrackType.ID3) {
                Log.i(TAG, "Event: ADDTRACK, trackType=" + event.track.type)

                // Listening to 'entercue' event to find the current cue.
                event.track.addEventListener(TextTrackEventTypes.ENTERCUE) { cueEvent: EnterCueEvent ->
                    Log.i(TAG, "Event: ENTERCUE, cue=" + cueEvent.cue)

                    // Decoding ID3 metadata. In this example, the data received
                    // is in the form: '{"content":{"id":"TXXX","description":"","text":"..."}}}'.
                    val cueContent = cueEvent.cue.content
                    try {
                        appendMetadata(cueContent!!.getJSONObject("content").getString("text"))
                    } catch (exception: JSONException) {
                        appendMetadata(cueContent.toString())
                        appendMetadata(cueContent.toString())
                    }
                }
            }
        }
    }

    /**
     * Demonstrates THEOplayer configuration that allows to handle EXT-X-PROGRAM-DATE-TIME metadata from HLS stream.
     */
    private fun handleHlsWithProgramDateTimeMetadata() {
        viewBinding.headerTextView.text = getString(R.string.hlsWithProgramDateTimeMetadataHeader)

        // Configuring THEOplayer with appropriate stream source.
        configureTHEOplayer(
            TypedSource.Builder(getString(R.string.hlsWithProgramDateTimeMetadataSourceUrl))
        )

        // Listening to 'timeupdate' events that are triggered every time EXT-X-PROGRAM-DATE-TIME
        // is updated.
        theoPlayer.addEventListener(PlayerEventTypes.TIMEUPDATE) { event: TimeUpdateEvent ->
            Log.i(TAG, "Event: TIMEUPDATE, currentTime=" + event.currentTime)

            // Once we know that EXT-X-PROGRAM-DATE-TIME was updated we have to request for its value.
            appendMetadata(theoPlayer.currentProgramDateTime?.toString() ?: "")
        }
    }

    /**
     * Demonstrates THEOplayer configuration that allows to handle EXT-X-DATERANGE metadata from HLS stream.
     */
    private fun handleHlsWithDateRangeMetadata() {
        viewBinding.headerTextView.text = getString(R.string.hlsWithDateRangeMetadataHeader)

        // Configuring THEOplayer with appropriate stream source. Note that logic that exposes date
        // ranges parsed from HLS manifest needs to be enabled.
        configureTHEOplayer(
            TypedSource.Builder(getString(R.string.hlsWithDateRangeMetadataSourceUrl))
                .hlsDateRange(true)
        )

        // Listening to 'addtrack' events to find text track of type 'daterange'.
        theoPlayer.textTracks.addEventListener(TextTrackListEventTypes.ADDTRACK) { event: AddTrackEvent ->
            if (event.track.type == TextTrackType.DATERANGE) {
                Log.i(TAG, "Event: ADDTRACK, trackType=" + event.track.type)

                // Listening to 'addcue' event to get parsed date range.
                event.track.addEventListener(TextTrackEventTypes.ADDCUE) { cueEvent: AddCueEvent ->
                    val cue = cueEvent.cue as DateRangeCue
                    Log.i(TAG, "Event: ADDCUE, cue=$cue")

                    // Decoding date range metadata. For demo purposes we are displaying
                    // content as it is encoding byte arrays with base64.
                    appendMetadata(
                        """
    StartDate: ${cue.startDate}
    EndDate: ${cue.endDate}
    Duration: ${cue.duration}
    Scte35Cmd: ${
                            if (cue.scte35Cmd != null) Base64.encodeToString(
                                cue.scte35Cmd,
                                Base64.NO_WRAP
                            ) else "N/A"
                        }
    Scte35In: ${
                            if (cue.scte35In != null) Base64.encodeToString(
                                cue.scte35In,
                                Base64.NO_WRAP
                            ) else "N/A"
                        }
    Scte35Out: ${
                            if (cue.scte35Out != null) Base64.encodeToString(
                                cue.scte35Out,
                                Base64.NO_WRAP
                            ) else "N/A"
                        }
    """.trimIndent()
                    )
                }
            }
        }
    }

    /**
     * Demonstrates THEOplayer configuration that allows to handle EMSG metadata from DASH stream.
     */
    private fun handleDashWithEmsgMetadata() {
        viewBinding.headerTextView.text = getString(R.string.dashWithEmsgMetadataHeader)

        // Configuring THEOplayer with appropriate stream source.
        configureTHEOplayer(
            TypedSource.Builder(getString(R.string.dashWithEmsgMetadataSourceUrl))
        )

        // Listening to 'addtrack' events to find text track of type 'emsg'.
        theoPlayer.textTracks.addEventListener(TextTrackListEventTypes.ADDTRACK) { event: AddTrackEvent ->
            if (event.track.type == TextTrackType.EMSG) {
                Log.i(TAG, "Event: ADDTRACK, trackType=" + event.track.type)

                // Listening to 'addcue' event to read EMSG metadata.
                event.track.addEventListener(TextTrackEventTypes.ADDCUE) { cueEvent: AddCueEvent ->
                    Log.i(TAG, "Event: ADDCUE, cue=" + cueEvent.cue)

                    // Decoding EMSG metadata. In this example, the data received
                    // is in the form: '{"content":{"0":73,"1":68,"2":51,"3":4,"4":0,"5":32,...}'.
                    val cueContent = cueEvent.cue.content
                    try {
                        val byteContent = ByteArrayOutputStream()
                        val jsonContent = cueContent!!.getJSONObject("content")
                        val jsonContentKeys = jsonContent.keys()
                        while (jsonContentKeys.hasNext()) {
                            byteContent.write(jsonContent.getInt(jsonContentKeys.next()))
                        }
                        appendMetadata(String(byteContent.toByteArray()))
                    } catch (e: JSONException) {
                        appendMetadata(cueContent.toString())
                    }
                }
            }
        }
    }

    /**
     * Demonstrates THEOplayer configuration that allows to handle EventStream metadata from DASH stream.
     */
    private fun handleDashWithEventStreamMetadata() {
        viewBinding.headerTextView.text = getString(R.string.dashWithEventStreamMetadataHeader)

        // Configuring THEOplayer with appropriate stream source.
        configureTHEOplayer(
            TypedSource.Builder(getString(R.string.dashWithEventStreamMetadataSourceUrl))
        )

        // Listening to 'addtrack' events to find text track of type 'eventstream'.
        theoPlayer.textTracks.addEventListener(TextTrackListEventTypes.ADDTRACK) { event: AddTrackEvent ->
            if (event.track.type == TextTrackType.EVENTSTREAM) {
                Log.i(TAG, "Event: ADDTRACK, trackType=" + event.track.type)

                // Listening to 'addcue' event to read EventStream metadata.
                event.track.addEventListener(TextTrackEventTypes.ADDCUE) { cueEvent: AddCueEvent ->
                    Log.i(TAG, "Event: ADDCUE, cue=" + cueEvent.cue)

                    // For demo purposes we are displaying whole content as it is.
                    appendMetadata(cueEvent.cue.content.toString())
                }
            }
        }
    }

    private fun configureTHEOplayer(typedSource: TypedSource.Builder) {
        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        val sourceDescription = SourceDescription.Builder(typedSource.build())

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.source = sourceDescription.build()
        theoPlayer.isAutoplay = true

        // Adding listeners to THEOplayer basic playback events.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY) { Log.i(TAG, "Event: PLAY") }
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING) { Log.i(TAG, "Event: PLAYING") }
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE) { Log.i(TAG, "Event: PAUSE") }
        theoPlayer.addEventListener(PlayerEventTypes.ENDED) { Log.i(TAG, "Event: ENDED") }
        theoPlayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
            Log.i(TAG, "Event: ERROR, error=" + event.errorObject)
        }
    }

    private fun appendMetadata(metadata: String) {
        if (viewBinding.metadataTextView.length() == 0) {
            viewBinding.metadataTextView.text = metadata
        } else {
            viewBinding.metadataTextView.append(
                """
    
    
    $metadata
    """.trimIndent()
            )
        }

        // If metadata content was scrolled to bottom then scroll down automatically after adding new content.
        val fullScrollHeight =
            viewBinding.metadataScrollView.scrollY + viewBinding.metadataScrollView.height
        val metadataHeight = viewBinding.metadataTextView.height
        if (metadataHeight <= fullScrollHeight) {
            viewBinding.metadataScrollView.post { viewBinding.metadataScrollView.fullScroll(View.FOCUS_DOWN) }
        }
    }

    // In order to work properly and in sync with the activity lifecycle changes (e.g. device
    // is rotated, new activity is started or app is moved to background) we need to call
    // the "onResume", "onPause" and "onDestroy" methods of the THEOplayerView when the matching
    // activity methods are called.
    override fun onPause() {
        super.onPause()
        viewBinding.theoPlayerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        viewBinding.theoPlayerView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding.theoPlayerView.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val constraintSet = ConstraintSet()
        constraintSet.clone(viewBinding.mainConstraintLayout)
        val headerTextViewId = viewBinding.headerTextView.id
        val theoPlayerViewId = viewBinding.theoPlayerView.id
        val metadataLabelTextViewId = viewBinding.metadataLabelTextView.id
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape orientation metadataLabelTextView (with attached metadataTextView)
            // is placed on the right of headerTextView (with attached theoPlayerView).
            // Additionally metadataLabelTextView width is shrunk to 40% of screen width and its
            // vertical position is centered.
            constraintSet.constrainPercentWidth(headerTextViewId, 0.4f)
            constraintSet.setVerticalBias(headerTextViewId, 0.5f)
            constraintSet.connect(
                metadataLabelTextViewId,
                ConstraintSet.START,
                headerTextViewId,
                ConstraintSet.END
            )
            constraintSet.connect(
                metadataLabelTextViewId,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP
            )
        } else {
            // In portrait orientation metadataLabelTextView (with attached metadataTextView)
            // is placed under the theoPlayerView.
            // Additionally metadataLabelTextView width is expanded to full screen width and its
            // vertical alignment is set to top.
            constraintSet.constrainPercentWidth(headerTextViewId, 1f)
            constraintSet.setVerticalBias(headerTextViewId, 0f)
            constraintSet.connect(
                metadataLabelTextViewId,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            constraintSet.connect(
                metadataLabelTextViewId,
                ConstraintSet.TOP,
                theoPlayerViewId,
                ConstraintSet.BOTTOM
            )
        }
        constraintSet.applyTo(viewBinding.mainConstraintLayout)

        // After orientation change metadata content scroll position is adjusted so it could be
        // scrolled automatically when new content is appended.
        viewBinding.metadataScrollView.post { viewBinding.metadataScrollView.fullScroll(View.FOCUS_DOWN) }
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