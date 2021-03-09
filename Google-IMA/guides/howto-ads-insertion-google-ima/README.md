# THEOplayer How To's - Ads Insertion with Google IMA

This guide covers steps needed to insert ads to the configured stream using the [Google IMA] ads module.

To obtain THEOplayer Android SDK  with Ads feature enabled please visit [Get Started with THEOplayer].

Presented code snippets are taken from [THEO Google IMA] reference app. Please note that in this
app all URLs are defined as an Android resource, but they can be inlined as well. Please check
[values.xml] file for URLs definition.


## Table of Contents

  * [Include the IMA SDK](#include-the-ima-sdk)
  * [Enabling Google IMA Integration]
  * [Injecting VAST Ads]
    * [Linear Pre-Roll Ad]
    * [Non-linear Pre-Roll Ad]
    * [Skippable Linear Mid-Roll Ad]
  * [Injecting VPAID Ads]
  * [Injecting VMAP Ads]
  * [Listening to Ad Related Events]
  * [Summary]


## Include the IMA SDK

You must include the native Google IMA Android SDK.

* You can include this through Gradle, as demonstrated by the dependencies `app/build.gradle`.
* You can manually download and install the Google IMA Android SDK, as described at [https://github.com/googleads/googleads-ima-android/releases](https://github.com/googleads/googleads-ima-android/releases).

## Enabling Google IMA Integration

To use the Google IMA integration please set **`app:googleIma`** property to **`true`** in
the **`THEOplayerView`** definition. It can be found in the main activity layout file
**[activity_player.xml]**:

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- ... -->

    <com.theoplayer.android.api.THEOplayerView
        android:id="@+id/theoPlayerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:googleIma="true"
        app:layout_behavior="@string/appbar_scrolling_view_behavior" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```


## Injecting VAST Ads

Ads definitions are added to `SourceDescription` object. For purposes of this guide it is assumed
that `SourceDescription` is already defined and used to configure THEOplayer source. Please check
[PlayerActivity.java] source to see how the `SourceDescription` object is defined. More information
about that can be found in [THEOplayer How To's - THEOplayer Android SDK Integration] guide.

### Linear Pre-Roll Ad

To inject linear ad, create **`GoogleImaAdDescription`** object by passing to it linear
ad VAST manifest. To make it pre-roll ad please put **`"start"`** as **`timeOffset`** value for
created `GoogleImaAdDescription` object. To simplify `GoogleImaAdDescription` object creation we are
going to use `GoogleImaAdDescription.Builder` method `googleImaAdDescription`:

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void configureTHEOplayer() {
        // ...

        sourceDescription.ads(
                // Inserting linear pre-roll ad defined with VAST standard.
                GoogleImaAdDescription.Builder
                        .googleImaAdDescription(getString(R.string.defaultVastLinearPreRollAdUrl))
                        .timeOffset("start")
                        .build()
        );

        // ...
    }
}
```

### Non-linear Pre-Roll Ad

Non-linear pre-roll ad is defined in the same way as linear pre-roll ad with the exception that given
VAST manifest must describe non-linear ad:

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void configureTHEOplayer() {
        // ...

        sourceDescription.ads(
                // Inserting nonlinear ad defined with VAST standard.
                GoogleImaAdDescription.Builder
                        .googleImaAdDescription(getString(R.string.defaultVastNonLinearAdUrl))
                        .timeOffset("start")
                        .build()
        );

        // ...
    }
}
```

### Skippable Linear Mid-Roll Ad

Linear mid-roll ad is defined in the similar way as linear pre-roll ad with the exception that given
**`timeOffset`** value points to exact time (in seconds) when ad has to be played. In this example
given ad URL defines that ad is skippable:

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void configureTHEOplayer() {
        // ...

        sourceDescription.ads(
                // Inserting skippable linear mid-roll (15s) ad defined with VAST standard.
                GoogleImaAdDescription.Builder
                        .googleImaAdDescription(getString(R.string.defaultVastLinearMidRollAdUrl))
                        .timeOffset("15")
                        .build()
        );

        // ...
    }
}
```


## Injecting VPAID Ads

Ads described with VPAID manifest can be defined the same way as ads described with VAST manifest as
shown in previous section:

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void configureTHEOplayer() {
        // ...

        sourceDescription.ads(
                // Inserting mid-roll (30s) ad defined with VPAID standard.
                GoogleImaAdDescription.Builder
                        .googleImaAdDescription(getString(R.string.defaultVpaidAdUrl))
                        .timeOffset("30")
                        .build()
        );

        // ...
    }
}
```


## Injecting VMAP Ads

Ads described with VMAP manifest can be defined the same way as ads described with VAST manifest as
shown in previous section. VMAP is a playlist of ads and can interfere with added VAST ads. In this
example additional flag `R.bool.loadVmapAds` is created to distinguish between VAST and VMAP ads.
Please change it's value to `true` in [values.xml] to inject ads described with VMAP manifest.

```xml
<resources>
    <!-- ... -->
    <bool name="loadVmapAds">true</bool>
