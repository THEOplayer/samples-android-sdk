# Reference Apps - Supporting background playback

The purpose of this app is to demonstrate how to use [THEOplayer] to:

- play-out media assets in a background service;
- use the [Media Session Connector] to report the playback state and receive player requests.

For quick start, please proceed with the [Quick Start](#quick-start) guide.
More information on how to use the [Media Session Connector] can be found on its documentation page and
the Android developer pages describing [Building a video app] and [Building an audio app].

## Guides

This app is an extension of a [THEO Basic Playback] application. For help with getting started with
THEOplayer, Android Studio and Android Media Sessions feel free to check related guides:

* [THEO Knowledge Base - Android Studio Setup]
* [THEO Knowledge Base - Virtual and Physical Devices]
* [THEOplayer How To's - THEOplayer Android SDK Integration]

## Quick Start

1. Optionally obtain a THEOplayer license. By default the THEOplayer SDK only plays assets hosted on
   the `theoplayer.com` domain. For play-out of other streams first obtain a license at the
   [THEOplayer portal](https://portal.theoplayer.com/).

2. Open _**THEO Custom Ads**_ application in Android Studio.

   For more information about installing Android Studio please check
   [THEO Knowledge Base - Android Studio Setup] guide.

   Android Studio should automatically synchronize and rebuild project. If this won't happen please
   select **File > Sync Project with Gradle Files** menu item to do it manually. Please note, that
   in very rare cases it will be required to synchronize project twice.

3. Specify the obtained THEOplayer license in the [AndroidManifest.xml](./app/src/main/AndroidManifest.xml),
   if applicable.

```xml
    <application>
        <meta-data
            android:name="THEOPLAYER_LICENSE"
            android:value="@string/theoplayer_license" />
    </application>
```

4. Select **Run > Run 'app'** menu item to run application on a device selected by default.

   To change the device please select **Run > Select Device...** menu item. For more information
   about working with Android devices please check [THEO Knowledge Base - Virtual and Physical Devices]
   guide.

## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE] file for details.

[//]: # (Links and Guides reference)
[THEOplayer]: https://www.theoplayer.com/
[THEO Basic Playback]: ../Basic-Playback
[THEO Knowledge Base - Android Studio Setup]: ../Basic-Playback/guides/knowledgebase-android-studio-setup/README.md
[THEO Knowledge Base - Virtual and Physical Devices]: ../Basic-Playback/guides/knowledgebase-virtual-and-physical-devices/README.md
[THEOplayer How To's - THEOplayer Android SDK Integration]: ../Basic-Playback/guides/howto-theoplayer-android-sdk-integration/README.md
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing
[Media Session Connector]: https://github.com/THEOplayer/android-connector/tree/main/mediasession/connectors
[Android Developer Guides - Working With a MediaSession]: https://developer.android.com/guide/topics/media-apps/working-with-a-media-session
[Building a video app]: https://developer.android.com/guide/topics/media-apps/video-app/building-a-video-app
[Building an audio app]: https://developer.android.com/guide/topics/media-apps/audio-app/building-an-audio-app

[//]: # (Project files reference)
[LICENSE]: LICENSE
