package com.theoplayer.sample.playback.metadata;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.DataBindingUtil;

import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.event.track.texttrack.TextTrackEventTypes;
import com.theoplayer.android.api.event.track.texttrack.list.TextTrackListEventTypes;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.android.api.player.track.texttrack.TextTrackType;
import com.theoplayer.android.api.player.track.texttrack.cue.DateRangeCue;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.TypedSource;
import com.theoplayer.sample.playback.metadata.databinding.ActivityPlayerBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import static android.util.Base64.NO_WRAP;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    private static final String PLAYER_PARAM__METADATA_ID = "METADATA_ID";

    private ActivityPlayerBinding viewBinding;
    private Player theoPlayer;

    public static void play(Context context, int metadataId) {
        Intent playIntent = new Intent(context, PlayerActivity.class);
        playIntent.putExtra(PLAYER_PARAM__METADATA_ID, metadataId);
        context.startActivity(playIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.getPlayer();

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configuring THEOplayer playback with parameters for appropriate metadata handling.
        switch (getIntent().getIntExtra(PLAYER_PARAM__METADATA_ID, 0)) {
            case R.string.hlsWithID3MetadataName:
                handleHlsWithID3Metadata();
                break;
            case R.string.hlsWithProgramDateTimeMetadataName:
                handleHlsWithProgramDateTimeMetadata();
                break;
            case R.string.hlsWithDateRangeMetadataName:
                handleHlsWithDateRangeMetadata();
                break;
            case R.string.dashWithEmsgMetadataName:
                handleDashWithEmsgMetadata();
                break;
            case R.string.dashWithEventStreamMetadataName:
                handleDashWithEventStreamMetadata();
                break;
            default:
                SpannableString toastMessage = SpannableString.valueOf(this.getString(R.string.missingMetadataConfiguration));
                toastMessage.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, toastMessage.length(), 0);
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Demonstrates THEOplayer configuration that allows to handle ID3 metadata from HLS stream.
     */
    private void handleHlsWithID3Metadata() {
        viewBinding.headerTextView.setText(getString(R.string.hlsWithID3MetadataHeader));

        // Configuring THEOplayer with appropriate stream source.
        configureTHEOplayer(
                TypedSource.Builder.typedSource(getString(R.string.hlsWithID3MetadataSourceUrl))
        );

        // Listening to 'addtrack' events to find text track of type 'id3'.
        theoPlayer.getTextTracks().addEventListener(TextTrackListEventTypes.ADDTRACK, event -> {

            if (event.getTrack().getType() == TextTrackType.ID3) {
                Log.i(TAG, "Event: ADDTRACK, trackType=" + event.getTrack().getType());

                // Listening to 'exitcue' event. For ID3 metadata exited cue means "current cue".
                event.getTrack().addEventListener(TextTrackEventTypes.EXITCUE, cueEvent -> {
                    Log.i(TAG, "Event: EXITCUE, cue=" + cueEvent.getCue());

                    // Decoding ID3 metadata. In this example, the data received
                    // is in the form: '{"content":{"id":"TXXX","description":"","text":"..."}}}'.
                    JSONObject cueContent = cueEvent.getCue().getContent();
                    try {
                        appendMetadata(cueContent.getJSONObject("content").getString("text"));
                    } catch (JSONException exception) {
                        appendMetadata(cueContent.toString());
                    }
                });
            }

        });
    }

    /**
     * Demonstrates THEOplayer configuration that allows to handle EXT-X-PROGRAM-DATE-TIME metadata from HLS stream.
     */
    private void handleHlsWithProgramDateTimeMetadata() {
        viewBinding.headerTextView.setText(getString(R.string.hlsWithProgramDateTimeMetadataHeader));

        // Configuring THEOplayer with appropriate stream source.
        configureTHEOplayer(
                TypedSource.Builder.typedSource(getString(R.string.hlsWithProgramDateTimeMetadataSourceUrl))
        );

        // Listening to 'timeupdate' events that are triggered every time EXT-X-PROGRAM-DATE-TIME
        // is updated.
        theoPlayer.addEventListener(PlayerEventTypes.TIMEUPDATE, event -> {
            Log.i(TAG, "Event: TIMEUPDATE, currentTime=" + event.getCurrentTime());

            // Once we know that EXT-X-PROGRAM-DATE-TIME was updated we have to request for its value.
            theoPlayer.requestCurrentProgramDateTime(date -> {
                appendMetadata(date == null ? "" : date.toString());
            });

        });
    }

    /**
     * Demonstrates THEOplayer configuration that allows to handle EXT-X-DATERANGE metadata from HLS stream.
     */
    private void handleHlsWithDateRangeMetadata() {
        viewBinding.headerTextView.setText(getString(R.string.hlsWithDateRangeMetadataHeader));

        // Configuring THEOplayer with appropriate stream source. Note that logic that exposes date
        // ranges parsed from HLS manifest needs to be enabled.
        configureTHEOplayer(
                TypedSource.Builder
                        .typedSource(getString(R.string.hlsWithDateRangeMetadataSourceUrl))
                        .hlsDateRange(true)
        );

        // Listening to 'addtrack' events to find text track of type 'daterange'.
        theoPlayer.getTextTracks().addEventListener(TextTrackListEventTypes.ADDTRACK, event -> {

            if (event.getTrack().getType() == TextTrackType.DATERANGE) {
                Log.i(TAG, "Event: ADDTRACK, trackType=" + event.getTrack().getType());

                // Listening to 'addcue' event to get parsed date range.
                event.getTrack().addEventListener(TextTrackEventTypes.ADDCUE, cueEvent -> {
                    DateRangeCue cue = (DateRangeCue) cueEvent.getCue();

                    Log.i(TAG, "Event: ADDCUE, cue=" + cue);

                    // Decoding date range metadata. For demo purposes we are displaying
                    // content as it is encoding byte arrays with base64.
                    appendMetadata("StartDate: " + cue.getStartDate() +
                            "\nEndDate: " + cue.getEndDate() +
                            "\nDuration: " + cue.getDuration() +
                            "\nScte35Cmd: " + (cue.getScte35Cmd() != null ? Base64.encodeToString(cue.getScte35Cmd(), NO_WRAP) : "N/A") +
                            "\nScte35In: " + (cue.getScte35In() != null ? Base64.encodeToString(cue.getScte35In(), NO_WRAP) : "N/A") +
                            "\nScte35Out: " + (cue.getScte35Out() != null ? Base64.encodeToString(cue.getScte35Out(), NO_WRAP) : "N/A"));
                });
            }

        });
    }

    /**
     * Demonstrates THEOplayer configuration that allows to handle EMSG metadata from DASH stream.
     */
    private void handleDashWithEmsgMetadata() {
        viewBinding.headerTextView.setText(getString(R.string.dashWithEmsgMetadataHeader));

        // Configuring THEOplayer with appropriate stream source.
        configureTHEOplayer(
                TypedSource.Builder.typedSource(getString(R.string.dashWithEmsgMetadataSourceUrl))
        );

        // Listening to 'addtrack' events to find text track of type 'emsg'.
        theoPlayer.getTextTracks().addEventListener(TextTrackListEventTypes.ADDTRACK, event -> {

            if (event.getTrack().getType() == TextTrackType.EMSG) {
                Log.i(TAG, "Event: ADDTRACK, trackType=" + event.getTrack().getType());

                // Listening to 'addcue' event to read EMSG metadata.
                event.getTrack().addEventListener(TextTrackEventTypes.ADDCUE, cueEvent -> {
                    Log.i(TAG, "Event: ADDCUE, cue=" + cueEvent.getCue());

                    // Decoding EMSG metadata. In this example, the data received
                    // is in the form: '{"content":{"0":73,"1":68,"2":51,"3":4,"4":0,"5":32,...}'.
                    JSONObject cueContent = cueEvent.getCue().getContent();
                    try {
                        ByteArrayOutputStream byteContent = new ByteArrayOutputStream();
                        JSONObject jsonContent = cueContent.getJSONObject("content");
                        Iterator<String> jsonContentKeys = jsonContent.keys();
                        while (jsonContentKeys.hasNext()) {
                            byteContent.write(jsonContent.getInt(jsonContentKeys.next()));
                        }
                        appendMetadata(new String(byteContent.toByteArray()));
                    } catch (JSONException e) {
                        appendMetadata(cueContent.toString());
                    }
                });
            }

        });
    }

    /**
     * Demonstrates THEOplayer configuration that allows to handle EventStream metadata from DASH stream.
     */
    private void handleDashWithEventStreamMetadata() {
        viewBinding.headerTextView.setText(getString(R.string.dashWithEventStreamMetadataHeader));

        // Configuring THEOplayer with appropriate stream source.
        configureTHEOplayer(
                TypedSource.Builder.typedSource(getString(R.string.dashWithEventStreamMetadataSourceUrl))
        );

        // Listening to 'addtrack' events to find text track of type 'eventstream'.
        theoPlayer.getTextTracks().addEventListener(TextTrackListEventTypes.ADDTRACK, event -> {

            if (event.getTrack().getType() == TextTrackType.EVENTSTREAM) {
                Log.i(TAG, "Event: ADDTRACK, trackType=" + event.getTrack().getType());

                // Listening to 'addcue' event to read EventStream metadata.
                event.getTrack().addEventListener(TextTrackEventTypes.ADDCUE, cueEvent -> {
                    Log.i(TAG, "Event: ADDCUE, cue=" + cueEvent.getCue());

                    // For demo purposes we are displaying whole content as it is.
                    appendMetadata(cueEvent.getCue().getContent().toString());
                });
            }
        });
    }

    private void configureTHEOplayer(TypedSource.Builder typedSource) {
        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        SourceDescription.Builder sourceDescription = SourceDescription.Builder
                .sourceDescription(typedSource.build());

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.setSource(sourceDescription.build());
        theoPlayer.setAutoplay(true);

        // Adding listeners to THEOplayer basic playback events.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY, event -> Log.i(TAG, "Event: PLAY"));
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING, event -> Log.i(TAG, "Event: PLAYING"));
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE, event -> Log.i(TAG, "Event: PAUSE"));
        theoPlayer.addEventListener(PlayerEventTypes.ENDED, event -> Log.i(TAG, "Event: ENDED"));
        theoPlayer.addEventListener(PlayerEventTypes.ERROR, event -> Log.i(TAG, "Event: ERROR, error=" + event.getError()));
    }

    private void appendMetadata(String metadata) {
        if (viewBinding.metadataTextView.length() == 0) {
            viewBinding.metadataTextView.setText(metadata);
        } else {
            viewBinding.metadataTextView.append("\n\n" + metadata);
        }

        // If metadata content was scrolled to bottom then scroll down automatically after adding new content.
        int fullScrollHeight = viewBinding.metadataScrollView.getScrollY() + viewBinding.metadataScrollView.getHeight();
        int metadataHeight = viewBinding.metadataTextView.getHeight();
        if (metadataHeight <= fullScrollHeight) {
            viewBinding.metadataScrollView.post(() -> viewBinding.metadataScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }


    // In order to work properly and in sync with the activity lifecycle changes (e.g. device
    // is rotated, new activity is started or app is moved to background) we need to call
    // the "onResume", "onPause" and "onDestroy" methods of the THEOplayerView when the matching
    // activity methods are called.

    @Override
    protected void onPause() {
        super.onPause();
        viewBinding.theoPlayerView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewBinding.theoPlayerView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewBinding.theoPlayerView.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(viewBinding.mainConstraintLayout);

        int headerTextViewId = viewBinding.headerTextView.getId();
        int theoPlayerViewId = viewBinding.theoPlayerView.getId();
        int metadataLabelTextViewId = viewBinding.metadataLabelTextView.getId();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape orientation metadataLabelTextView (with attached metadataTextView)
            // is placed on the right of headerTextView (with attached theoPlayerView).
            // Additionally metadataLabelTextView width is shrunk to 40% of screen width and its
            // vertical position is centered.
            constraintSet.constrainPercentWidth(headerTextViewId, 0.4F);
            constraintSet.setVerticalBias(headerTextViewId, 0.5F);
            constraintSet.connect(metadataLabelTextViewId, ConstraintSet.START, headerTextViewId, ConstraintSet.END);
            constraintSet.connect(metadataLabelTextViewId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        } else {
            // In portrait orientation metadataLabelTextView (with attached metadataTextView)
            // is placed under the theoPlayerView.
            // Additionally metadataLabelTextView width is expanded to full screen width and its
            // vertical alignment is set to top.
            constraintSet.constrainPercentWidth(headerTextViewId, 1F);
            constraintSet.setVerticalBias(headerTextViewId, 0F);
            constraintSet.connect(metadataLabelTextViewId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(metadataLabelTextViewId, ConstraintSet.TOP, theoPlayerViewId, ConstraintSet.BOTTOM);
        }

        constraintSet.applyTo(viewBinding.mainConstraintLayout);

        // After orientation change metadata content scroll position is adjusted so it could be
        // scrolled automatically when new content is appended.
        viewBinding.metadataScrollView.post(() -> viewBinding.metadataScrollView.fullScroll(View.FOCUS_DOWN));
    }
}