</resources>
```

Having that, define **`GoogleImaAdDescription`** object that keep information about ads defined with
VMAP manifest:

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void configureTHEOplayer() {
        // ...

        // VMAP standard defines ads playlist and contains ads time offset definitions. To avoid
        // overlapping, VMAP ads are defined separately.
        if (getResources().getBoolean(R.bool.loadVmapAds)) {
            sourceDescription.ads(
                    // Inserting linear pre-roll, mid-roll (15s) and post-roll ads defined with VMAP standard.
                    GoogleImaAdDescription.Builder
                            .googleImaAdDescription(getString(R.string.defaultVmapAdUrl))
                            .build()
            );
        } else {
            // VAST ads definitions from previous section
        }

        // ...
    }
}
```


## Listening to Ad Related Events

In addition to that, specify few event listeners to get better view of actual ads playback state:

```java
public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    // ...

    private void configureTHEOplayer() {
        // ...

        // Adding listeners to THEOplayer basic ad events.
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_BEGIN,
            event -> Log.i(TAG, "Event: AD_BEGIN, ad=" + event.getAd()));
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_END,
            event -> Log.i(TAG, "Event: AD_END, ad=" + event.getAd()));
        theoPlayer.getAds().addEventListener(AdsEventTypes.AD_ERROR,
            event -> Log.i(TAG, "Event: AD_ERROR, error=" + event.getError()));
    }
}
```


## Summary

This guide covered ways of inserting ads to the configured stream using the Google IMA ads module.

For more guides about THEOplayer SDK API usage and tips&tricks please visit [THEO Docs] portal.


[//]: # (Sections reference)
[Enabling Google IMA Integration]: #enabling-google-ima-integration
[Injecting VAST Ads]: #injecting-vast-ads
[Linear Pre-Roll Ad]: #linear-pre-roll-ad
[Non-linear Pre-Roll Ad]: #non-linear-pre-roll-ad
[Skippable Linear Mid-Roll Ad]: #skippable-linear-mid-roll-ad
[Injecting VPAID Ads]: #injecting-vpaid-ads
[Injecting VMAP Ads]: #injecting-vmap-ads
[Listening to Ad Related Events]: #listening-to-ad-related-events
[Summary]: #summary

[//]: # (Links and Guides reference)
[THEO Google IMA]: ../..
[THEO Docs]: https://docs.portal.theoplayer.com/
[THEOplayer How To's - THEOplayer Android SDK Integration]: ../../../Basic-Playback/guides/howto-theoplayer-android-sdk-integration/README.md
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing
[Google IMA]: https://developers.google.com/interactive-media-ads/

[//]: # (Project files reference)
[PlayerActivity.java]: ../../app/src/main/java/com/theoplayer/sample/ads/googleima/PlayerActivity.java
[activity_player.xml]: ../../app/src/main/res/layout/activity_player.xml
[values.xml]: ../../app/src/main/res/values/values.xml
