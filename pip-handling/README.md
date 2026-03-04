# PiP Handling

A sample demonstrating how to use Picture-in-Picture (PiP) mode with [OptiView Player]
(formerly THEOplayer), allowing video playback to continue in a floating window.

The app uses native Android PiP API to auto enter PiP when navigating away (API 31+),
provides a manual PiP trigger button, and supports background playback while in PiP.
It uses Jetpack Compose with `DefaultUI` for the player UI.

[`PlayerActivity`](src/main/java/com/theoplayer/sample/ui/pip/PlayerActivity.kt) uses Compose with
`DefaultUI` and configures PiP via `setPictureInPictureParams`.

Please note that this sample demonstrates a native PiP implementation. While the player
also offers a [PiP API], a native PiP implementation might be a better choice to have the
freedom to implement the PiP type needed due to the vast differences in how PiP works on
different Android versions and differing limitations on different API levels.

Please refer to [Android's guide on how to implement PiP](https://developer.android.com/develop/ui/views/picture-in-picture) for more information.

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
