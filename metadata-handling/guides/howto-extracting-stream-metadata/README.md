# THEOplayer How To's - Extracting Stream Metadata

This guide is going to show how to extract metadata of various types that can be attached to
played stream.

To obtain THEOplayer Android SDK please visit [Get Started with THEOplayer].

Presented code snippets are taken from [THEO Metadata Handling] reference app. Please note that in
this app all URLs are defined as an Android resource, but they can be inlined as well. Please check
[values.xml] file for URLs definition.


## Table of Contents

  * [Extracting metadata]
    * [HLS with ID3 metadata]
    * [HLS with PROGRAM-DATE-TIME]
    * [HLS with DATERANGE]
    * [DASH with EMSG]
    * [DASH with EventStream]
  * [Summary]


## Extracting metadata

Different stream types can contain different types of metadata. Below we are going to show the most
popular metadata types. Please note that all examples have similar structure.

All of them are using THEOplayer instance taken from `THEOplayerView` defined in [activity_player.xml]:

```java
public class PlayerActivity extends AppCompatActivity {

    private Player theoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ...

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.getPlayer();

        // ...
    }
}
```

All of them are using `configureTHEOplayer(...)` method to configure THEOplayer with appropriate
`TypedSource`:

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void configureTHEOplayer(TypedSource.Builder typedSource) {
        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        SourceDescription.Builder sourceDescription = SourceDescription.Builder
                .sourceDescription(typedSource.build());

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.setSource(sourceDescription.build());
        theoPlayer.setAutoplay(true);

        // Adding listeners to THEOplayer basic playback events.
        // ...
    }
}
```

And finally, all of them are using `appendMetadata(...)` method to display extracted metadata on the
screen:

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void appendMetadata(String metadata) {
        if (viewBinding.metadataTextView.length() == 0) {
            viewBinding.metadataTextView.setText(metadata);
        } else {
            viewBinding.metadataTextView.append("\n\n" + metadata);
        }

        // ...
    }
}
```

Please visit [activity_player.xml] to check layout definition and [PlayerActivity.java] for full
implementation.

### HLS with ID3 metadata

`ID3` is a metadata container used in conjunction with the HLS streams. It allows information such as
the title, artist, album, track number, and other information about the stream to be stored in
the stream itself.

To find `ID3` metadata in HLS stream, listen to `TextTrackListEventTypes.ADDTRACK` event
on THEO's `TextTrackList` object, looking for text track of type `TextTrackType.ID3`. Once required
text track is found, listen to `TextTrackEventTypes.EXITCUE` event on that track to get `ID3`
data from incoming cues:

```java
public class PlayerActivity extends AppCompatActivity {
    // ...

    /**
     * Demonstrates THEOplayer configuration that allows to handle ID3 metadata from HLS stream.
     */
    private void handleHlsWithID3Metadata() {
        // ...

        // Configuring THEOplayer with appropriate stream source.
        configureTHEOplayer(
                TypedSource.Builder.typedSource(getString(R.string.hlsWithID3MetadataSourceUrl))
        );

        // Listening to 'addtrack' events to find text track of type 'id3'.
        theoPlayer.getTextTracks().addEventListener(TextTrackListEventTypes.ADDTRACK, event -> {

            if (event.getTrack().getType() == TextTrackType.ID3) {
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

    // ...
}
```

### HLS with PROGRAM-DATE-TIME

THEOplayer has support for associating media segments with an absolute date and time. This can be
useful for synchronising video playback with displaying other relevant information about the video
stream.

THEOplayer enables this feature by making use of the `EXT-X-PROGRAM-DATE-TIME` tag information that
gets embedded in the HLS manifest file.

Once the `PROGRAM-DATE-TIME` information is set in the HLS manifest, current program date can be
requested by calling `requestCurrentProgramDateTime()` method on THEOplayer instance. This value
gets updated with each `PlayerEventTypes.TIMEUPDATE` event thrown by THEOplayer:

```java
public class PlayerActivity extends AppCompatActivity {
    // ...

    /**
     * Demonstrates THEOplayer configuration that allows to handle EXT-X-PROGRAM-DATE-TIME metadata from HLS stream.
     */
    private void handleHlsWithProgramDateTimeMetadata() {
        // ...

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

    // ...
}
```

