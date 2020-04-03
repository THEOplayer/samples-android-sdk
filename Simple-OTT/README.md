# Reference Apps - THEO Simple OTT

The purpose of this app is to demonstrate how [THEOplayer] could be used in a "real" production-like
application.

For quick start, please proceed with the [Quick Start](#quick-start) guide.  
For application architecture, please proceed with the [Application Architecture](#application-architecture) guide.


## Guides

_**THEO Simple OTT**_ application shows various THEOplayer features used together. To learn more
about them please check following guides:

  * [THEOplayer How To's - Full Screen Management]
  * [THEOplayer How To's - Google Cast Integration]
  * [THEOplayer How To's - Downloading Stream Content]

This app is an extension of [THEO Basic Playback] application. For help with getting started with
THEOplayer or Android Studio feel free to check related guides:

  * [THEO Knowledge Base - Android Studio Setup]
  * [THEO Knowledge Base - Virtual and Physical Devices]
  * [THEOplayer How To's - THEOplayer Android SDK Integration]


## Quick Start

1. Obtain THEOplayer Android SDK with **Caching** and **ExoPlayer** features enabled and unzip it.

   Please visit [Get Started with THEOplayer] to get required THEOplayer Android SDK.

2. Copy **`theoplayer-android-[name]-[version]-minapi16-release.aar`** file from unzipped SDK into
   application **[libs]** folder and rename it to **`theoplayer.aar`**.

   Project is configured to load SDK with such name, for using other name please change
   `implementation ':theoplayer@aar'` dependency in [app-level build.gradle] file accordingly.

   Please check [THEOplayer How To's - THEOplayer Android SDK Integration] guide for more information
   about integrating THEOplayer Android SDK.

3. Open _**THEO Simple OTT**_ application in Android Studio.

   For more information about installing Android Studio please check
   [THEO Knowledge Base - Android Studio Setup] guide.

   Android Studio should automatically synchronize and rebuild project. If this won't happen please
   select **File > Sync Project with Gradle Files** menu item to do it manually. Please note, that
   in very rare cases it will be required to synchronize project twice.

4. Select **Run > Run 'app'** menu item to run application on a device selected by default.

   To change the device please select **Run > Select Device...** menu item. For more information
   about working with Android devices please check [THEO Knowledge Base - Virtual and Physical Devices]
   guide.


## Application Architecture

Application presents view of four tabs:

  * **LIVE** - where live streams can be played
  * **ON DEMAND** - where VoD streams can be played
  * **OFFLINE** - where available VoD streams can be downloaded nad played
  * **SETTINGS** - where all downloaded streams can be removed and download preferences can be set

![Architecture Diagram](guides/images/architecture_diagram.png "Architecture Diagram")

Streams presented on tabs are defined in [stream_sources.json] file stored in application raw
resources. They can be easily updated. Every tab that displays streams has its own section in this
JSON configuration. The stream sources JSON configuration should be structured as follows:

```
{
  "live": StreamSource[],
  "onDemand": StreamSource[],
  "offline": StreamSource[]
}
```

where `StreamSource` has following structure:

```
{
  "title": "Stream Title",
  "description": "Some Stream Description",
  "image": "@drawable/streamImage",
  "source": "hxxps://some.host.com/some-asset.m3u8"
}
```

Please note that `image` should keep reference to existing drawable resource.


## Streams/Content Rights:

The DRM streams used in this app (if any) are provided by our Partner: [EZ DRM] and hold all
the rights for the content. These streams are DRM protected and cannot be used for any other purposes.


## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE] file for details.


[//]: # (Links and Guides reference)
[THEOplayer]: https://www.theoplayer.com/
[THEO Basic Playback]: ../Basic-Playback
[THEO Knowledge Base - Android Studio Setup]: ../Basic-Playback/guides/knowledgebase-android-studio-setup/README.md
[THEO Knowledge Base - Virtual and Physical Devices]: ../Basic-Playback/guides/knowledgebase-virtual-and-physical-devices/README.md
[THEOplayer How To's - THEOplayer Android SDK Integration]: ../Basic-Playback/guides/howto-theoplayer-android-sdk-integration/README.md
[THEOplayer How To's - Full Screen Management]: ../Full-Screen-Handling/guides/howto-full-screen-management/README.md
[THEOplayer How To's - Google Cast Integration]: ../Google-Cast/guides/howto-google-cast-integration/README.md
[THEOplayer How To's - Downloading Stream Content]: ../Offline-Playback/guides/howto-downloading-stream-content/README.md
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing
[EZ DRM]: https://ezdrm.com/

[//]: # (Project files reference)
[LICENSE]: LICENSE
[libs]: app/libs
[app-level build.gradle]: app/build.gradle
[stream_sources.json]: app/src/main/res/raw/stream_sources.json
