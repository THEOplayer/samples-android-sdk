# THEOplayer How To's - Google Cast Integration

This guide covers steps needed to integrate Android application with [Google Cast].

To obtain THEOplayer Android SDK with Chromecast feature enabled please visit
[Get Started with THEOplayer].

Presented code snippets are taken from [THEO Google Cast] reference app. Please note that in this
app all URLs are defined as an Android resource, but they can be inlined as well. Please check
[values.xml] file for URLs definition.


## Table of Contents

  * [Adding Required Dependencies]
  * [Initializing Cast Context]
  * [Adding Cast Button]
  * [Defining Cast Metadata]
  * [Defining Custom Cast Source]
  * [Defining Cast Strategy]
  * [Listening to Cast Related Events]
  * [Summary]


## Adding Required Dependencies

To add support for Google Cast the first thing to do is to include the Cast Framework and
Media Router libraries under the `dependencies` node in [app-level build.gradle] file:

```groovy
dependencies {
    // ...

    implementation 'androidx.mediarouter:mediarouter:1.1.0'
    implementation 'com.google.android.gms:play-services-cast-framework:18.1.0'

    // ...
}
```

After saving changes please select **File > Sync Project with Gradle Files** menu item to synchronize
the project.


## Initializing Cast Context

The app must implement the [OptionsProvider] interface to supply options needed to initialize
the `CastContext` singleton. A `DefaultCastOptionsProvider` is provided as part of the THEOplayer
Android SDK. This `OptionsProvider` associates the THEOplayer chromecast receiver with the sender
application. Class needs to be registered in [AndroidManifest.xml] file within a meta-data tag under
the `<application>` node:

```xml
<application>

    <meta-data
        android:name="com.google.android.gms.cast.framework.OPTIONS_PROVIDER_CLASS_NAME"
        android:value="com.theoplayer.android.api.cast.chromecast.DefaultCastOptionsProvider" />

    <!-- ... -->

</application>
```

Optionally, custom settings can be provided to integrate with Google Cast (e.g. a custom receiver id).
This can be done by creating custom [OptionsProvider] implementation:

```java
public class CastOptionsProvider implements OptionsProvider {

    @Override
    public CastOptions getCastOptions(Context context) {
            return new CastOptions.Builder()
                .setReceiverApplicationId("<the_receiver_app_id>")
                .build();
    }

    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }
}
```

Having that, THEO cast session manager (`Chromecast` object) and global cast context can be accessed
as follows:

```java
public class PlayerActivity extends AppCompatActivity {

    private Chromecast theoChromecast;
    private CastContext castContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ...

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);

        // Gathering THEO objects references.
        theoChromecast = viewBinding.theoPlayerView.getCast().getChromecast();
        castContext = CastContext.getSharedInstance(this);

        // ...
    }
}
```

Please note that in our example [Data Binding Library] is used. For more detailed information about
defining `THEOplayerView` please check [THEOplayer How To's - THEOplayer Android SDK Integration] guide.


## Adding Cast Button

This is not required to have cast button added to have working Google Cast integration, but
it's wort to have it, to inform user about casting availability and allow them to manage global
cast session.

The cast button is visible when a receiver that supports the app is discovered. When the user
first clicks on the cast button, a cast dialog is displayed which lists the discovered devices.
When the user clicks on the cast button while the device is connected, it displays the current
media metadata (such as title, name of the recording studio and a thumbnail image) or allows the
user to disconnect from the cast device.

We are going to add cast button on the application bar. To do that prepare [activity_player_menu.xml]
in [menu] resource folder:

```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <item
        android:id="@+id/castMenuItem"
        android:title="@string/castMenuItem"
        app:actionProviderClass="androidx.mediarouter.app.MediaRouteActionProvider"
        app:showAsAction="always" />

</menu>
```

Now inflate that menu in the main activity (here [PlayerActivity.java]) by overriding
`onCreateOptionsMenu` method. After that, use [CastButtonFactory] to wire created menu item up
with the framework:

```java
public class PlayerActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_player_menu, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.castMenuItem);
        return true;
    }
}
```

To discover other Cast Widgets please visit [Cast UX Widgets] page.


## Defining Cast Metadata

When the cast button is clicked while the device is connected, then the current media metadata, such
as title, name of the recording studio and thumbnail image can be displayed. Such metadata are also
used be cast receiver application.

To have such metadata displayed, build `ChromecastMetadataDescription` object and attach it to
the `SourceDescritpion` definition by calling `metadata` method:

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void configureTHEOplayer() {
        // ...

        // Creating a ChromecastMetadataDescription builder that defines stream metadata to be
        // displayed on cast sender and receiver while casting.
        ChromecastMetadataDescription.Builder chromecastMetadata = ChromecastMetadataDescription.Builder
                .chromecastMetadata()
                .title(getString(R.string.defaultTitle))
                .images(getString(R.string.defaultPosterUrl));

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        SourceDescription.Builder sourceDescription = SourceDescription.Builder
                .sourceDescription(typedSource.build())
                .poster(getString(R.string.defaultPosterUrl))
                .metadata(chromecastMetadata.build());

        // ...
    }
}
```

## Defining Custom Cast Source

Some streaming set-up requires casting a different stream to a Cast Receiver device than the one
playing on a Cast Receiver device. This can be caused for example by different DRM tokens provisioned
for a specific device.

To define custom cast source please define separate `SourceDescription` definition, the same way as
regular THEOplayer source. Once created please attach it directly to `Chromecast` instance that can
be gathered from `THEOplayerView` (see [Initializing Cast Context] section):

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void configureTHEOplayer() {
        // ...

        // Some streaming setups requires casting a different stream to a Cast Receiver device
        // than the one playing on a Cast Sender device, e.g. different DRM capabilities.
        // Code below shows how to configure such different stream to cast.
        SourceDescription.Builder otherSourceDescription = SourceDescription.Builder
                .sourceDescription(getString(R.string.defaultSourceUrl));
        theoChromecast.setSource(otherSourceDescription.build());

        // ...
    }
}
```


