# OptiView Player Android Reference Apps

## License

This projects falls under the license as defined in https://github.com/THEOplayer/license-and-disclaimer.

## Introduction

The example apps in this repo are intended to demonstrate a common understanding of how the OptiView Player
(formerly THEOplayer) SDKs can be used. These apps have the following characteristics:

  * Easy to read code, allowing them to be used as samples on how specific features can be integrated.
  * Open source code, providing common ground when investigating issues, allowing the Dolby support
    team to showcase reference implementations, and customers to provide a clear reproduction project
    showcasing the issue they are seeing during integration or production.
  * Clean look and feel, allowing the apps to be used for marketing demo's where needed in an always
    up to date capability.
  * Extensible, making it easy to create new samples of features or set up reproduction and test cases.

## Rationale

In order to use the SDK in a streaming pipeline, it needs to be integrated within an application.
During the development of these applications, developers need access to solid documentation and
examples at the risk of integrations not being of sufficient quality. As these applications are
developed by and owned by customers, it is not always possible for OptiView team to get insights
into the code. As a result, when issues occur during integration or when the app is in production,
it can be difficult to analyse where the issue is. Similarly, when issues occur in the integrated
code which are hard to reproduce, this is most often related to mistakes in the integration.

## Reference Apps

We have provided the following applications showcasing some of the features of OptiView Player. 
These can be tested locally by opening this repository in Android Studio and selecting which app your want to launch.

  * [Basic Playback](./basic-playback/README.md)
  * [DRM Playback](./drm-playback/README.md)
  * [Open Video UI](./open-video-ui/README.md)
  * [Localization](./localization/README.md)
  * [Background Playback](./background-playback/README.md)
  * [Google-IMA](./google-ima/README.md)
  * [Google-DAI](./google-dai/README.md)
  * [OptiView Ads](./advertising-optiview-ads/README.md)
  * [Chromecast](./google-cast/README.md)
  * [Millicast](./streaming-millicast/README.md)
  * [THEOlive](./streaming-theolive/README.md)
  * [Metadata Handling](./metadata-handling/README.md)
  * [Fullscreen Handling](./full-screen-handling/README.md)
  * [Custom Surface Rendering](./custom-surface-rendering/README.md)
  * [Offline Playback](./offline-playback/README.md)
  * [PiP Handling](./pip-handling/README.md)
  * [Simple OTT App](./simple-ott/README.md)

We highly recommend reading through the [Basic Playback](./basic-playback/README.md) example first.
