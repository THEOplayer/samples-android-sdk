package com.theoplayer.demo.simpleott.model

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.cache.Cache
import com.theoplayer.android.api.cache.CacheStatus
import com.theoplayer.android.api.cache.CachingParameters
import com.theoplayer.android.api.cache.CachingTaskStatus
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.cache.CacheEventTypes
import com.theoplayer.android.api.event.cache.CacheStateChangeEvent
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.demo.simpleott.R
import com.theoplayer.demo.simpleott.ToastUtils
import com.theoplayer.demo.simpleott.network.WiFiNetworkInfo
import java.util.*

/**
 * This class allows to download content of a stream for offline playback.
 *
 *
 * It uses THEOplayer's `Cache` instance.
 *
 *
 * Download possibility depends on WiFi connectivity state. User can decide to allow download only
 * when WiFi is connected.
 */
class OfflineSourceDownloader(
    private val context: Context,
    streamSourceRepository: StreamSourceRepository,
    wiFiNetworkInfo: WiFiNetworkInfo
) {
    private val theoCache: Cache?
    private val wiFiNetworkInfo: WiFiNetworkInfo
    private val offlineSources: MutableList<OfflineSource>

    init {
        theoCache = THEOplayerGlobal.getSharedInstance(context).cache
        this.wiFiNetworkInfo = wiFiNetworkInfo
        offlineSources = ArrayList()

        // Wrapping StreamSource instances with OfflineSource to allow downloading content.
        for (streamSource in streamSourceRepository.offlineStreamSources) {
            offlineSources.add(OfflineSource(streamSource))
        }

        // Recovering any existing THEOplayer's cache state.
        if (theoCache != null) {
            if (theoCache.status == CacheStatus.INITIALISED) {
                loadExistingCachingTasks()
            } else {
                theoCache.addEventListener(CacheEventTypes.CACHE_STATE_CHANGE,
                    EventListener { event: CacheStateChangeEvent? -> loadExistingCachingTasks() })
            }
        }

        // Observing WiFi network info changes
        this.wiFiNetworkInfo.downloadOnlyOnWiFi().observe(
            (context as LifecycleOwner)
        ) { downloadOnlyOnWiFi: Boolean ->
            onWiFiNetworkInfoChange(
                downloadOnlyOnWiFi,
                wiFiNetworkInfo.isConnectedToWiFi
            )
        }
        this.wiFiNetworkInfo.connectedToWiFi().observe(
            (context as LifecycleOwner)
        ) { connectedToWiFi: Boolean ->
            onWiFiNetworkInfoChange(
                wiFiNetworkInfo.isDownloadOnlyOnWiFi,
                connectedToWiFi
            )
        }
    }

    /**
     * Returns offline stream sources wrapped in `OfflineSource` object.
     *
     * @return - offline sources.
     */
    fun getOfflineSources(): List<OfflineSource> {
        return offlineSources
    }

    /**
     * Starts caching task to download stream content for given `offlineSource`.
     *
     *
     * If caching task does not exist yet if is created. New caching task is configured to download
     * whole stream content and to expire after 7 days of its creation.
     *
     * @param offlineSource - offline source for which content has to be downloaded.
     */
    fun startCachingTask(offlineSource: OfflineSource) {
        if (theoCache != null) {
            val cachingTaskStatus = offlineSource.cachingTaskStatus.value
            if (cachingTaskStatus == null || cachingTaskStatus == CachingTaskStatus.EVICTED || cachingTaskStatus == CachingTaskStatus.ERROR) {
                Log.i(TAG, "Creating caching task, title='" + offlineSource.title + "'")
                val cachingParameters = CachingParameters.Builder()

                // By default whole content is downloaded, but here we are stating that explicitly.
                // An amount of seconds (e.g. "20") or a percentage (e.g. "50%") can be specified
                // to download only part of the content.
                cachingParameters.amount("100%")

                // By default cashing task is evicted after 30 minutes since its creation.
                // Here we want to have it expired after 7 days since creation.
                val in7Days = Calendar.getInstance()
                in7Days.add(Calendar.DAY_OF_MONTH, 7)
                cachingParameters.expirationDate(in7Days.time)

                // Getting prepared source description for given source.
                val sourceDescription = SourceDescription.Builder.sourceDescription(
                    TypedSource.Builder.typedSource(offlineSource.source).build()
                ).build()
                if (sourceDescription != null) {
                    // Creating caching task for given source and adding appropriate event listeners to it.
                    // Newly created caching task does not start downloading automatically.
                    offlineSource.assignCachingTask(
                        theoCache.createTask(
                            sourceDescription,
                            cachingParameters.build()
                        )
                    )
                }
            }

            // If "Download only on wifi" is set to true, then check if WiFi connection is available
            // If it isn't then inform the user that download will not start
            if (wiFiNetworkInfo.isDownloadOnlyOnWiFi && !wiFiNetworkInfo.isConnectedToWiFi) {
                ToastUtils.toastMessage(context, R.string.wifiDisconnectedWarning)
            } else {
                // Starting caching task, content is being downloaded.
                offlineSource.startCachingTask()
            }
        } else {
            // Being here means that caching is not supported.
            ToastUtils.toastMessage(context, R.string.cachingNotSupported)
        }
    }

    /**
     * Pauses caching task so stream content download for given `offlineSource` is on-hold.
     *
     * @param offlineSource - offline source for which to pause content download.
     */
    fun pauseCachingTask(offlineSource: OfflineSource) {
        offlineSource.pauseCachingTask()
    }

    /**
     * Removes caching task so download stream content for given `offlineSource` is purged.
     *
     * @param offlineSource - offline source for which to remove downloaded content.
     */
    fun removeCachingTask(offlineSource: OfflineSource) {
        // Before deleting a task, ask the user for confirmation
        if (offlineSource.hasStatus(CachingTaskStatus.DONE)) {
            MaterialAlertDialogBuilder(context)
                .setTitle(offlineSource.title)
                .setMessage(R.string.removeCachingTaskQuestion)
                .setNegativeButton(R.string.no) { dialog: DialogInterface, buttonType: Int -> dialog.dismiss() }
                .setPositiveButton(R.string.yes) { dialog: DialogInterface?, buttonType: Int ->
                    removeCachingTaskInternal(
                        offlineSource
                    )
                }
                .show()
        } else {
            removeCachingTaskInternal(offlineSource)
        }
    }

    private fun removeCachingTaskInternal(offlineSource: OfflineSource) {
        offlineSource.removeCachingTask()
        if (theoCache != null) {
            // In case there exist some detached caching tasks for the same source
            for (cachingTask in theoCache.tasks) {
                if (cachingTask.source.sources[0].src == offlineSource.source) {
                    cachingTask.remove()
                }
            }
        }
    }

    /**
     * Removes all existing caching tasks. Any downloaded content of all `OfflineSource`
     * instances is purged.
     */
    fun removeAllCachingTasks() {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.removeAllCachingTasks)
            .setMessage(R.string.removeAllCachingTasksQuestion)
            .setNegativeButton(R.string.no) { dialog: DialogInterface, buttonType: Int -> dialog.dismiss() }
            .setPositiveButton(R.string.yes) { dialog: DialogInterface?, buttonType: Int -> removeAllCachingTasksInternal() }
            .show()
    }

    private fun removeAllCachingTasksInternal() {
        for (offlineSource in offlineSources) {
            offlineSource.removeCachingTask()
        }
        // In case there exist some detached caching tasks
        if (theoCache != null) {
            for (cachingTask in theoCache.tasks) {
                cachingTask.remove()
            }
        }
    }

    /**
     * Updates offline sources by corresponding caching tasks if exists.
     *
     *
     * Note that, there can be cases when content is being cached (or is cached), but app was
     * destroyed by system (or by user). After launching app again, offline sources will be
     * updated with those caching tasks and caching progress will be again presented to the user.
     */
    private fun loadExistingCachingTasks() {
        if (theoCache != null && theoCache.status == CacheStatus.INITIALISED) {
            Log.i(TAG, "Event: CACHE_INITIALISED, found " + theoCache.tasks.length() + " tasks...")
            for (cachingTask in theoCache.tasks) {
                val cachingTaskSourceUrl = cachingTask.source.sources[0].src
                for (offlineSource in offlineSources) {
                    if (offlineSource.source == cachingTaskSourceUrl) {
                        Log.i(TAG, "Setting caching task for: $cachingTaskSourceUrl")
                        offlineSource.assignCachingTask(cachingTask)
                        break
                    }
                }
            }
        }
    }

    /**
     * Manages existing caching task according to current WiFi connectivity state.
     *
     *
     * When user wants to download only when WiFi is connected then when WiFi connection is lost
     * all ongoing downloads are paused and when WiFi connection is restored then all halted
     * caching tasks are started again.
     *
     * @param downloadOnlyOnWiFi - `true` if download is allowed when WiFi is connected;
     * `false` otherwise.
     * @param connectedToWiFi    - `true` if WiFi is connected; `false` otherwise.
     */
    private fun onWiFiNetworkInfoChange(downloadOnlyOnWiFi: Boolean, connectedToWiFi: Boolean) {
        if (downloadOnlyOnWiFi) {
            for (offlineSource in offlineSources) {
                if (connectedToWiFi) {
                    // Start all halted downloads, some tasks may end up with "ERROR" status after turning off WiFi connection
                    if (offlineSource.hasStatus(CachingTaskStatus.ERROR) || offlineSource.hasStatus(
                            CachingTaskStatus.IDLE
                        )
                    ) {
                        startCachingTask(offlineSource)
                    }
                } else {
                    // Pause all ongoing downloads
                    if (offlineSource.hasStatus(CachingTaskStatus.LOADING)) {
                        pauseCachingTask(offlineSource)
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = OfflineSourceDownloader::class.java.simpleName
    }
}