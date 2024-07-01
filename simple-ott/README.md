# Reference Apps - THEO Simple OTT

## Prerequisite

Please read through the quick start section of the [Basic Playback] application before continuing.

## THEO Simple OTT

The purpose of this app is to demonstrate how [THEOplayer] could be used in a "real" production-like
application.

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
[Basic Playback]: ../basic-playback/README.md

[//]: # (Project files reference)
[LICENSE]: ../LICENSE
[stream_sources.json]: src/main/res/raw/stream_sources.json
