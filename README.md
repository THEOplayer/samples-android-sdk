# THEOplayer Android Reference Apps

## License

This projects falls under the license as defined in https://github.com/THEOplayer/license-and-disclaimer.

## Introduction

In order to get a common understanding of how the THEOplayer SDKs are to be used, we will include
rich example apps. These apps will have the following characteristics:

  * Easy to read code, allowing them to be used as samples on how specific features can be integrated.
  * Open source code, providing common ground when investigating issues, allowing the THEO support
    team to showcase reference implementations, and customers to provide a clear reproduction project
    showcasing the issue they are seeing during integration or production.
  * Clean look and feel, allowing the apps to be used for marketing demo's where needed in an always
    up to date capability.
  * Extensible, making it easy to create new samples of features or set up reproduction and test cases.


## Rationale

In order to use the SDK in a streaming pipeline, it needs to be integrated within an application.
During the development of these applications, developers need access to solid documentation and
examples at the risk of integrations not being of sufficient quality. As these applications are
developed by and owned by customers, it is not always possible for THEOplayer team to get insights
into the code. As a result, when issues occur during integration or when the app is in production,
it can be difficult to analyse where the issue is. Similarly, when issues occur in the integrated
code which are hard to reproduce, this is most often related to mistakes in the integration.


## Reference Apps

We have provided the following applications showcasing some of the features of THEOplayer. 
These can be tested locally by opening this repository in Android Studio and selecting which app your want to launch.

  * [THEO Basic Playback](./basic-playback/README.md)
  * [THEO DRM Playback](./drm-playback/README.md)
  * [THEO Open Video UI](./open-video-ui/README.md)
  * [THEO Background Playback](./background-playback/README.md)
  * [THEO Google-IMA](./google-ima/README.md)
  * [THEO Casting](./google-cast/README.md)
  * [THEO Metadata Handling](./metadata-handling/README.md)
  * [THEO Full Screen Handling](./full-screen-handling/README.md)
  * [THEO Custom Surface Rendering](./custom-surface-rendering/README.md)
  * [THEO Offline Playback](./offline-playback/README.md)
  * [THEO PiP Handling](./pip-handling/README.md)
  * [THEO Simple OTT App](./simple-ott/README.md)

We highly recommend reading through the [Basic Playback](./basic-playback/README.md) example first.
