package com.theoplayer.demo.simpleott.datamodel

import android.content.*
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.theoplayer.android.api.cache.CachingTask
import com.theoplayer.android.api.cache.CachingTaskStatus
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.cache.task.CachingTaskEventTypes
import com.theoplayer.android.api.event.cache.task.CachingTaskProgressEvent
import com.theoplayer.android.api.event.cache.task.CachingTaskStateChangeEvent
import com.theoplayer.demo.simpleott.FullScreenPlayerActivity

class OfflineSource(item: AssetItem) : AssetItem() {
    private var cachingTask: CachingTask?
    val cachingTaskStatus: MutableLiveData<CachingTaskStatus?>
    private val cachingTaskProgress: MutableLiveData<Double>
    private val uiEnabled: MutableLiveData<Boolean>

    init {
        name = item.name
        description = item.description
        imageUrl = item.imageUrl
        videoSource = item.videoSource
        cachingTask = null
        cachingTaskStatus = MutableLiveData()
        cachingTaskProgress = MutableLiveData()
        uiEnabled = MutableLiveData()
    }

    fun setCachingTask(cachingTask: CachingTask?) {
        this.cachingTask = cachingTask
        cachingTaskStatus.setValue(cachingTask?.status ?: CachingTaskStatus.EVICTED)
        cachingTaskProgress.setValue(cachingTask?.percentageCached ?: 0.0)
        if (cachingTask != null) {
            cachingTask.addEventListener(CachingTaskEventTypes.CACHING_TASK_PROGRESS,
                EventListener { event: CachingTaskProgressEvent? ->
                    Log.i(
                        TAG,
                        "Event: CACHING_TASK_PROGRESS, title='" + name + "', progress=" + cachingTask.percentageCached
                    )
                    cachingTaskProgress.setValue(cachingTask.percentageCached)
                })
            // Changing the task status is asynchronous and the code has to react on the status change
            cachingTask.addEventListener(CachingTaskEventTypes.CACHING_TASK_STATE_CHANGE,
                EventListener { event: CachingTaskStateChangeEvent? ->
                    Log.i(
                        TAG,
                        "Event: CACHING_TASK_STATE_CHANGE, title='" + name + "', status=" + cachingTask.status + ", progress=" + cachingTask.percentageCached
                    )
                    cachingTaskStatus.setValue(cachingTask.status)
                    cachingTaskProgress.setValue(cachingTask.percentageCached)
                    uiEnabled.setValue(true)
                })
        }
    }

    val cachingTaskStatusLiveData: LiveData<CachingTaskStatus?>
        get() = cachingTaskStatus

    fun getCachingTaskStatus(): CachingTaskStatus? {
        return if (cachingTask != null) cachingTask!!.status else null
    }

    val uiEnabledLiveData: LiveData<Boolean?>
        get() = uiEnabled
    val cachingTaskProgressLiveData: LiveData<Double>
        get() = cachingTaskProgress

    fun startCachingTask() {
        uiEnabled.value = false
        if (cachingTask != null) {
            Log.i(TAG, "Starting caching task, title='$name'")
            cachingTask!!.start()
        }
    }

    fun pauseCachingTask() {
        uiEnabled.value = false
        if (cachingTask != null) {
            Log.i(TAG, "Pausing caching task, title='$name'")
            cachingTask!!.pause()
        }
    }

    fun removeCachingTask() {
        uiEnabled.value = false
        if (cachingTask != null) {
            Log.i(TAG, "Removing caching task, title='$name'")
            cachingTask!!.remove()
        }
    }

    fun play(context: Context) {
        FullScreenPlayerActivity.play(context, videoSource)
    }

    companion object {
        private val TAG = OfflineSource::class.java.simpleName
    }
}