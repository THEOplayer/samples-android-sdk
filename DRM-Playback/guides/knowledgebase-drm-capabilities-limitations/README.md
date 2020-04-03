# THEO Knowledge Base - DRM Capabilities Limitations

This guide is going to describe DRM capabilities limitations on Android devices.

For more information about DRM Systems please check [THEO Knowledge Base - DRM Systems] guide.


## Table of Contents

  * [Widevine DRM System]
  * [Widevine Security Levels]
  * [Why Widevine Security Level Matters?]
  * [Summary]


## Widevine DRM System

In order to be able to access DRM-protected content, Android device or media player needs
to support the required DRM that content needs. Otherwise unexpected issues might occur while trying
to watch certain content.

To trust that Android devices are secure from piracy, streaming services are making use of Google's
[Widevine DRM] platform.

Widevine is an industry standard to protect content as it's transferred over the internet and played
back on devices. It uses a combination of Common Encryption, licensing key exchange, and streaming
quality. The idea is to simplify the amount of work on the service provider's end by supporting
multiple levels of streaming quality based on the security capabilities of the receiving device.

THEOplayer supports Widevine over all security levels.


## Widevine Security Levels

Security levels are based on usage of the TEE ([Trusted Execution Environment]) - a secure area of
main processor, which runs in parallel of the operating system, in an isolated environment. TEE
guarantees that the loaded code and data are protected with respect to confidentiality and integrity.

Widevine protects content across three levels of security, simply named L3, L2 and L1, where
the most secure way of protecting content is L1:

  * **L1** - All content processing, cryptography and control is performed within the TEE
    of the device's processor, to prevent external tampering and copying of the media file.

  * **L2** - Only cryptography operations, but not video processing, are handled inside the TEE.

  * **L3** - Content processing and cryptography operations are intentionally handled outside
    of a TEE, or the device doesn't support a TEE.


## Why Widevine Security Level Matters?

A number of video streaming services don't allow some devices to stream movies or TV shows
at resolutions higher than 480p. To play Full HD or 4K content they require the highest security
level - **Widevine L1**.

The reason for the lockout is that these services are protected by Digital Rights Management (DRM),
to prevent the copying and unauthorized redistribution of these video files.


## Summary

This guide explained what are the DRM capabilities limitations. For more information about inspecting
DRM capabilities on the Android device feel free to check
[THEOplayer How To's - Inspecting DRM Capabilities] guide.

For more guides about THEOplayer please visit [THEO Docs] portal.


[//]: # (Sections reference)
[Widevine DRMSystem]: #widevine-drm-system
[Widevine Security Levels]: #widevine-security-levels
[Why Widevine Security Level Matters?]: #why-widevine-security-level-matters
[Summary]: #summary

[//]: # (Links and Guides reference)
[THEO Docs]: https://docs.portal.theoplayer.com/
[THEO Knowledge Base - DRM Systems]: https://docs.portal.theoplayer.com/docs/docs/advanced-topics/content-protection/content-protection-1-digital-rights-management-drm-systems
[THEOplayer How To's - Inspecting DRM Capabilities]: ../howto-inspecting-drm-capabilities/README.md
[Widevine DRM]: https://www.widevine.com/solutions/widevine-drm
[Trusted Execution Environment]: https://en.wikipedia.org/wiki/Trusted_execution_environment
