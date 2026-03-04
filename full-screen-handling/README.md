# Full-Screen Handling

A sample demonstrating how to manage fullscreen mode with [OptiView Player] (formerly THEOplayer),
including orientation-coupled fullscreen and a custom fullscreen activity.

The app enters fullscreen when the device is rotated to landscape and supports a custom
fullscreen activity with play/pause and exit controls. It uses Jetpack Compose with `DefaultUI`
for the player UI.

[`PlayerActivity`](src/main/java/com/theoplayer/sample/ui/fullscreen/PlayerActivity.kt) uses Compose with
`DefaultUI` and enables orientation-coupled fullscreen via `fullScreenManager`. A
[`CustomFullScreenActivity`](src/main/java/com/theoplayer/sample/ui/fullscreen/CustomFullScreenActivity.kt)
extends `FullScreenActivity` to provide custom play/pause and exit controls in fullscreen mode.

## Quick Start

1. Open this repository in Android Studio.
2. Select the `full-screen-handling` run configuration.
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
