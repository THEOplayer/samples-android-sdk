# Reference Apps - THEO Basic Playback

The purpose of this app is to demonstrate how to integrate [THEOplayer] into an Android app and setup
playback of a sample stream.

For quick start, please proceed with the [Quick Start](#quick-start) guide.


## THEO Docs

The [THEOplayer documentation] will provide an end-to-end explanation of how to setup an Android application with
THEOplayer included. It will cover setting up an IDE, including all dependencies, adding THEOplayer
to the activity and running the application.


## Quick Start

### Using THEOplayer distribution from public repository

This project is already set up to use THEOplayer from jitpack.io so you need to check it out and build.
Below is a short explanation about how to include THEOplayer in the project.
For the more comprehensive explanation please refer to [THEOplayer documentation].

To use THEOplayer from jitpack.io, you need to include it in the build.gradle file on the project level:

    dependencyResolutionManagement {
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
         implementation 'com.theoplayer.theoplayer-sdk-android:unified:4.12.0'
        ...
    }

## Streams/Content Rights:

The DRM streams used in this app (if any) are provided by our Partner: [EZ DRM] and hold all
the rights for the content. These streams are DRM protected and cannot be used for any other purposes.


## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE] file for details.

[//]: # (Links and Guides reference)
[THEOplayer]: https://www.theoplayer.com/
[THEO Portal]: https://portal.theoplayer.com/
[THEOplayer documentation]: https://docs.theoplayer.com/getting-started/01-sdks/02-android/00-getting-started.md#getting-started-on-android
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing
[EZ DRM]: https://ezdrm.com/

[//]: # (Project files reference)
[LICENSE]: LICENSE
[app-level build.gradle]: app/build.gradle