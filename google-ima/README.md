# Google IMA

A sample demonstrating how to integrate [Google IMA] (Interactive Media Ads) with [OptiView Player]
(formerly THEOplayer) to insert client-sided ads into video playback.

[`PlayerActivity`](src/main/java/com/theoplayer/sample/ads/googleima/PlayerActivity.kt) sets up Compose
with `DefaultUI` and configures a source that includes a Google IMA ad description. The THEOplayer IMA
integration handles ad scheduling, playback, and UI overlays automatically.

## Quick Start

1. Open this repository in Android Studio.
2. Select the `google-ima` run configuration.
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
    implementation(libs.theoplayer.connector.ima)
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
[Google IMA]: https://developers.google.com/interactive-media-ads/
[THEOportal]: https://portal.theoplayer.com/