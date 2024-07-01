# Reference Apps - Supporting background playback

## Prerequisite

Please read through the quick start section of the [Basic Playback] application before continuing.

## Background Playback

The purpose of this app is to demonstrate how to use [THEOplayer] to:

- play-out media assets in a background service;
- use the [Media Session Connector] to report the playback state and receive player requests.

More information on how to use the [Media Session Connector] can be found on its documentation page and
the Android developer pages describing [Building a video app] and [Building an audio app].

All information on how to use these APIs can be found in the [PlayerActivity](src/main/java/com/theoplayer/sample/background/PlayerActivity.kt).

## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE] file for details.

[//]: # (Links and Guides reference)
[THEOplayer]: https://www.theoplayer.com/
[Basic Playback]: ../basic-playback/README.md
[Media Session Connector]: https://github.com/THEOplayer/android-connector/tree/master/connectors/mediasession
[Android Developer Guides - Working With a MediaSession]: https://developer.android.com/guide/topics/media-apps/working-with-a-media-session
[Building a video app]: https://developer.android.com/guide/topics/media-apps/video-app/building-a-video-app
[Building an audio app]: https://developer.android.com/guide/topics/media-apps/audio-app/building-an-audio-app

[//]: # (Project files reference)
[LICENSE]: ../LICENSE
