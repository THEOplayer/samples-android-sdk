# THEOplayer Android Reference Apps

In order to get a common understanding of how the THEOplayer SDKs are to be used, we will include
rich example apps. These apps will have following characteristics:

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

In order to keep these apps as simple as possible, we will maintain a set of different sample apps
over a single sample app showing all different use cases.

  * [THEO Basic Playback](./Basic-Playback/README.md)
  * [THEO DRM Playback](./DRM-Playback/README.md)
  * [THEO Basic Ads](./Basic-Ads/README.md)
  * [THEO Google IMA](./Google-IMA/README.md)
  * [THEO Custom URL Playback](./Custom-URL-Playback/README.md)
  * [THEO Custom Ads](./Custom-Ads/README.md)
  * [THEO Google Cast](./Google-Cast/README.md)
  * [THEO Full Screen Handling](./Full-Screen-Handling/README.md)
  * [THEO PiP Handling](./PiP-Handling/README.md)
  * [THEO Metadata Handling](./Metadata-Handling/README.md)
  * [THEO Offline Playback](./Offline-Playback/README.md)
  * [THEO Custom UI](./Custom-UI/README.md)
  * [THEO Simple OTT](./Simple-OTT/README.md)
  * [THEO Programmable Stream](./Programmable-Stream/README.md)


## Streams/Content Rights:

The DRM streams are provided by our Partner: [EZ DRM](https://ezdrm.com/) and hold all the rights
for the content. These streams are DRM protected and cannot be used for any other purposes.


## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE](./LICENSE) file for details.
