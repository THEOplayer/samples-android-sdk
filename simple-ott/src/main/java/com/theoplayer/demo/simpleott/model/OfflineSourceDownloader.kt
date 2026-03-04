package com.theoplayer.demo.simpleott.model

import android.content.Context
import android.content.DialogInterface
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import android.widget.Toast
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
import com.theoplayer.demo.simpleott.R
import com.theoplayer.demo.simpleott.network.WiFiNetworkInfo
import com.theoplayer.sample.common.SourceManager
import java.util.Calendar

class OfflineSourceDownloader(
    private val context: Context,
    val wiFiNetworkInfo: WiFiNetworkInfo
) {
    private val theoCache: Cache = THEOplayerGlobal.getSharedInstance(context).cache!!
    val offlineSources: List<OfflineSource>

    init {
        offlineSources = listOf(
            OfflineSource(
                "Big Buck Bunny",
                R.drawable.image_big_buck_bunny,
                SourceManager.BIG_BUCK_BUNNY_HLS
            ),
            OfflineSource(
                "Sintel",
                R.drawable.image_sintel,
                SourceManager.SINTEL_HLS
            ),
            OfflineSource(
                "Cosmos",
                R.drawable.image_caminandes_llama_drama,
                SourceManager.COSMOS_DASH
            )
        )

        // Recovering any existing THEOplayer's cache state.
        if (theoCache.status == CacheStatus.INITIALISED) {
            loadExistingCachingTasks()
        } else {
            theoCache.addEventListener(CacheEventTypes.CACHE_STATE_CHANGE,
                EventListener { event: CacheStateChangeEvent? -> loadExistingCachingTasks() })
        }

        // Observing WiFi network info changes
        wiFiNetworkInfo.downloadOnlyOnWiFi().observe(
            (context as LifecycleOwner)
        ) { downloadOnlyOnWiFi: Boolean ->
            onWiFiNetworkInfoChange(
                downloadOnlyOnWiFi,
                wiFiNetworkInfo.isConnectedToWiFi
            )
        }
        wiFiNetworkInfo.connectedToWiFi().observe(
            (context as LifecycleOwner)
        ) { connectedToWiFi: Boolean ->
            onWiFiNetworkInfoChange(
                wiFiNetworkInfo.isDownloadOnlyOnWiFi,
                connectedToWiFi
            )
        }
    }

    fun startCachingTask(offlineSource: OfflineSource) {
        val cachingTaskStatus = offlineSource.cachingTaskStatus.value
        if (cachingTaskStatus == null || cachingTaskStatus == CachingTaskStatus.EVICTED || cachingTaskStatus == CachingTaskStatus.ERROR) {
            Log.i(TAG, "Creating caching task, title='" + offlineSource.title + "'")
            val cachingParameters = CachingParameters.Builder()
            cachingParameters.amount("100%")

            val in7Days = Calendar.getInstance()
            in7Days.add(Calendar.DAY_OF_MONTH, 7)
            cachingParameters.expirationDate(in7Days.time)

            offlineSource.setCachingTask(
                theoCache.createTask(
                    offlineSource.sourceDescription,
                    cachingParameters.build()
                )
            )
        }

        if (wiFiNetworkInfo.isDownloadOnlyOnWiFi && !wiFiNetworkInfo.isConnectedToWiFi) {
            val toastMessage = SpannableString.valueOf(context.getString(R.string.wifiDisconnectedWarning))
            toastMessage.setSpan(
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0, toastMessage.length, 0
            )
            Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
        } else {
            offlineSource.startCachingTask()
        }
    }

    fun pauseCachingTask(offlineSource: OfflineSource) {
        offlineSource.pauseCachingTask()
    }

    fun removeCachingTask(offlineSource: OfflineSource) {
        if (offlineSource.hasStatus(CachingTaskStatus.DONE)) {
            MaterialAlertDialogBuilder(context)
                .setTitle(offlineSource.title)
                .setMessage(R.string.removeCachingTaskQuestion)
                .setNegativeButton(R.string.no) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                    removeCachingTaskInternal(offlineSource)
                }
                .show()
        } else {
            removeCachingTaskInternal(offlineSource)
        }
    }

    private fun removeCachingTaskInternal(offlineSource: OfflineSource) {
        offlineSource.removeCachingTask()
        for (cachingTask in theoCache.tasks) {
            if (cachingTask.source.sources[0].src == offlineSource.sourceUrl) {
                cachingTask.remove()
            }
        }
    }

    fun removeAllCachingTasks() {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.removeAllCachingTasks)
            .setMessage(R.string.removeAllCachingTasksQuestion)
            .setNegativeButton(R.string.no) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int -> removeAllCachingTasksInternal() }
            .show()
    }

    private fun removeAllCachingTasksInternal() {
        for (offlineSource in offlineSources) {
            offlineSource.removeCachingTask()
        }
        for (cachingTask in theoCache.tasks) {
            cachingTask.remove()
        }
    }

    private fun loadExistingCachingTasks() {
        if (theoCache.status == CacheStatus.INITIALISED) {
            Log.i(TAG, "Event: CACHE_INITIALISED, found " + theoCache.tasks.length() + " tasks...")
            for (cachingTask in theoCache.tasks) {
                val cachingTaskSourceUrl = cachingTask.source.sources[0].src
                for (offlineSource in offlineSources) {
                    if (offlineSource.sourceUrl == cachingTaskSourceUrl) {
                        Log.i(TAG, "Setting caching task for: $cachingTaskSourceUrl")
                        offlineSource.setCachingTask(cachingTask)
                        break
                    }
                }
            }
        }
    }

    private fun onWiFiNetworkInfoChange(downloadOnlyOnWiFi: Boolean, connectedToWiFi: Boolean) {
        if (downloadOnlyOnWiFi) {
            for (offlineSource in offlineSources) {
                if (connectedToWiFi) {
                    if (offlineSource.hasStatus(CachingTaskStatus.ERROR) || offlineSource.hasStatus(CachingTaskStatus.IDLE)) {
                        startCachingTask(offlineSource)
                    }
                } else {
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