### HLS with DATERANGE

HLS supports manifests which contain the `EXT-X-DATERANGE` tag. This is used to define date range
metadata in a media playlist. A possible use case is defining timed metadata for interstitial regions
such as advertisements, but can be used to define any timed metadata needed by the stream.

To extract `DATERANGE` metadata in HLS stream, enable that feature in THEOplayer. It can be done on
`THEOplayerView` definition by adding `app:hlsDateRange="true"` argument (see [activity_player.xml])
or directly in played `TypedSource` definition by calling `hlsDateRange(true)`:

```java
public class PlayerActivity extends AppCompatActivity {
    // ...

    /**
     * Demonstrates THEOplayer configuration that allows to handle EXT-X-DATERANGE metadata from HLS stream.
     */
    private void handleHlsWithDateRangeMetadata() {
        // ...

        // Configuring THEOplayer with appropriate stream source. Note that logic that exposes date
        // ranges parsed from HLS manifest needs to be enabled.
        configureTHEOplayer(
                TypedSource.Builder
                        .typedSource(getString(R.string.hlsWithDateRangeMetadataSourceUrl))
                        .hlsDateRange(true)
        );

        //...
    }
}
```

Having that, listen to `TextTrackListEventTypes.ADDTRACK` event on THEO's `TextTrackList` object,
looking for text track of type `TextTrackType.DATERANGE`. Once required text track is found,
listen to `TextTrackEventTypes.ADDCUE` event on that track to get `DATARANGE` data from incoming cues:

```java
public class PlayerActivity extends AppCompatActivity {
    // ...

    /**
     * Demonstrates THEOplayer configuration that allows to handle EXT-X-DATERANGE metadata from HLS stream.
     */
    private void handleHlsWithDateRangeMetadata() {
        // ...

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

    // ...
}
```

### DASH with EMSG

To extract `EMSG` metadata from DASH stream, listen to `TextTrackListEventTypes.ADDTRACK`
event on THEO's `TextTrackList` object, looking for text track of type `TextTrackType.EMSG`. Once
required text track is found, listen to `TextTrackEventTypes.ADDCUE` event on that track
to get data from incoming cues:

```java
public class PlayerActivity extends AppCompatActivity {
    // ...

    /**
     * Demonstrates THEOplayer configuration that allows to handle EMSG metadata from DASH stream.
     */
    private void handleDashWithEmsgMetadata() {
        // ...

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

    // ...
}
```

### DASH with EventStream

To extract EventStream metadata from DASH stream, listen to `TextTrackListEventTypes.ADDTRACK`
event on THEO's `TextTrackList` object, looking for text track of type `TextTrackType.EVENTSTREAM`.
Once required text track is found, listen to `TextTrackEventTypes.ADDCUE` event on that track
to get data from incoming cues:

```java
public class PlayerActivity extends AppCompatActivity {
    // ...

    /**
     * Demonstrates THEOplayer configuration that allows to handle EventStream metadata from DASH stream.
     */
    private void handleDashWithEventStreamMetadata() {
        // ...

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

    // ...
}
```


## Summary

This guide covered ways of extracting stream metadata of various types.

For more guides about THEOplayer SDK API usage and tips&tricks please visit [THEO Docs] portal.


[//]: # (Sections reference)
[Extracting metadata]: #extracting-metadata
[HLS with ID3 metadata]: #hls-with-id3-metadata
[HLS with PROGRAM-DATE-TIME]: #hls-with-program-date-time
[HLS with DATERANGE]: #hls-with-daterange
[DASH with EMSG]: #dash-with-emsg
[DASH with EventStream]: #dash-with-eventstream
[Summary]: #summary

[//]: # (Links and Guides reference)
[THEO Metadata Handling]: ../..
[THEO Docs]: https://docs.portal.theoplayer.com/
[THEOplayer How To's - THEOplayer Android SDK Integration]: ../../../Basic-Playback/guides/howto-theoplayer-android-sdk-integration/README.md
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing

[//]: # (Project files reference)
[PlayerActivity.java]: ../../app/src/main/java/com/theoplayer/sample/playback/metadata/PlayerActivity.java
[activity_player.xml]: ../../app/src/main/res/layout/activity_player.xml
[values.xml]: ../../app/src/main/res/values/values.xml
