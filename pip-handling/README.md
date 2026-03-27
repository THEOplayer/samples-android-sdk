# PiP Handling

A sample demonstrating how to use Picture-in-Picture (PiP) mode with [OptiView Player]
(formerly THEOplayer), allowing video playback to continue in a floating window.

The app combines the player's [PiP API] with native Android PiP APIs and the AndroidX
`VideoPlaybackPictureInPicture` helper for a fully native PiP experience:

- Auto-enter PiP when navigating away (API 31+),
- Manual PiP trigger via a toolbar button,
- Play/pause `RemoteAction`s shown as overlay buttons in the PiP window,
- Dynamic aspect ratio that stays in sync with the video dimensions,
- Background playback so that the audio continues while in PiP

Please refer to [Android's guide on PiP](https://developer.android.com/develop/ui/views/picture-in-picture)
for more information on native PiP capabilities.

## Quick Start

1. Open this repository in Android Studio.
2. Select the `pip-handling` run configuration.
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
[THEOportal]: https://portal.theoplayer.com/
[PiP API]: https://optiview.dolby.com/docs/theoplayer/v10/api-reference/android/com/theoplayer/android/api/pip/package-summary.html
