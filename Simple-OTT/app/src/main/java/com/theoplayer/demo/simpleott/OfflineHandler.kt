package com.theoplayer.demo.simpleott

import android.content.Context
import android.content.DialogInterface
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.theoplayer.android.api.cache.Cache
import com.theoplayer.android.api.cache.CacheStatus
import com.theoplayer.android.api.cache.CachingParameters
import com.theoplayer.android.api.cache.CachingTaskStatus
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.cache.CacheEventTypes
import com.theoplayer.android.api.event.cache.CacheStateChangeEvent
import com.theoplayer.demo.simpleott.datamodel.AssetItem
import com.theoplayer.demo.simpleott.datamodel.OfflineSource
import java.util.*

class OfflineHandler(
    private val context: Context,
    private val theoCache: Cache?,
    vods: Array<AssetItem>,
    onlyOnWifi: LiveData<Boolean>,
    wifiConnected: LiveData<Boolean>
) {
    private val offlineSources: ArrayList<OfflineSource>
    private val onlyOnWifiSetting: LiveData<Boolean>
    private val wifiConnected: LiveData<Boolean>

    init {
        offlineSources = ArrayList()
        onlyOnWifiSetting = onlyOnWifi
        this.wifiConnected = wifiConnected
        onlyOnWifiSetting.observe((context as LifecycleOwner)) { onlyOnWifiSetting: Boolean ->
            pauseWhenOnWifiSettingChanged(
                onlyOnWifiSetting
            )
        }
        this.wifiConnected.observe((context as LifecycleOwner)) { isWifiConnected: Boolean ->
            pauseOrResumeWhenOnWifiStatusChanged(
                isWifiConnected
            )
        }
        for (vod in vods) {
            offlineSources.add(OfflineSource(vod))
        }
    }

    fun init() {
        // initializing caching tasks from THEO cache
        if (theoCache != null) {
            if (theoCache.status == CacheStatus.INITIALISED) {
                loadExistingCachingTasks()
            } else {
                theoCache.addEventListener(CacheEventTypes.CACHE_STATE_CHANGE,
                    EventListener { event: CacheStateChangeEvent? -> loadExistingCachingTasks() })
            }
        }
        if (theoCache != null) {
            loadExistingCachingTasks()
        }
    }

    private fun loadExistingCachingTasks() {
        if (theoCache != null) {
            Log.i(TAG, "Event: CACHE_INITIALISED, found " + theoCache.tasks.length() + " tasks...")
            for (cachingTask in theoCache.tasks) {
                val cachingTaskSourceUrl = cachingTask.source.sources[0].src
                for (offlineSource in offlineSources) {
                    if (offlineSource.videoSource == cachingTaskSourceUrl) {
                        Log.i(TAG, "Setting caching task for: $cachingTaskSourceUrl")
                        offlineSource.setCachingTask(cachingTask)
                        break
                    }
                }
            }
        }
    }

    fun deleteAllCachedItems() {
        for (offlineSource in offlineSources) {
            offlineSource.removeCachingTask()
        }
    }

    fun startCachingTaskHandler(offlineSource: OfflineSource) {
        if (theoCache != null) {
            val cachingTaskStatus = offlineSource.cachingTaskStatusLiveData.value
            if (cachingTaskStatus == null || cachingTaskStatus == CachingTaskStatus.EVICTED || cachingTaskStatus == CachingTaskStatus.ERROR) {
                Log.i(TAG, "Creating caching task, title='" + offlineSource.name + "'")
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
                val sourceDescription =
                    SourceDescriptionUtil.getBySourceUrl(offlineSource.videoSource)
                if (sourceDescription != null) {
                    // Creating caching task for given source and adding appropriate event listeners to it.
                    // Newly created caching task does not start downloading automatically.
                    offlineSource.setCachingTask(
                        theoCache.createTask(
                            sourceDescription,
                            cachingParameters.build()
                        )
                    )
                }
            }

            // Starting caching task, content is being downloaded.
            startOfflineSourceUnderConditions(offlineSource)
        } else {
            // Being here means that caching is not supported.
            val toastMessage =
                SpannableString.valueOf(context.getString(R.string.cachingNotSupported))
            toastMessage.setSpan(
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0,
                toastMessage.length,
                0
            )
            Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun startOfflineSourceUnderConditions(offlineSource: OfflineSource) {
        // If "only on wifi" setting is set to true, then check if WiFi connection is available
        // If it isn't then inform the user that download will not start
        if (onlyOnWifiSetting.value === java.lang.Boolean.TRUE && wifiConnected.value === java.lang.Boolean.FALSE) {
            val toastMessage = SpannableString.valueOf(context.getString(R.string.notOnWifi))
            toastMessage.setSpan(
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0,
                toastMessage.length,
                0
            )
            Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
        } else {
            offlineSource.startCachingTask()
        }
    }

    private fun pauseWhenOnWifiSettingChanged(onlyOnWifiSetting: Boolean) {
        // When downloading on mobile data (not on wifi) and the user changes the "Only on WiFi" setting, pause the download
        if (onlyOnWifiSetting && wifiConnected.value === java.lang.Boolean.FALSE) {
            for (offlineSource in filterOfflineSourcesByState(CachingTaskStatus.LOADING)) {
                offlineSource.pauseCachingTask()
            }
        }
    }

    private fun pauseOrResumeWhenOnWifiStatusChanged(isWifiConnected: Boolean) {
        // Resume the download when the WiFi connection is restored
        // Mind that some tasks may end up with "ERROR" status after turning off WiFi connection. Those need to be handled as well
        if (onlyOnWifiSetting.value === java.lang.Boolean.TRUE) {
            if (!isWifiConnected) {
                for (offlineSource in filterOfflineSourcesByState(CachingTaskStatus.LOADING)) {
                    offlineSource.pauseCachingTask()
                }
            } else {
                // Handling "ERROR"ed tasks
                for (offlineSource in filterOfflineSourcesByState(CachingTaskStatus.ERROR)) {
                    startCachingTaskHandler(offlineSource)
                }
                for (offlineSource in filterOfflineSourcesByState(CachingTaskStatus.IDLE)) {
                    offlineSource.startCachingTask()
                }
            }
        }
    }

    private fun filterOfflineSourcesByState(vararg status: CachingTaskStatus): ArrayList<OfflineSource> {
        val result = ArrayList<OfflineSource>()
        for (of in offlineSources) {
            if (listOf<CachingTaskStatus?>(*status).indexOf(of.getCachingTaskStatus()) > -1) {
                result.add(of)
            }
        }
        return result
    }

    fun removeCachingTaskHandler(offlineSource: OfflineSource) {
        // Before deleting a task, ask the user for confirmation
        if (CachingTaskStatus.DONE == offlineSource.cachingTaskStatusLiveData.value) {
            AlertDialog.Builder(context)
                .setTitle(offlineSource.name)
                .setMessage(R.string.cachingTaskCancelQuestion)
                .setPositiveButton(R.string.yes) { dialog: DialogInterface?, buttonType: Int -> offlineSource.removeCachingTask() }
                .setNegativeButton(R.string.no) { dialog: DialogInterface, buttonType: Int -> dialog.dismiss() }
                .show()
        } else {
            offlineSource.removeCachingTask()
        }
    }

    fun getOfflineSources(): Array<OfflineSource> {
        return offlineSources.toTypedArray()
    }

    companion object {
        private val TAG = OfflineHandler::class.java.simpleName
    }
}