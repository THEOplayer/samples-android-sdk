# DRM Playback

A sample demonstrating how to play DRM-protected streams with [OptiView Player] (formerly THEOplayer)
using a custom Widevine content protection integration.

The app implements a custom `ContentProtectionIntegration` for Axinom Widevine DRM, intercepting
license requests to add authentication tokens. It uses Jetpack Compose with `DefaultUI` for the player UI.

[`PlayerActivity`](src/main/java/com/theoplayer/sample/drm_playback/PlayerActivity.kt) sets up Compose
with `DefaultUI` and registers a custom `ContentProtectionIntegration` for Axinom Widevine DRM.
The integration factory (`AxinomWidevineContentProtectionIntegrationFactory`) and integration class
intercept license requests to inject the required authorization token. You can find more information
about Open Video UI [here](https://optiview.dolby.com/docs/open-video-ui/android/).

## Quick Start

1. Open this repository in Android Studio.
2. Select the `drm-playback` run configuration.
3. Build and run on a real device.

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

You can find more information about DRM connectors on its own repo [here](https://github.com/THEOplayer/samples-drm-integration/tree/master).

## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE](../LICENSE) file for details.

[OptiView Player]: https://optiview.dolby.com/
[Open Video UI]: https://optiview.dolby.com/docs/open-video-ui/android/
[THEOportal]: https://portal.theoplayer.com/
