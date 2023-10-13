# Reference Apps - THEO Custom UI

The purpose of this app is to demonstrate how [THEOplayer] can be setup and configured to be
controlled by a custom native UI.

For quick start, please proceed with the [Quick Start](#quick-start) guide.

## THEO Docs

THEOplayer's Android SDK offers a dedicated, [open-source UI package](https://github.com/THEOplayer/android-ui) 
developed using JetPack Compose. This UI package is highly flexible and customizable, 
making it the recommended choice for initiating your UI development on THEOplayer for Android.

However, it is also feasible to develop a customized user interface.
The guides below will provide a detailed explanation how to use create custom THEOplayer UI that
allows to control played stream:

  * [THEOplayer How To's - Defining Custom Player Controls]

This app is an extension of [THEO Basic Playback] application. For help with getting started with
THEOplayer or Android Studio feel free to check [THEOplayer documentation]

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
           implementation 'com.theoplayer.theoplayer-sdk-android:basic-minapi21:4.12.0'
          ...
      }

## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE] file for details.

[//]: # (Links and Guides reference)
[THEOplayer]: https://www.theoplayer.com/
[THEO Basic Playback]: ../Basic-Playback
[THEOplayer How To's - Defining Custom Player Controls]: guides/howto-defining-custom-player-controls/README.md
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing
[THEOplayer documentation]: https://docs.theoplayer.com/getting-started/01-sdks/02-android/00-getting-started.md#getting-started-on-android
[EZ DRM]: https://ezdrm.com/

[//]: # (Project files reference)
[LICENSE]: LICENSE
[libs]: app/libs
[app-level build.gradle]: app/build.gradle
