# THEOplayer How To's - Full Screen Management

This guide is going to show how to deal with full screen mode, how to couple device orientation
with full screen mode, how to use THEOplayer SDK API to request full screen mode on demand and how
to create custom full screen activity.

To obtain THEOplayer Android SDK please visit [Get Started with THEOplayer].

Presented code snippets are taken from [THEO Full Screen Handling] reference app. Please note that
in this app all URLs are defined as an Android resource, but they can be inlined as well. Please check
[values.xml] file for URLs definition.


## Table of Contents

  * [Coupling Device Orientation with Full Screen Mode]
  * [Requesting Full Screen Mode]
  * [Custom Full Screen Activity]
    * [Applying Material Design]
  * [Listening to Full Screen Mode Related Events]
  * [Summary]


## Coupling Device Orientation with Full Screen Mode

It is very nice feature for user experience to enter full screen mode when device is rotated to
landscape orientation. THEOplayer supports such feature.

To allow coupling first Android needs to know that the application is going to handle itself
`orientation` and `screenSize` changes. To do that please update **`<activity>`** tag in the
**[AndroidManifest.xml]**, by adding **`android:configChanges`** attribute.

If you would like for the player to go into a particular orientation in fullscreen,
use the **`android:screenOrientation`** attribute. For all possible values, refer to the [activity documentation](https://developer.android.com/guide/topics/manifest/activity-element).
If you would like to do this programmatically, use the `fullscreenOrientation`flag as of v4.3.0.

```xml
<activity
    android:name=".PlayerActivity"
    android:configChanges="orientation|screenSize"
    android:screenOrientation="userLandscape"
    android:theme="@style/TheoTheme.SplashScreen">

    <!-- ... -->

</activity>
```

As a next and last step please enable **`fullScreenOrientationCoupled`** feature in **`THEOplayerView`
settings** (check [PlayerActivity.java] for full implementation):

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void configureTHEOplayer() {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.getSettings().setFullScreenOrientationCoupled(true);

        // Always go into a particular orientation when in fullscreen. (as of v4.3.0)
        // For all possible values see `ScreenOrientation`.
        viewBinding.theoPlayerView.getSettings().setFullScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
    }
}
```


## Requesting Full Screen Mode

It is possible to use THEOplayer SDK API to request full screen manually. Access to THEO's
`FullScreenManager` is needed in order to access that API:

```java
public class PlayerActivity extends AppCompatActivity {

    private ActivityPlayerBinding viewBinding;
    private FullScreenManager theoFullScreenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ...

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);

        // Gathering THEO objects references.
        theoFullScreenManager = viewBinding.theoPlayerView.getFullScreenManager();

        // ...
    }
}
```

Let's create "Full Screen" button. Check [activity_player.xml] for whole layout setup:

```xml
<androidx.constraintlayout.widget.ConstraintLayout>
    <!-- ... -->

    <Button
        android:id="@+id/fullScreenButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:layout_marginTop="@dimen/spaceMargin"
        android:text="@string/fullScreenLabel"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/descriptionTextView" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Now request entering full screen mode when "Full Screen" button is clicked:

```java
public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ...

        // Configure UI behavior and default values.
        viewBinding.fullScreenButton.setOnClickListener(
                view -> theoFullScreenManager.requestFullScreen()
        );

        // ...
    }
}
```

Call `theoFullScreenManager.exitFullScreen()` for exiting full screen mode, as shown in next section.


## Custom Full Screen Activity

`THEOplayerView` uses a separate `Activity` for full screen so that the full screen view is
completely isolated from the `Activity` the `THEOplayerView` is in. The `Activity` has its own
orientation, window settings, and back button behaviour. By using a separate `Activity`, we can
leave those settings and the view hierarchy intact in the `Activity` in which the `THEOplayerView`
is embedded.

In order to customize the behavior and/or look of the full screen activity, use custom full screen
activity class. The steps to do this are given below.

As a first step, extend THEO's `FullScreenActivity` (see [CustomFullScreenActivity.java] for full
implementation):

```java
public class CustomFullScreenActivity extends FullScreenActivity {

    private ActivityFullscreenBinding viewBinding;
    private FullScreenManager theoFullScreenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ...

        // Inflating custom view and obtaining an instance of the binding class.
        viewBinding = ActivityFullscreenBinding.inflate(LayoutInflater.from(this), null, false);
        getDelegate().addContentView(viewBinding.getRoot(), new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        // Gathering THEO objects references.
        // ...
        theoFullScreenManager = getTHEOplayerView().getFullScreenManager();

        // Configuring UI behavior.
        // ...
        viewBinding.exitFullScreenButton.setOnClickListener((button) -> onFullScreenExit());

        // ...
    }

    private void onFullScreenExit() {
        theoFullScreenManager.exitFullScreen();
    }

    private AppCompatDelegate getDelegate() { /* ... */ }

    // ...
}
```

