package com.theoplayer.sample.playback.offline;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.theoplayer.android.api.THEOplayerGlobal;
import com.theoplayer.android.api.cache.Cache;
import com.theoplayer.android.api.cache.CacheStatus;
import com.theoplayer.android.api.cache.CachingTask;
import com.theoplayer.android.api.event.cache.CacheEventTypes;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OfflineSourceViewModel extends AndroidViewModel {

    private static final String TAG = OfflineSourceViewModel.class.getSimpleName();

    private Cache theoCache;
    private List<OfflineSource> offlineSources;

    public OfflineSourceViewModel(@NonNull Application application) {
        super(application);

        this.theoCache = THEOplayerGlobal.getSharedInstance(application).getCache();

        initializeOfflineSources();
        scheduleOfflineDrmLicenseRenewal();
    }

    public List<OfflineSource> getOfflineSources() {
        return offlineSources;
    }

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

    private void initializeOfflineSources() {
        // Creating list of sources that can be cached and watched offline.
        offlineSources = Arrays.asList(
                new OfflineSource(
                        getApplication().getString(R.string.bigBuckBunnyTitle),
                        getApplication().getString(R.string.bigBuckBunnyPoster),
                        getApplication().getString(R.string.bigBuckBunnySourceUrl)
                ),
                new OfflineSource(
                        getApplication().getString(R.string.sintelTitle),
                        getApplication().getString(R.string.sintelPoster),
                        getApplication().getString(R.string.sintelSourceUrl)
                ),
                new OfflineSource(
                        getApplication().getString(R.string.tearsOfStealTitle),
                        getApplication().getString(R.string.tearsOfStealPoster),
                        getApplication().getString(R.string.tearsOfStealSourceUrl)
                ),
                new OfflineSource(
                        getApplication().getString(R.string.elephantsDreamTitle),
                        getApplication().getString(R.string.elephantsDreamPoster),
                        getApplication().getString(R.string.elephantsDreamSourceUrl)
                )
        );

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
            Log.i(TAG, "Event: CACHE_INITIALISED, found " + theoCache.getTasks().length() + " tasks...");

            for (CachingTask cachingTask : theoCache.getTasks()) {
                String cachingTaskSourceUrl = cachingTask.getSource().getSources().get(0).getSrc();

                for (OfflineSource offlineSource : offlineSources) {
                    if (offlineSource.getSourceUrl().equals(cachingTaskSourceUrl)) {
                        offlineSource.setCachingTask(cachingTask);
                        break;
                    }
                }
            }
        }
    }

}
