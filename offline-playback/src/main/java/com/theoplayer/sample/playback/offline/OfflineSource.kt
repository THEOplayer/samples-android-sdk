package com.theoplayer.sample.playback.offline

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.theoplayer.android.api.cache.CachingTask
import com.theoplayer.android.api.cache.CachingTaskStatus
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.cache.task.CachingTaskEventTypes
import com.theoplayer.android.api.event.cache.task.CachingTaskProgressEvent
import com.theoplayer.android.api.event.cache.task.CachingTaskStateChangeEvent
import com.theoplayer.android.api.source.SourceDescription

class OfflineSource internal constructor(
    val title: String,
    val poster: String,
    val sourceDescription: SourceDescription
) {
    val sourceUrl: String get() = sourceDescription.sources[0].src
    private var cachingTask: CachingTask? = null
    val cachingTaskStatus: MutableLiveData<CachingTaskStatus?> = MutableLiveData()
    val isStateUpToDate: MutableLiveData<Boolean?> = MutableLiveData()
    val cachingTaskProgress: MutableLiveData<Double?> = MutableLiveData()
    val cachingTaskSizeText: MutableLiveData<String?> = MutableLiveData()

    private var progressEventCount = 0

    fun setCachingTask(cachingTask: CachingTask?) {
        this.cachingTask = cachingTask
        isStateUpToDate.value = false
        progressEventCount = 0
        cachingTaskStatus.value = cachingTask?.status ?: CachingTaskStatus.EVICTED
        cachingTaskProgress.value = cachingTask?.percentageCached ?: 0.0
        updateSizeText(cachingTask)
        if (cachingTask != null) {
            cachingTask.addEventListener(
                CachingTaskEventTypes.CACHING_TASK_PROGRESS,
                EventListener { event: CachingTaskProgressEvent? ->
                    Log.i(
                        TAG,
                        "Event: CACHING_TASK_PROGRESS, title='" + title + "', progress=" + cachingTask.percentageCached + "', bytes=" + cachingTask.bytes,
                    )
                    cachingTaskProgress.value = cachingTask.percentageCached
                    progressEventCount++
                    if (progressEventCount % 5 == 0) {
                        updateSizeText(cachingTask)
                    }
                })
            cachingTask.addEventListener(
                CachingTaskEventTypes.CACHING_TASK_STATE_CHANGE,
                EventListener { event: CachingTaskStateChangeEvent? ->
                    Log.i(
                        TAG,
                        "Event: CACHING_TASK_STATE_CHANGE, title='" + title + "', status=" + cachingTask.status + ", progress=" + cachingTask.percentageCached
                    )
                    cachingTaskStatus.value = cachingTask.status
                    cachingTaskProgress.value = cachingTask.percentageCached
                    updateSizeText(cachingTask)
                    isStateUpToDate.value = true
                })
        }
        isStateUpToDate.value = true
    }

    private fun updateSizeText(cachingTask: CachingTask?) {
        if (cachingTask == null || cachingTask.status == CachingTaskStatus.EVICTED) {
            cachingTaskSizeText.value = null
            return
        }
        val cached = cachingTask.bytesCached
        if (cached > 0) {
            cachingTaskSizeText.value = formatBytes(cached)
        } else {
            cachingTaskSizeText.value = null
        }
    }

    fun startCachingTask() {
        if (cachingTask != null) {
            Log.i(TAG, "Starting caching task, title='$title'")
            isStateUpToDate.value = false
            cachingTask!!.start()
        }
    }

    fun pauseCachingTask() {
        if (cachingTask != null) {
            Log.i(TAG, "Pausing caching task, title='$title'")
            isStateUpToDate.value = false
            cachingTask!!.pause()
        }
    }

    fun removeCachingTask() {
        if (cachingTask != null) {
            Log.i(TAG, "Removing caching task, title='$title'")
            isStateUpToDate.value = false
            cachingTask!!.remove()
        }
    }

    fun renewLicense() {
        if (cachingTask != null) {
            Log.i(TAG, "Renewing license for caching task, title='$title'")
            cachingTask!!.license().renew()
        }
    }

    companion object {
        private val TAG = OfflineSource::class.java.simpleName

        private fun formatBytes(bytes: Long): String {
            return when {
                bytes >= 1_000_000_000 -> String.format("%.1f GB", bytes / 1_000_000_000.0)
                bytes >= 1_000_000 -> String.format("%.1f MB", bytes / 1_000_000.0)
                bytes >= 1_000 -> String.format("%.0f KB", bytes / 1_000.0)
                else -> String.format("%.0f B", bytes)
            }
        }
    }
}
