# Reference Apps - THEO Programmable Stream

The purpose of this app is to easily reproduce any [THEOplayer] configuration.

For quick start, please proceed with the [Quick Start](#quick-start) guide.  
For application architecture, please proceed with the [Application Architecture](#application-architecture) guide.

## THEO Docs

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

### Using library downloaded from [THEO Portal]

To use THEOplayer from a library downloaded from THEO Portal, follow these steps:
1. Obtain THEOplayer Android SDK and unzip it.

   Please visit [THEOplayer documentation] to get required THEOplayer Android SDK.

2. Copy **`theoplayer-android-[name]-[version]-minapi21-release.aar`** file from unzipped SDK into
   application **[libs]** folder and rename it to **`theoplayer.aar`**.

   Project is configured to load SDK with such name, for using other name please change
   `implementation ':theoplayer@aar'` dependency in [app-level build.gradle] file accordingly.

3. Open _**THEO Programmable Stream**_ application in Android Studio.

   Android Studio should automatically synchronize and rebuild project. If this won't happen please
   select **File > Sync Project with Gradle Files** menu item to do it manually. Please note, that
   in very rare cases it will be required to synchronize project twice.

4. Select **Run > Run 'app'** menu item to run application on a device selected by default.



## Application Architecture

Application requires URL to remote JSON configuration that can be loaded. Such URL is defined in
`defaultJsonConfigUrl` string resource (see [values.xml]):

```xml
<resources>
    <string name="defaultJsonConfigUrl">hxxps://some-host.com/some_configuration.json</string>
</resources>
```

The JSON configuration file should be structured as follows:

```
{
    playerConfiguration: PlayerConfiguration,
    source: SourceDescription
}
```

where:
  * `playerConfiguration` should be able to take any [PlayerConfiguration] without taking into
    account analytics integrations,
  * `source` should be able to take any [SourceDescription] not taking into account analytics
    integrations and ad integrations other than Google IMA or the THEO ad system.

Once stream is loaded, various information about the stream itself can be checked on the following tabs:

  * **Time Info**:
    * current time
    * current Program Date Time (if available)
    * duration
    * buffered time ranges
    * played time ranges
    * seekable time ranges
  * **Tracks Info**:
    * available audio tracks, indicating which track is active
    * available video tracks, their qualities and the active quality
    * available text tracks including which tracks are active with an option should be available to
      inspect the cues-property
  * **State & Logs**:
    * preload setting
    * error on the player (if available)
    * player status
    * event-log indicating which events were dispatched by the player
  * **Ads**:
    * current ad breaks
    * scheduled ad breaks


## Streams/Content Rights:

The DRM streams used in this app (if any) are provided by our Partner: [EZ DRM] and hold all
the rights for the content. These streams are DRM protected and cannot be used for any other purposes.


## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE] file for details.


[//]: # (Links and Guides reference)
[THEOplayer]: https://www.theoplayer.com/
[THEOplayer documentation]: https://docs.theoplayer.com/getting-started/01-sdks/02-android/00-getting-started.md#getting-started-on-android
[THEO Basic Playback]: ../Basic-Playback
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing
[EZ DRM]: https://ezdrm.com/
[PlayerConfiguration]: https://docs.theoplayer.com/api-reference/web/theoplayer.playerconfiguration.md
[SourceDescription]: https://docs.theoplayer.com/api-reference/web/theoplayer.source.md

[//]: # (Project files reference)
[LICENSE]: LICENSE
[libs]: app/libs
[app-level build.gradle]: app/build.gradle
[values.xml]: app/src/main/res/values/values.xml
