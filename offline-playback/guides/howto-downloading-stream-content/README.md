# THEOplayer How To's - Downloading Stream Content

This guide is going to show how to use THEOplayer's Cache API to download protected and unprotected
stream content for offline playback.

To obtain THEOplayer Android SDK with Caching and ExoPlayer features enabled please visit
[Get Started with THEOplayer].

Presented code snippets are taken from [THEO Offline Playback] reference app. Please note that in
this app all URLs are defined as an Android resource, but they can be inlined as well. Please check
[values.xml] file for URLs definition.


## Table of Contents

  * [Introduction]
  * [Getting Cache Instance]
  * [Initiating Download]
  * [Starting/Resuming Download]
  * [Pausing Download]
  * [Canceling Download]
  * [Removing Downloaded Content]
  * [Playing Downloaded Content]
  * [Inspecting Ongoing Downloads]
  * [Inspecting Download Completion Rate]
  * [Downloading DRM Stream and Renewing DRM License]
  * [Summary]


## Introduction

Cache API is using [CachingTask] to handle downloading stream content. Such `CachingTask` can have
following status values describing actual state of downloaded content:

  * `IDLE` - the task has been created, but content is not downloaded at the moment.
  * `LOADING` - the task is currently downloading the content.
  * `DONE` - the task has finished downloading all content.
  * `ERROR` - the task has encountered an error while downloading or evicting content.
  * `EVICTED` - all data associated with the task has been removed because the task expired or
    the user invoked the remove method.

Code samples below are using `OfflineSource` object that helps to manage the download of a defined
stream source. It keeps reference to the associated `CachingTask` (if any) and allows to act on
it as well as tracking download state and progress. See [OfflineSource.java] for full implementation.

Additionally all used `SourceDescription` object definitions are created in `SourceDescriptionRepository`
helper and can be accessed from there by source URL. See [SourceDescriptionRepository.java] for full
implementation.


## Getting Cache Instance

To be able to download stream content and have THEO's Cache instance working correctly the first
thing to do is to include the [ExoPlayer] library under the `dependencies` node in the
[app-level build.gradle] file:

```groovy
dependencies {
    // ...

    implementation 'com.google.android.exoplayer:exoplayer:2.6.1'
}
```

After saving changes please select **File > Sync Project with Gradle Files** menu item to synchronize
project state.

Having that, THEO's `Cache` instance can be accessed as shown in [OfflineActivity.java]:

```java
public class OfflineActivity extends AppCompatActivity {

    private Cache theoCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ...

        // Gathering THEO objects references.
        theoCache = THEOplayerGlobal.getSharedInstance(this).getCache();

        // ...
    }
}
```

Once `Cache` instance has been created, it is possible to check its state changes and if
`CacheStatus.INITIALISED` - search for any existing `CachingTask` (see [OfflineSourceViewModel.java]
or full implementation):

```java
public class OfflineSourceViewModel extends AndroidViewModel {

    private final Cache theoCache;

    // ...

    public OfflineSourceViewModel(@NonNull Application application) {
        super(application);

        this.theoCache = THEOplayerGlobal.getSharedInstance(application).getCache();

        initializeOfflineSources();
        // ...
    }

    private void initializeOfflineSources() {
        // ...

        // Updating offline sources state by corresponding caching tasks if exists.
        // Note that, there can be cases when content is being cached (or is cached), but view was
        // destroyed by system (or by user). After launching app again, offline sources will be
        // updated with those caching tasks and caching progress will be again presented to the user.
        if (theoCache != null) {
            if (theoCache.getStatus() == CacheStatus.INITIALISED) {
                loadExistingCachingTasks();
            } else {
                theoCache.addEventListener(CacheEventTypes.CACHE_STATE_CHANGE,
                        event -> loadExistingCachingTasks());
            }
        }
    }

    private void loadExistingCachingTasks() {
        if (theoCache != null && theoCache.getStatus() == CacheStatus.INITIALISED) {
            for (CachingTask cachingTask : theoCache.getTasks()) {
               // ...
            }
        }
    }
}
```


## Initiating Download

To initiate stream content download create new `CachingTask`, by calling `createTask(...)`
method on THEO's `Cache` instance.

As a parameters, except `SourceDescription`, `CachingParameters` object needs to be passed with
definition of the amount of content to be downloaded (by default `100%`) and the time when
downloaded content became expired (by default `30` minutes since task creation):

