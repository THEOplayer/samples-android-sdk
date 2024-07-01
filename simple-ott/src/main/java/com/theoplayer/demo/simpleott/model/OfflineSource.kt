package com.theoplayer.demo.simpleott.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.theoplayer.android.api.cache.CachingTask
import com.theoplayer.android.api.cache.CachingTaskStatus
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.cache.task.CachingTaskEventTypes
import com.theoplayer.android.api.event.cache.task.CachingTaskProgressEvent
import com.theoplayer.android.api.event.cache.task.CachingTaskStateChangeEvent

/**
 * Wrapper for stream source definition. It is flavored with caching task so stream source can be
 * downloaded and UI can be updated with download state and progress.
 */
class OfflineSource internal constructor(streamSource: StreamSource) : StreamSource(
    streamSource.title,
    streamSource.description,
    streamSource.source,
    streamSource.imageResId
) {
    private var cachingTask: CachingTask? = null

    /**
     * Provides caching task status holder that can be observed.
     *
     * @return caching task status holder.
     */
    val cachingTaskStatus = MutableLiveData<CachingTaskStatus?>()

    /**
     * Provides caching task progress holder that can be observed.
     *
     * @return caching task progress holder.
     */
    val cachingTaskProgress = MutableLiveData<Double?>()

    /**
     * Provides holder of this `OfflineSource` instance state up-to-dateness that can be observed.
     *
     * @return this `OfflineSource` instance state up-to-dateness holder.
     */
    val isStateUpToDate = MutableLiveData<Boolean?>()

    /**
     * Assigns `CachingTask` instance and configures current `OfflineSource`
     * instances with it.
     *
     * @param cachingTask - The corresponding caching task.
     */
    fun assignCachingTask(cachingTask: CachingTask?) {
        this.cachingTask = cachingTask
        isStateUpToDate.value = false
        cachingTaskStatus.value = cachingTask?.status ?: CachingTaskStatus.EVICTED
        cachingTaskProgress.value = cachingTask?.percentageCached ?: 0.0
        if (cachingTask != null) {
            cachingTask.addEventListener(CachingTaskEventTypes.CACHING_TASK_PROGRESS,
                EventListener { event: CachingTaskProgressEvent? ->
                    Log.i(
                        TAG,
                        "Event: CACHING_TASK_PROGRESS, title='" + title + "', progress=" + cachingTask.percentageCached
                    )
                    cachingTaskProgress.setValue(cachingTask.percentageCached)
                })
            cachingTask.addEventListener(CachingTaskEventTypes.CACHING_TASK_STATE_CHANGE,
                EventListener { event: CachingTaskStateChangeEvent? ->
                    Log.i(
                        TAG,
                        "Event: CACHING_TASK_STATE_CHANGE, title='" + title + "', status=" + cachingTask.status + ", progress=" + cachingTask.percentageCached
                    )
                    cachingTaskStatus.setValue(cachingTask.status)
                    cachingTaskProgress.setValue(cachingTask.percentageCached)
                    isStateUpToDate.setValue(true)
                })
        }
        isStateUpToDate.setValue(true)
    }

    /**
     * Checks if assigned caching task is in given status.
     *
     * @param cachingTaskStatus - caching task status to check
     * @return `true` - if assigned caching task is in queried status; `false` otherwise.
     */
    fun hasStatus(cachingTaskStatus: CachingTaskStatus): Boolean {
        return cachingTask != null && cachingTask!!.status == cachingTaskStatus
    }

    /**
     * Allows to start assigned caching task updating this `OfflineSource` instance
     * state accordingly. Source is being downloaded.
     */
    fun startCachingTask() {
        if (cachingTask != null) {
            Log.i(TAG, "Starting caching task, title='$title'")
            isStateUpToDate.value = false
            cachingTask!!.start()
        }
    }

    /**
     * Allows to pause assigned caching task updating this `OfflineSource` instance
     * state accordingly. Source downloading is on-hold.
     */
    fun pauseCachingTask() {
        if (cachingTask != null) {
            Log.i(TAG, "Pausing caching task, title='$title'")
            isStateUpToDate.value = false
            cachingTask!!.pause()
        }
    }

    /**
     * Allows to remove assigned caching task updating this `OfflineSource` instance
     * state accordingly. Active caching task is cancelled. Any downloaded content is purged.
     */
    fun removeCachingTask() {
        if (cachingTask != null) {
            Log.i(TAG, "Removing caching task, title='$title'")
            isStateUpToDate.value = false
            cachingTask!!.remove()
        }
    }

    companion object {
        private val TAG = OfflineSource::class.java.simpleName
    }
}