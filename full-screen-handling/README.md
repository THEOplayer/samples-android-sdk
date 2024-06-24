# Reference Apps - THEO Full Screen Handling

The purpose of this app is to demonstrate how to manage [THEOplayer] full screen mode.

For quick start, please proceed with the [Quick Start](#quick-start) guide.


## THEO Docs

The guides below will provide a detailed explanation how to deal with full screen mode.

  * [THEOplayer How To's - Full Screen Management]

This app is an extension of [THEO Basic Playback] application. For help with getting started with
THEOplayer or Android Studio feel free to check related [THEOplayer documentation]

## Quick Start

### Using THEOplayer distribution from public repository

This project is already set up to use THEOplayer from jitpack.io so you need to check it out and build.
Below is a short explanation about how to include THEOplayer in the project.
For the more comprehensive explanation please refer to [THEOplayer documentation].

To use THEOplayer from jitpack.io, you need to include it in the build.gradle file on the project level:

     allprojects {
         repositories {
             ...
             maven { url 'https://jitpack.io' }
             ...
         }
     }

Then, to use the library, specify the following in the module's level build.gradle file:

     dependencies {
         ...
         // THEOplayer required dependencies.
          implementation 'com.theoplayer.theoplayer-sdk-android:core:6.1.0'
         ...
     }

## Streams/Content Rights:

The DRM streams used in this app (if any) are provided by our Partner: [EZ DRM] and hold all
the rights for the content. These streams are DRM protected and cannot be used for any other purposes.

## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE] file for details.

[//]: # (Links and Guides reference)
[THEOplayer]: https://www.theoplayer.com/
[THEO Basic Playback]: ../Basic-Playback
[THEOplayer How To's - Full Screen Management]: guides/howto-full-screen-management/README.md
[THEO Portal]: https://portal.theoplayer.com/
[THEOplayer documentation]: https://docs.theoplayer.com/getting-started/01-sdks/02-android/00-getting-started.md#getting-started-on-android
[EZ DRM]: https://ezdrm.com/

[//]: # (Project files reference)
[LICENSE]: LICENSE
[libs]: app/libs
[app-level build.gradle]: build.gradle.kts