```java
public class OfflineActivity extends AppCompatActivity {

    // ...

    private void onStartCachingTaskHandler(OfflineSource offlineSource) {
        // ...

        CachingParameters.Builder cachingParameters = new CachingParameters.Builder();

        // By default whole content is downloaded, but here we are stating that explicitly.
        // An amount of seconds (e.g. "20") or a percentage (e.g. "50%") can be specified
        // to download only part of the content.
        cachingParameters.amount("100%");

        // By default cashing task is evicted after 30 minutes since its creation.
        // Here we want to have it expired after 7 days since creation.
        Calendar in7Days = Calendar.getInstance();
        in7Days.add(Calendar.DAY_OF_MONTH, 7);
        cachingParameters.expirationDate(in7Days.getTime());

        // Getting prepared source description for given source.
        SourceDescription sourceDescription = SourceDescriptionRepository
                .getBySourceUrl(OfflineActivity.this, offlineSource.getSourceUrl());

        if (sourceDescription != null) {
            // Creating caching task for given source and adding appropriate event listeners to it.
            // Newly created caching task does not start downloading automatically.
            offlineSource.setCachingTask(theoCache.createTask(
                    sourceDescription,
                    cachingParameters.build()
            ));
        }

         // ...
    }

    // ...

}
```

Such created task is in the `IDLE`, which means that content is not downloaded at the moment.
Please go to [Starting/Resuming Download] section to get know how to start caching task.


## Starting/Resuming Download

`CachingTask` in an `IDLE` state can be started so stream content will be downloaded. Such state
can be achieved when `CachingTask` was created or paused.

To start downloading simply call `start()` method on the `CachingTask` instance:

```java
public class OfflineActivity extends AppCompatActivity {

    // ...

    private void onStartCachingTaskHandler(OfflineSource offlineSource) {
         // ...

        // Starting caching task, content is being downloaded.
        offlineSource.startCachingTask();
    }

    // ...

}

public class OfflineSource {

    private CachingTask cachingTask;

    // ...

    public void startCachingTask() {
        if (cachingTask != null) {
            // ...
            cachingTask.start();
        }
    }

    // ...

}
```


## Pausing Download

`CachingTask` in a `LOADING` state can be paused so that stream content downloaded will be on hold.
Such state can be achieved when `CachingTask` was started.

To start downloading simply call `pause()` method on the `CachingTask` instance:

```java
public class OfflineActivity extends AppCompatActivity {

    // ...

    private void onPauseCachingTaskHandler(OfflineSource offlineSource) {
        offlineSource.pauseCachingTask();
    }

    // ...

}

public class OfflineSource {

    private CachingTask cachingTask;

    // ...

    public void pauseCachingTask() {
        if (cachingTask != null) {
            // ...
            cachingTask.pause();
        }
    }

    // ...

}
```


## Canceling Download

`CachingTask` in a `LOADING` state can be cancelled. That means that `CachingTask` will be paused
and then any downloaded content will be removed. Please got to [Removing Downloaded Content] section
for more details about removing downloaded content.

It's good to ask user first if he/she really want to cancel ongoing download:

```java
public class OfflineActivity extends AppCompatActivity {

    // ...

    private void onRemoveCachingTaskHandler(OfflineSource offlineSource) {
        if (CachingTaskStatus.LOADING == offlineSource.getCachingTaskStatus().getValue()) {
            // Caching task to be removed is right now downloading content.
            // Asking user first if he/she really wants to cancel it.
            new AlertDialog.Builder(this)
                    .setTitle(offlineSource.getTitle())
                    .setMessage(R.string.cachingTaskCancelQuestion)
                    .setPositiveButton(R.string.yes, (dialog, buttonType) -> offlineSource.removeCachingTask())
                    .setNegativeButton(R.string.no, (dialog, buttonType) -> dialog.dismiss())
                    .show();
        }
        // ...
    }

    // ...

}
```


## Removing Downloaded Content

Any created `CachingTask` can be removed regardless of its state. That means that any downloaded
content (or its part) will be deleted. If download is currently proceeding that it will be paused
first, see [Canceling Download] section for more details.

To remove downloaded stream content simply call `remove()` method on the `CachingTask` instance:

```java
public class OfflineActivity extends AppCompatActivity {

    // ...

    private void onRemoveCachingTaskHandler(OfflineSource offlineSource) {
        // ...
        offlineSource.removeCachingTask();
    }

    // ...

}

public class OfflineSource {

    private CachingTask cachingTask;

    // ...

    public void removeCachingTask() {
        if (cachingTask != null) {
            // ...
            cachingTask.remove();
        }
    }

    // ...

}
```


