package com.theoplayer.sample.playback.offline

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.cache.CacheStatus
import com.theoplayer.android.api.cache.CachingTaskStatus

class OfflineDrmLicenseRenewalWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val theoCache = THEOplayerGlobal.getSharedInstance(applicationContext).cache
        if (theoCache != null && theoCache.status == CacheStatus.INITIALISED) {
            for (cachingTask in theoCache.tasks) {
                if (cachingTask.status != CachingTaskStatus.EVICTED) {
                    Log.i(TAG, "Renewing license, sourceUrl=" + cachingTask.source.sources[0].src)
                    cachingTask.license().renew()
                }
            }
        }
        return Result.success()
    }

    companion object {
        private val TAG = OfflineDrmLicenseRenewalWorker::class.java.simpleName
    }
}