## Defining Cast Strategy

THEOplayer supports Google Cast session takeover. This means that when the user has selected a cast
device and it is casting, a new video will automatically take over the existing session. In this
manner, the user is not prompted for a cast device again so the viewing experience is faster and smoother.

Three types of behaviour are defined:

  * **`auto`** - when a cast session already exists, **the player will automatically start casting**
    when the play button is clicked. It is possible to start a session by clicking the cast button
    or using the global API.
  * **`manual`** - when a cast session exists **the player will NOT automatically start casting**.
    However, when the cast button is clicked and a session exists, the existing session is used
    and the user is not prompted with a dialog.
  * **`disabled`** - the player is not affected by Google Cast.

To set cast strategy variant, set  **`app:castStrategy`** property to in the **`THEOplayerView`**
definition. It can be found in the main activity layout file **[activity_player.xml]**:

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- ... -->

    <com.theoplayer.android.api.THEOplayerView
        android:id="@+id/theoPlayerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:castStrategy="auto"
        app:layout_behavior="@string/appbar_scrolling_view_behavior" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```


## Listening to Cast Related Events

Additionally, specifying few event listeners will help to get better view of actual cast session state:

```java
public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    // ...

    private void configureTHEOplayer() {
        // ...

        // Adding listeners to THEOplayer cast events.
        theoChromecast.addEventListener(ChromecastEventTypes.STATECHANGE,
            event -> Log.i(TAG, "Event: CAST_STATECHANGE, state=" + event.getState()));
        theoChromecast.addEventListener(ChromecastEventTypes.ERROR,
            event -> Log.i(TAG, "Event: CAST_ERROR, error=" + event.getError()));
    }
}
```


## Summary

This guide covered ways of integrating application with [Google Cast]. For more detailed information
about configuring Google Cast sender application and other Cast widgets please check
[Google Cast Sender Application] guide.

For more guides about THEOplayer SDK API usage and tips&tricks please visit [THEO Docs] portal.


[//]: # (Sections reference)
[Adding Required Dependencies]: #adding-required-dependencies
[Initializing Cast Context]: #initializing-cast-context
[Adding Cast Button]: #adding-cast-button
[Defining Cast Metadata]: #defining-cast-metadata
[Defining Custom Cast Source]: #defining-custom-cast-source
[Defining Cast Strategy]: #defining-cast-strategy
[Listening to Cast Related Events]: #listening-to-cast-related-events
[Summary]: #summary

[//]: # (Links and Guides reference)
[THEO Google Cast]: ../..
[THEO Docs]: https://docs.portal.theoplayer.com/
[THEOplayer How To's - THEOplayer Android SDK Integration]: ../../../Basic-Playback/guides/howto-theoplayer-android-sdk-integration/README.md
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing
[Google Cast]: http://www.google.com/cast/
[Google Cast Sender Application]: https://developers.google.com/cast/docs/android_sender
[Cast UX Widgets]: https://developers.google.com/cast/docs/android_sender/integrate#the_cast_ux_widgets
[OptionsProvider]: https://developers.google.com/cast/docs/reference/android/com/google/android/gms/cast/framework/OptionsProvider
[CastButtonFactory]: https://developers.google.com/android/reference/com/google/android/gms/cast/framework/CastButtonFactory
[Data Binding Library]: https://developer.android.com/topic/libraries/data-binding

[//]: # (Project files reference)
[AndroidManifest.xml]: ../../app/src/main/AndroidManifest.xml
[app-level build.gradle]: ../../app/build.gradle
[PlayerActivity.java]: ../../app/src/main/java/com/theoplayer/sample/playback/googlecast/PlayerActivity.java
[activity_player.xml]: ../../app/src/main/res/layout/activity_player.xml
[menu]: ../../app/src/main/res/menu
[activity_player_menu.xml]: ../../app/src/main/res/menu/activity_player_menu.xml
[values.xml]: ../../app/src/main/res/values/values.xml