## Playing Downloaded Content

To play downloaded stream content it's not needed to do anything special. The same `SourceDescription`
defined in `SourceDescriptionRepository` used to create `CachingTask` (see [Initiating Download])
can be used to configure THEOplayer source. THEOplayer automatically checks if there's `CachingTask`
associated with stream URL (used to define `SourceDescription`) and use downloaded stream content
if exists (check [PlayerActivity.java] for full implementation):

```java
public class PlayerActivity extends AppCompatActivity {

    // ...

    private void configureTHEOplayer(String sourceUrl) {
        // ...

        // Creating a SourceDescription that contains the settings to be applied as a new
        // THEOplayer source.
        SourceDescription sourceDescription = SourceDescriptionRepository.getBySourceUrl(this, sourceUrl);

        // Configuring THEOplayer with defined SourceDescription object to be played automatically.
        theoPlayer.setSource(sourceDescription);
        theoPlayer.setAutoplay(true);

        // ...
    }

    // ...
}
```


## Inspecting Ongoing Downloads

To know which `CachingTask` is currently downloading stream content, iterate over all tasks looking
for those in `LOADING` state:

```java
public class OfflineSourceViewModel extends AndroidViewModel {

    private Cache theoCache;

    // ...

    private void loadExistingCachingTasks() {
        if (theoCache != null && theoCache.getStatus() == CacheStatus.INITIALISED) {

            for (CachingTask cachingTask : theoCache.getTasks()) {
                if (cachingTask.getStatus() == CachingTaskStatus.LOADING) {
                    // ...
                }
            }

        }
    }
}
```

To get notified about `CachingTask` state change simply add listener to
`CachingTaskEventTypes.CACHING_TASK_STATE_CHANGE` event and then call `getStatus()` on the
`CachingTask` when fired:

```java
public class OfflineSource {

    private MutableLiveData<CachingTaskStatus> cachingTaskStatus;

    // ...

    public void setCachingTask(CachingTask cachingTask) {
        // ...

        cachingTask.addEventListener(CachingTaskEventTypes.CACHING_TASK_STATE_CHANGE, event -> {
            cachingTaskStatus.setValue(cachingTask.getStatus());
            // ...
        });

        // ...
    }

    // ...
}
```

## Inspecting Download Completion Rate

To get notified about `CachingTask` progress change simply add listener to
`CachingTaskEventTypes.CACHING_TASK_PROGRESS` event and then call `getPercentageCached()` on the
`CachingTask` when fired.

```java
public class OfflineSource {

    private MutableLiveData<Double> cachingTaskProgress;

    // ...

    public void setCachingTask(CachingTask cachingTask) {
        // ...

        cachingTask.addEventListener(CachingTaskEventTypes.CACHING_TASK_PROGRESS, event -> {
            cachingTaskProgress.setValue(cachingTask.getPercentageCached());
            // ...
        });

        // ...
    }

    // ...
}
```

`CachingTask` offers other methods that allow to calculate completion rate basing on cached
size or time:

  * `getBytes()` - returns an estimation of the amount of data expressed in bytes that task will
    download and store. Returns -1 if the estimate is not available yet.
  * `getBytesCached()` - returns an amount of data expressed in bytes that task has downloaded and stored.
  * `getDuration()` - returns an amount of content in seconds that will be available after this task
    finishes. Returns -1 if the duration is not available yet.
  * `getSecondsCached()` - returns an amount of content that has already been cached.


## Downloading DRM Stream and Renewing DRM License

To correctly download protected content it is need to have license that is capable to be downloaded.
Then while defining `KeySystemConfiguration`, mark it as `LicenseType.PERSISTENT`. See sample
protected stream configuration in [SourceDescriptionRepository.java]. Moreover, enable Experimental
Rendering feature by calling `setExperimentalRenderingEnabled(true)` method on `TypedSource`:

```java
public final class SourceDescriptionRepository {

    // ...

    private static SourceDescription getBigBuckBunnySourceDescription(Context context) {
        return SourceDescription.Builder.sourceDescription(
                TypedSource.Builder
                        .typedSource(context.getString(R.string.bigBuckBunnySourceUrl))
                        .drm(
                                DRMConfiguration.Builder.widevineDrm(
                                        // Note that license has to have PERSISTENT type configured
                                        // to be cached and to allow offline playback.
                                        KeySystemConfiguration.Builder
                                                .keySystemConfiguration(context.getString(R.string.bigBuckBunnyLicenseUrl))
                                                .licenseType(LicenseType.PERSISTENT)
                                                .build()
                                ).build()
                        )
                        .setExperimentalRenderingEnabled(true)
                        .build()
        ).build();
    }

    // ...

}
```

