# Reference Apps - THEO Basic Playback

The purpose of this app is to demonstrate how to integrate [THEOplayer] into an Android app and setup
playback of a sample stream.

## THEO Docs

The [THEOplayer Documentation] will provide an end-to-end explanation of how to setup an Android application with THEOplayer included. 
It will cover setting up an IDE, including all dependencies, adding THEOplayer to the activity and running the application.

## Quick Start

### Using THEOplayer from official Maven repository

The project is pre-configured to utilize THEOplayer from the [official THEOplayer Maven repository](https://maven.theoplayer.com/#/releases). 
The remaining steps involve cloning the repository and initiating the build process.

Below is a short explanation about how to include THEOplayer in the project.


To use THEOplayer from `maven.theoplayer.com`, you need to include it in the build.gradle file on the project level:

    dependencyResolutionManagement {
        repositories {
            ...
            maven { url = uri("https://maven.theoplayer.com/releases") }
            ...
        }
    }

Then, to use the library, specify the following in the module's level `build.gradle` file. 

    dependencies {
        ...
         implementation 'com.theoplayer.theoplayer-sdk-android:core:7.6.0'
        ...
    }

For the most recent version, see [maven.theoplayer.com](https://maven.theoplayer.com/#/releases/com/theoplayer/theoplayer-sdk-android/core).

### Adding your own license

In order to play your own streams, you need a THEOplayer license from our [THEO Portal].
This license can be put [here](../common/src/main/res/values/values.xml).

The sample applications then uses it in the [AndroidManifest.xml](src/main/AndroidManifest.xml) file:
```xml
    <application>
        <meta-data
            android:name="THEOPLAYER_LICENSE"
            android:value="@string/theoplayer_license" />
    </application>
```

### Adding your own sources

If you have added your own license, you can add your sources in our [SourceManager](../common/src/main/java/com/theoplayer/sample/common/SourceManager.kt) and make the applications use them.


## License

This project is licensed under the BSD 3 Clause License - see the [LICENSE] file for details.

[//]: # (Links and Guides reference)
[THEOplayer]: https://www.theoplayer.com/
[THEO Portal]: https://portal.theoplayer.com/
[THEOplayer Documentation]: https://www.theoplayer.com/docs/theoplayer/android/

[//]: # (Project files reference)
[LICENSE]: ../LICENSE
