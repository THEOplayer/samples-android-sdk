package com.theoplayer.sample.playback.offline;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.theoplayer.android.api.THEOplayerGlobal;
import com.theoplayer.android.api.cache.Cache;
import com.theoplayer.android.api.cache.CacheStatus;
import com.theoplayer.android.api.cache.CachingTask;
import com.theoplayer.android.api.cache.CachingTaskStatus;

public class OfflineDrmLicenseRenewalWorker extends Worker {

    private static final String TAG = OfflineDrmLicenseRenewalWorker.class.getSimpleName();

    public OfflineDrmLicenseRenewalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Cache theoCache = THEOplayerGlobal.getSharedInstance(getApplicationContext()).getCache();
        if (theoCache != null && theoCache.getStatus() == CacheStatus.INITIALISED) {
            for (CachingTask cachingTask : theoCache.getTasks()) {
                if (cachingTask.getStatus() != CachingTaskStatus.EVICTED) {
                    Log.i(TAG, "Renewing license, sourceUrl=" + cachingTask.getSource().getSources().get(0).getSrc());
                    cachingTask.license().renew();
                }
            }
        }
        return Result.success();
    }

}
