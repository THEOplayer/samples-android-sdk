# Background Playback

A sample demonstrating how to continue audio/video playback in the background with [OptiView Player]
(formerly THEOplayer) using Android's MediaSession API and a foreground service.

The app uses the [Media Session Connector] to report the playback state and receive player requests
and show media notification controls, handle audio focus changes, and pause playback
when audio output changes (e.g. headphone removal). It uses Jetpack Compose with `DefaultUI`
for the player UI.

[`PlayerActivity`](src/main/java/com/theoplayer/sample/background/PlayerActivity.kt) uses Compose with
`DefaultUI` and binds to a `MediaPlaybackService` foreground service that keeps playback alive in the
background. `AudioFocusManager` handles audio focus transitions, and `MediaNotificationBuilder` creates
the media-style notification with playback controls.

More information on how to use the [Media Session Connector] can be found on its documentation page and
the Android developer pages describing [Building a video app] and [Building an audio app].

## Quick Start

1. Open this repository in Android Studio.
2. Select the `background-playback` run configuration.
3. Build and run on a device or emulator.

### THEOplayer dependency

This project uses THEOplayer from the [official Maven repository](https://maven.theoplayer.com/#/releases).

The repository is declared in the project-level `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://maven.theoplayer.com/releases") }
    }
}
```

Dependencies are managed through a [version catalog](../gradle/libs.versions.toml) and declared
in the module-level [`build.gradle.kts`](build.gradle.kts):

```kotlin
dependencies {
    implementation(libs.theoplayer)
    implementation(libs.theoplayer.ui)
    implementation(libs.theoplayer.connector.mediasession)
    implementation(libs.media)
}
```

### License key

To play your own streams, add a THEOplayer license from the [THEOportal] in
[`common/src/main/res/values/values.xml`](../common/src/main/res/values/values.xml):

```xml
<string name="theoplayer_license">YOUR_LICENSE_HERE</string>
```

The license is picked up automatically via the `<meta-data>` tag in
[`AndroidManifest.xml`](src/main/AndroidManifest.xml).

### Streams

Video sources are defined in [`SourceManager`](../common/src/main/java/com/theoplayer/sample/common/SourceManager.kt).

## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE](../LICENSE) file for details.

[OptiView Player]: https://optiview.dolby.com/
[Open Video UI]: https://optiview.dolby.com/docs/open-video-ui/android/
[Media Session Connector]: https://github.com/THEOplayer/android-connector/tree/master/connectors/mediasession
[Android Developer Guides - MediaSession]: https://developer.android.com/media/legacy/mediasession
[Building a video app]: https://developer.android.com/media/legacy/video
[Building an audio app]: https://developer.android.com/media/legacy/audio
[THEOportal]: https://portal.theoplayer.com/