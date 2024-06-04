# Reference Apps - THEO Google Cast

The purpose of this app is to demonstrate how to enable and configure [Google Cast] functionality
in [THEOplayer] and the ability to cast to a neighbouring Cast device.

For quick start, please proceed with the [Quick Start](#quick-start) guide.

## Guides

The guides below will provide a detailed explanation how to configure Google Cast in THEOplayer.

  * [THEOplayer How To's - Google Cast Integration]

This app is an extension of [THEO Basic Playback] application. For help with getting started with
THEOplayer or Android Studio feel free to check related guides:

  * [THEO Knowledge Base - Android Studio Setup]
  * [THEO Knowledge Base - Virtual and Physical Devices]
  * [THEOplayer How To's - THEOplayer Android SDK Integration]


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
         implementation "com.theoplayer.theoplayer-sdk-android:integration-cast:6.1.0"
         ...
     }

## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE] file for details.

[//]: # (Links and Guides reference)
[THEOplayer]: https://www.theoplayer.com/
[THEO Basic Playback]: ../Basic-Playback
[THEO Knowledge Base - Android Studio Setup]: ../Basic-Playback/guides/knowledgebase-android-studio-setup/README.md
[THEO Knowledge Base - Virtual and Physical Devices]: ../Basic-Playback/guides/knowledgebase-virtual-and-physical-devices/README.md
[THEO Knowledge Base - DRM Systems]: https://docs.portal.theoplayer.com/docs/docs/advanced-topics/content-protection/content-protection-1-digital-rights-management-drm-systems
[THEOplayer How To's - THEOplayer Android SDK Integration]: ../Basic-Playback/guides/howto-theoplayer-android-sdk-integration/README.md
[THEOplayer How To's - Google Cast Integration]: guides/howto-google-cast-integration/README.md
[THEOplayer documentation]: https://docs.theoplayer.com/getting-started/01-sdks/02-android/00-getting-started.md#getting-started-on-android
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing
[EZ DRM]: https://ezdrm.com/
[Google Cast]: http://www.google.com/cast/

[//]: # (Project files reference)
[LICENSE]: LICENSE
[libs]: app/libs
[app-level build.gradle]: build.gradle
