# Reference Apps - THEO Basic Playback

The purpose of this app is to demonstrate how to integrate [THEOplayer] into an Android app and setup
playback of a sample stream.

For quick start, please proceed with the [Quick Start](#quick-start) guide.


## THEO Docs

This app is an extension of [THEO Basic Playback] application. For help with getting started with
THEOplayer or Android Studio feel free to check [THEOplayer documentation]


## Guides

The guides below will provide an end-to-end explanation of how to setup an Android application with
THEOplayer included. It will cover setting up an IDE, including all dependencies, adding THEOplayer
to the activity and running the application.

  * [THEO Knowledge Base - Android Studio Setup]
  * [THEO Knowledge Base - Simple Android Application]
  * [THEO Knowledge Base - Virtual and Physical Devices]
  * [THEOplayer How To's - THEOplayer Android SDK Integration]


## Quick Start

To use THEOplayer from a library downloaded from THEO Portal, follow these steps:
1. Obtain THEOplayer Android SDK and unzip it.

   Please visit [THEOplayer documentation] to get required THEOplayer Android SDK.

2. Copy **`theoplayer-android-[name]-[version]-minapi21-release.aar`** file from unzipped SDK into
   application **[libs]** folder and rename it to **`theoplayer.aar`**.

   Project is configured to load SDK with such name, for using other name please change
   `implementation ':theoplayer@aar'` dependency in [app-level build.gradle] file accordingly.

3. Open _**THEO Verizon Media**_ application in Android Studio.
   Android Studio should automatically synchronize and rebuild project. If this won't happen please
   select **File > Sync Project with Gradle Files** menu item to do it manually. Please note, that
   in very rare cases it will be required to synchronize project twice.

4. Select **Run > Run 'app'** menu item to run application on a device selected by default.


## Streams/Content Rights:

The DRM streams used in this app (if any) are provided by our Partner: [EZ DRM] and hold all
the rights for the content. These streams are DRM protected and cannot be used for any other purposes.


## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE] file for details.

[//]: # (Links and Guides reference)
[THEOplayer]: https://www.theoplayer.com/
[THEO Knowledge Base - Android Studio Setup]: guides/knowledgebase-android-studio-setup/README.md
[THEO Knowledge Base - Simple Android Application]: guides/knowledgebase-simple-application/README.md
[THEO Knowledge Base - Virtual and Physical Devices]: guides/knowledgebase-virtual-and-physical-devices/README.md
[THEOplayer How To's - THEOplayer Android SDK Integration]: guides/howto-theoplayer-android-sdk-integration/README.md
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing
[EZ DRM]: https://ezdrm.com/

[//]: # (Project files reference)
[LICENSE]: LICENSE
[libs]: app/libs
[app-level build.gradle]: app/build.gradle
