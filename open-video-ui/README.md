# Open Video UI

A theme showcase for [Open Video UI] for Android with [OptiView Player] (formerly THEOplayer).

The app demonstrates how to customize the player UI using Jetpack Compose; from simple color tweaks
to fully custom layouts built with `UIController`.

You can find more information about Open Video UI [here](https://optiview.dolby.com/docs/open-video-ui/android/).

## Themes

| Theme | Description |
|-------|-------------|
| **Default** | The stock `DefaultUI` with no customization. Shows what you get out of the box with `THEOplayerTheme`. |
| **Custom Colors** | Wraps `DefaultUI` in a custom `MaterialTheme` to change the accent color. |
| **Nitflex** | A fully custom Netflix-inspired skin built with `UIController`.|
| **Minimal** | A barebones player using `UIController` with only a play/pause button and a seek bar. Useful as a starting point for embedding a player with minimal controls. |
| **Portrait** | A fullscreen vertical player with TikTok-style side action buttons (like, comment, share).|
| **Festive** | A holiday-themed player with red/green Christmas colors.|
| **Modern** | A YouTube-inspired layout with pill-shaped grouped controls.|

## Quick Start

1. Open this repository in Android Studio.
2. Select the `open-video-ui` run configuration.
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