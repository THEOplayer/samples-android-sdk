package com.theoplayer.sample.playback.offline

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.cache.Cache
import com.theoplayer.android.api.cache.CacheStatus
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.cache.CacheEventTypes
import com.theoplayer.android.api.event.cache.CacheStateChangeEvent
import java.util.*
import java.util.concurrent.TimeUnit

class OfflineSourceViewModel(application: Application) : AndroidViewModel(application) {
    private val theoCache: Cache?

    // Creating list of sources that can be cached and watched offline.
    var offlineSources: List<OfflineSource>? = listOf(
        OfflineSource(
            getApplication<Application>().getString(R.string.bigBuckBunnyTitle),
            getApplication<Application>().getString(R.string.bigBuckBunnyPoster),
            getApplication<Application>().getString(R.string.bigBuckBunnySourceUrl)
        ),
        OfflineSource(
            getApplication<Application>().getString(R.string.sintelTitle),
            getApplication<Application>().getString(R.string.sintelPoster),
            getApplication<Application>().getString(R.string.sintelSourceUrl)
        ),
        OfflineSource(
            getApplication<Application>().getString(R.string.tearsOfStealTitle),
            getApplication<Application>().getString(R.string.tearsOfStealPoster),
            getApplication<Application>().getString(R.string.tearsOfStealSourceUrl)
        ),
        OfflineSource(
            getApplication<Application>().getString(R.string.bipBopTitle),
            getApplication<Application>().getString(R.string.bipBopPoster),
            getApplication<Application>().getString(R.string.bipBopSourceUrl)
        )
    )

    init {
        theoCache = THEOplayerGlobal.getSharedInstance(application).cache
        initializeOfflineSources()
        scheduleOfflineDrmLicenseRenewal()
    }

    private fun scheduleOfflineDrmLicenseRenewal() {
        // Defining task responsible for renewing DRM license of cached sources.
        // Task is scheduled to be executed once a day, but only if Internet connection is available.
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()
        val renewDrmLicenseRequest: PeriodicWorkRequest =
            PeriodicWorkRequest.Builder(OfflineDrmLicenseRenewalWorker::class.java, 1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
        WorkManager.getInstance(getApplication()).enqueue(renewDrmLicenseRequest)
    }

    private fun initializeOfflineSources() {

        // Updating offline sources state by corresponding caching tasks if exists.
        // Note that, there can be cases when content is being cached (or is cached), but view was
        // destroyed by system (or by user). After launching app again, offline sources will be
        // updated with those caching tasks and caching progress will be again presented to the user.
        if (theoCache != null) {
            if (theoCache.status == CacheStatus.INITIALISED) {
                loadExistingCachingTasks()
            } else {
                theoCache.addEventListener(CacheEventTypes.CACHE_STATE_CHANGE,
                    EventListener { loadExistingCachingTasks() })
            }
        }
    }

    private fun loadExistingCachingTasks() {
        if (theoCache != null && theoCache.status == CacheStatus.INITIALISED) {
            Log.i(TAG, "Event: CACHE_INITIALISED, found " + theoCache.tasks.length() + " tasks...")
            for (cachingTask in theoCache.tasks) {
                val cachingTaskSourceUrl = cachingTask.source.sources[0].src
                for (offlineSource in offlineSources!!) {
                    if (offlineSource.sourceUrl == cachingTaskSourceUrl) {
                        offlineSource.setCachingTask(cachingTask)
                        break
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = OfflineSourceViewModel::class.java.simpleName
    }
}