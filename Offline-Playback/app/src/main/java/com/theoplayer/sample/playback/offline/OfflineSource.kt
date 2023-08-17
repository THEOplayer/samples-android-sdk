package com.theoplayer.sample.playback.offline

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.theoplayer.android.api.cache.CachingTask
import com.theoplayer.android.api.cache.CachingTaskStatus
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.cache.task.CachingTaskEventTypes
import com.theoplayer.android.api.event.cache.task.CachingTaskProgressEvent
import com.theoplayer.android.api.event.cache.task.CachingTaskStateChangeEvent

class OfflineSource internal constructor(
    val title: String,
    val poster: String,
    val sourceUrl: String
) {
    private var cachingTask: CachingTask? = null
    val cachingTaskStatus: MutableLiveData<CachingTaskStatus?> = MutableLiveData()
    val isStateUpToDate: MutableLiveData<Boolean?> = MutableLiveData()
    val cachingTaskProgress: MutableLiveData<Double?> = MutableLiveData()

    fun setCachingTask(cachingTask: CachingTask?) {
        this.cachingTask = cachingTask
        isStateUpToDate.value = false
        cachingTaskStatus.value = cachingTask?.status ?: CachingTaskStatus.EVICTED
        cachingTaskProgress.value = cachingTask?.percentageCached ?: 0.0
        if (cachingTask != null) {
            cachingTask.addEventListener(
                CachingTaskEventTypes.CACHING_TASK_PROGRESS,
                EventListener { event: CachingTaskProgressEvent? ->
                    Log.i(
                        TAG,
                        "Event: CACHING_TASK_PROGRESS, title='" + title + "', progress=" + cachingTask.percentageCached
                    )
                    cachingTaskProgress.setValue(cachingTask.percentageCached)
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
                    isStateUpToDate.setValue(true)
                })
        }
        isStateUpToDate.value = true
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
    }
}