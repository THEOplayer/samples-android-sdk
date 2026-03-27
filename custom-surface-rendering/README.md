# Custom Surface Rendering

A sample demonstrating how to render [OptiView Player] (formerly THEOplayer) on custom surfaces,
including SurfaceView, TextureView, and SurfaceControl (API 29+).

[`PlayerActivity`](src/main/java/com/theoplayer/sample/surface/PlayerActivity.kt) uses XML DataBinding
with buttons to switch between `SurfaceView`, `TextureView`, and `SurfaceControl` rendering targets.
`CustomSurfaceView` and `CustomTextureView` implement proper surface callbacks, while `AspectRatioHelper`
handles aspect ratio calculations for FIT and ASPECT_FILL modes.

## Quick Start

1. Open this repository in Android Studio.
2. Select the `custom-surface-rendering` run configuration.
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
[THEOportal]: https://portal.theoplayer.com/