Such defined licence can expire after some time and have to be renewed so downloaded protected
stream content could be played again. To do that, call `license()` method on the `CachingTask` to
get `CachingTaskLicense` and call `renew()` on it. Most likely this will have to be done periodically,
so it's worth to create some `Worker` which will do that job (see [OfflineDrmLicenseRenewalWorker.java]):

```java
public class OfflineDrmLicenseRenewalWorker extends Worker {

    // ...

    @NonNull
    @Override
    public Result doWork() {
        Cache theoCache = THEOplayerGlobal.getSharedInstance(getApplicationContext()).getCache();
        if (theoCache != null && theoCache.getStatus() == CacheStatus.INITIALISED) {
            for (CachingTask cachingTask : theoCache.getTasks()) {
                if (cachingTask.getStatus() != CachingTaskStatus.EVICTED) {
                    cachingTask.license().renew();
                }
            }
        }
        return Result.success();
    }

}
```

Next, schedule created worker. In our example we want to renew licenses once a day, see
[OfflineSourceViewModel.java]:

```java
public class OfflineSourceViewModel extends AndroidViewModel {

    // ...

    private void scheduleOfflineDrmLicenseRenewal() {
        // Defining task responsible for renewing DRM license of cached sources.
        // Task is scheduled to be executed once a day, but only if Internet connection is available.
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        PeriodicWorkRequest renewDrmLicenseRequest =
                new PeriodicWorkRequest.Builder(OfflineDrmLicenseRenewalWorker.class, 1, TimeUnit.DAYS)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(getApplication()).enqueue(renewDrmLicenseRequest);
    }

    // ...
}
```


## Summary

This guide covered ways of using THEOplayer's Cache API to download protected and unprotected
stream content for offline playback.

For more guides about THEOplayer SDK API usage and tips&tricks please visit [THEO Docs] portal.


[//]: # (Sections reference)
[Introduction]: #introduction
[Getting Cache Instance]: #getting-cache-instance
[Initiating Download]: #initiating-download
[Starting/Resuming Download]: #startingresuming-download
[Pausing Download]: #pausing-download
[Canceling Download]: #canceling-download
[Removing Downloaded Content]: #removing-downloaded-content
[Playing Downloaded Content]: #playing-downloaded-content
[Inspecting Ongoing Downloads]: #inspecting-ongoing-downloads
[Inspecting Download Completion Rate]: #inspecting-download-completion-rate
[Downloading DRM Stream and Renewing DRM License]: #downloading-drm-stream-and-renewing-drm-license
[Summary]: #summary

[//]: # (Links and Guides reference)
[THEO Offline Playback]: ../..
[THEO Docs]: https://docs.portal.theoplayer.com/
[THEOplayer How To's - THEOplayer Android SDK Integration]: ../../../Basic-Playback/guides/howto-theoplayer-android-sdk-integration/README.md
[Get Started with THEOplayer]: https://www.theoplayer.com/licensing
[ExoPlayer]: https://exoplayer.dev/
[CachingTask]: https://docs.portal.theoplayer.com/docs/api-reference/theoplayer-cachingtask

[//]: # (Project files reference)
[app-level build.gradle]: ../../build.gradle.kts
[PlayerActivity.java]: ../../src/main/java/com/theoplayer/sample/playback/offline/PlayerActivity.java
[OfflineActivity.java]: ../../src/main/java/com/theoplayer/sample/playback/offline/OfflineActivity.java
[OfflineSource.java]: ../../src/main/java/com/theoplayer/sample/playback/offline/OfflineSource.java
[OfflineSourceViewModel.java]: ../../src/main/java/com/theoplayer/sample/playback/offline/OfflineSourceViewModel.java
[OfflineDrmLicenseRenewalWorker.java]: ../../src/main/java/com/theoplayer/sample/playback/offline/OfflineDrmLicenseRenewalWorker.java
[SourceDescriptionRepository.java]: ../../src/main/java/com/theoplayer/sample/playback/offline/SourceDescriptionRepository.java
[activity_player.xml]: ../../src/main/res/layout/activity_player.xml
[values.xml]: ../../src/main/res/values/values.xml