For now `getDelegate()` is intentionally omitted, but we'll get back to that in [Applying Material Design]
section.

Our `CustomFullScreenActivity` is inflating `ActivityFullscreenBinding` view. This is class generated
automatically by used [Data Binding Library] (seee [THEOplayer How To's - THEOplayer Android SDK Integration]).
This view is defined in [activity_fullscreen.xml] file:

```xml
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    tools:context=".CustomFullScreenActivity">

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="@dimen/spaceMargin">

        <!-- ... -->

        <com.google.android.material.button.MaterialButton
            android:id="@+id/exitFullScreenButton"
            style="?attr/materialImageButtonStyle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_margin="@dimen/spaceMargin"
            app:icon="@drawable/ic_fullscreen_exit" />

    </LinearLayout>

</layout>
```

Note that `@+id/exitFullScreenButton` has style `?attr/materialImageButtonStyle` applied. Its
definition can be found in [styles.xml], but for now it's not important, see [Applying Material Design]
section for more details.

As a last step, configure `THEOplayerView` to use custom full screen activity:

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void configureTHEOplayer() {
        // ...

        // Setting custom full screen activity which allows to change behavior
        // and/or look of the full screen activity.
        theoFullScreenManager.setFullscreenActivity(CustomFullScreenActivity.class);

        // ...
    }
}
```

### Applying Material Design

THEO's `FullScreenActivity` extends `Activity`, but to have [Material Design] applied it is expected
to extend `AppCompatActivity` instead, but it's not possible. To achieve that, create
`AppCompatDelegate` instance:

```java
public class CustomFullScreenActivity extends FullScreenActivity {

    private AppCompatDelegate appCompatDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Adding support for extended AppCompat features.
        // It allows to use styles and themes defined for material components.
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

        // ...
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    private AppCompatDelegate getDelegate() {
        if (appCompatDelegate == null) {
            appCompatDelegate = AppCompatDelegate.create(this, null);
        }
        return appCompatDelegate;
    }

    // ...
}
```

Having that, all views can be inflated by calling `getDelegate()` method and defined widgets can
have material look&feel applied, as shown in [Custom Full Screen Activity] section.


## Listening to Full Screen Mode Related Events

Additionally, to get better view of actual full screen state, define `FullScreenChangeListener`:

```java
public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    // ...

    private void configureTHEOplayer() {
        // ...

        // Adding listeners to THEOplayer basic full screen changes events.
        theoFullScreenManager.addFullScreenChangeListener(new FullScreenChangeListener() {

            @Override
            public void onEnterFullScreen() {
                Log.i(TAG, "Event: FULL_SCREEN_ENTERED");
            }

            @Override
            public void onExitFullScreen() {
                Log.i(TAG, "Event: FULL_SCREEN_EXITED");
            }

        });
    }
}
```


## Summary

This guide showed how to create custom full screen activity, how to set full screen mode depending
on device orientation and how to sue THEOplayer SDK API to request full screen mode.

For more guides about THEOplayer SDK API usage and tips&tricks please visit [THEO Docs] portal.


[//]: # (Sections reference)
[Coupling Device Orientation with Full Screen Mode]: #coupling-device-orientation-with-full-screen-mode
[Requesting Full Screen Mode]: #requesting-full-screen-mode
[Custom Full Screen Activity]: #custom-full-screen-activity
[Applying Material Design]: #applying-material-design
[Listening to Full Screen Mode Related Events]: #listening-to-full-screen-mode-related-events
[Summary]: #summary

[//]: # (Links and Guides reference)
[THEO Full Screen Handling]: ../..
[THEO Docs]: https://docs.portal.theoplayer.com/
[THEOplayer How To's - THEOplayer Android SDK Integration]: ../../../Basic-Playback/guides/howto-theoplayer-android-sdk-integration/README.md
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing
[Data Binding Library]: https://developer.android.com/topic/libraries/data-binding
[Material Design]: https://developer.android.com/guide/topics/ui/look-and-feel

[//]: # (Project files reference)
[AndroidManifest.xml]: ../../src/main/AndroidManifest.xml
[PlayerActivity.java]: ../../src/main/java/com/theoplayer/sample/ui/fullscreen/PlayerActivity.java
[CustomFullScreenActivity.java]: ../../src/main/java/com/theoplayer/sample/ui/fullscreen/CustomFullScreenActivity.java
[values.xml]: ../../src/main/res/values/values.xml
[styles.xml]: ../../src/main/res/values/styles.xml
[activity_player.xml]: ../../src/main/res/layout/activity_player.xml
[activity_fullscreen.xml]: ../../src/main/res/layout/activity_fullscreen.xml
