# Reference Apps - THEO Google IMA

The purpose of this app is to demonstrate how to use [Google IMA] ads module in [THEOplayer]
to insert advertisement to video playback.

For quick start, please proceed with the [Quick Start](#quick-start) guide.


## THEO Docs

The [THEOplayer documentation] will provide a detailed explanation about Advertising and Ads insertion by using
Google IMA module.

  * [THEOplayer How To's - Ads Insertion with Google IMA]

This app is an extension of [THEO Basic Playback] application. For help with getting started with
THEOplayer or Android Studio feel free to check related [THEOplayer documentation] 


## Quick Start

### Using library downloaded from [THEO Portal]

To use THEOplayer from a library downloaded from THEO Portal, follow these steps:

1. Obtain THEOplayer Android SDK with **Ads** feature enabled and unzip it.

   Please visit [Get Started with THEOplayer] to get required THEOplayer Android SDK.

2. Copy **`theoplayer-android-[name]-[version]-minapi21-release.aar`** file from unzipped SDK into
   application **[libs]** folder and rename it to **`theoplayer.aar`**.

   Project is configured to load SDK with such name, for using other name please change
   `implementation ':theoplayer@aar'` dependency in [app-level build.gradle] file accordingly.
   
3. Confirm that `app/build.gradle` has the Google IMA Android SDK as one of its dependencies.

4. Open _**THEO Google IMA**_ application in Android Studio.

   Android Studio should automatically synchronize and rebuild project. If this won't happen please
   select **File > Sync Project with Gradle Files** menu item to do it manually. Please note, that
   in very rare cases it will be required to synchronize project twice.

5. Select **Run > Run 'app'** menu item to run application on a device selected by default.


## Streams/Content Rights:

The DRM streams used in this app (if any) are provided by our Partner: [EZ DRM] and hold all
the rights for the content. These streams are DRM protected and cannot be used for any other purposes.


## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE] file for details.


[//]: # (Links and Guides reference)
[THEOplayer]: https://www.theoplayer.com/
[THEO Basic Playback]: ../Basic-Playback
[THEO Portal]: https://portal.theoplayer.com/
[THEOplayer documentation]: https://docs.theoplayer.com/getting-started/01-sdks/02-android/00-getting-started.md#getting-started-on-android
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing
[EZ DRM]: https://ezdrm.com/
[Google IMA]: https://developers.google.com/interactive-media-ads/

[//]: # (Project files reference)
[LICENSE]: LICENSE
[libs]: app/libs
[app-level build.gradle]: build.gradle
