# Offline Playback

A sample demonstrating how to download and play content offline with [OptiView Player]
(formerly THEOplayer) using the caching API.

The app manages caching tasks (create, pause, resume, remove), tracks download progress with
size overlays, and supports DRM license renewal for protected content. It uses Jetpack Compose
with `DefaultUI` for the player UI.

[`OfflineActivity`](src/main/java/com/theoplayer/sample/playback/offline/OfflineActivity.kt) displays downloadable sources and some extra
information about them. [`PlayerActivity`](src/main/java/com/theoplayer/sample/playback/offline/PlayerActivity.kt) uses Compose with `DefaultUI` for playback. `OfflineDrmLicenseRenewalWorker` handles background DRM license renewal via WorkManager.

## Quick Start

1. Open this repository in Android Studio.
2. Select the `offline-playback` run configuration.
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
    implementation(libs.work.runtime)
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
