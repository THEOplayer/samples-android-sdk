# THEOplayer How To's - DRM Stream Configuration

This guide covers steps needed to configure the THEOplayer with stream protected by Widevine DRM
solution. For purposes of this guide it will be MPEG-DASH stream.

To obtain THEOplayer Android SDK please visit [Get Started with THEOplayer].

Presented code snippets are taken from [THEO DRM Playback] reference app. Please note that in this
app all URLs are defined as an Android resource, but they can be inlined as well. Please check
[values.xml] file for URLs definition.


## Table of Contents

  * [Configuring Sample DRM Stream]
  * [Listening to DRM Related Events]
  * [Summary]


## Configuring Sample DRM Stream

DRM configurations are added to `TypedSource` object. For purposes of this guide it is assumed
that `TypedSource` is already defined and used to configure THEOplayer source. Check
[PlayerActivity.java] source to see how the `TypedSource` object is defined. More information
about that can be found in [THEOplayer How To's - THEOplayer Android SDK Integration] guide.

Firstly, create a **`KeySystemConfiguration.Builder`** that allows to define license acquisition URL.
In our guide we are configuring URL to Widevine license server. Please open the main activity class
[PlayerActivity.java] and add following lines:

```java
public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ...

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer();
    }

    private void configureTHEOplayer() {
        //...

        // Creating a KeySystemConfiguration builder that contains license acquisition URL used
        // during the licensing process with a DRM server.
        KeySystemConfiguration.Builder keySystemConfig = KeySystemConfiguration.Builder
                .keySystemConfiguration(getString(R.string.defaultLicenseUrl));

        // ...
    }
}
```

After that, build `KeySystemConfiguration` object and create a **`DRMConfiguration.Builder`**
that allows to define Widevine license acquisition parameters:

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void configureTHEOplayer() {
        // ...

        // Creating a DRMConfiguration builder that contains license acquisition parameters
        // for integration with a Widevine license server.
        DRMConfiguration.Builder drmConfiguration = DRMConfiguration.Builder
                .widevineDrm(keySystemConfig.build());

        // ...
    }
}
```

As a last step, build **`DRMConfiguration`** object and update existing **`TypedSource`**
with it:

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void configureTHEOplayer() {
        // ...

        // Creating a TypedSource builder that defines the location of a single stream source
        // and has Widevine DRM parameters applied.
        TypedSource.Builder typedSource = TypedSource.Builder
                .typedSource(getString(R.string.defaultSourceUrl))
                .drm(drmConfiguration.build());

        // ...
    }
}
```


## Listening to DRM Related Events

In addition to that, specify few event listeners to get better view of actual content protection state:

```java
public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    // ...

    private void configureTHEOplayer() {
        // ...

        // Adding listeners to THEOplayer content protection events.
        theoPlayer.addEventListener(PlayerEventTypes.CONTENTPROTECTIONSUCCESS,
            event -> Log.i(TAG, "Event: CONTENT_PROTECTION_SUCCESS, mediaTrackType=" + event.getMediaTrackType()));
        theoPlayer.addEventListener(PlayerEventTypes.CONTENTPROTECTIONERROR,
            event -> Log.i(TAG, "Event: CONTENT_PROTECTION_ERROR, error=" + event.getError()));
    }
}
```


## Summary

This guide covered ways of configuring DRM protected stream playback in THEOplayer.

For more guides about THEOplayer SDK API usage and tips&tricks please visit [THEO Docs] portal.


[//]: # (Sections reference)
[Configuring Sample DRM Stream]: #configuring-sample-drm-stream
[Listening to DRM Related Events]: #listening-to-drm-related-events
[Summary]: #summary

[//]: # (Links and Guides reference)
[THEO DRM Playback]: ../..
[THEO Docs]: https://docs.portal.theoplayer.com/
[THEOplayer How To's - THEOplayer Android SDK Integration]: ../../../Basic-Playback/guides/howto-theoplayer-android-sdk-integration/README.md
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing

[//]: # (Project files reference)
[PlayerActivity.java]: ../../app/src/main/java/com/theoplayer/sample/playback/drm/PlayerActivity.java
[values.xml]: ../../app/src/main/res/values/values.xml
