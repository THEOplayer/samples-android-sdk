# Reference Apps - THEO Basic Playback

The purpose of this app is to demonstrate how to integrate [THEOplayer] into an Android app and setup
playback of a sample stream.

For quick start, please proceed with the [Quick Start](#quick-start) guide.


## Guides

The guides below will provide an end-to-end explanation of how to setup an Android application with
THEOplayer included. It will cover setting up an IDE, including all dependencies, adding THEOplayer
to the activity and running the application.

  * [THEO Knowledge Base - Android Studio Setup]
  * [THEO Knowledge Base - Simple Android Application]
  * [THEO Knowledge Base - Virtual and Physical Devices]
  * [THEOplayer How To's - THEOplayer Android SDK Integration]


## Quick Start

1. Obtain THEOplayer Android SDK and unzip it.

   Please visit [Get Started with THEOplayer] to get required THEOplayer Android SDK.

2. Copy **`theoplayer-android-[name]-[version]-minapi16-release.aar`** file from unzipped SDK into
   application **[libs]** folder and rename it to **`theoplayer.aar`**.

   Project is configured to load SDK with such name, for using other name please change
   `implementation ':theoplayer@aar'` dependency in [app-level build.gradle] file accordingly.

   Please check [THEOplayer How To's - THEOplayer Android SDK Integration] guide for more information
   about integrating THEOplayer Android SDK.

3. Open _**THEO Basic Playback**_ application in Android Studio.

   For more information about installing Android Studio please check
   [THEO Knowledge Base - Android Studio Setup] guide.

   Android Studio should automatically synchronize and rebuild project. If this won't happen please
   select **File > Sync Project with Gradle Files** menu item to do it manually. Please note, that
   in very rare cases it will be required to synchronize project twice.

4. Select **Run > Run 'app'** menu item to run application on a device selected by default.

   To change the device please select **Run > Select Device...** menu item. For more information
   about working with Android devices please check [THEO Knowledge Base - Virtual and Physical Devices]
   guide.


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
