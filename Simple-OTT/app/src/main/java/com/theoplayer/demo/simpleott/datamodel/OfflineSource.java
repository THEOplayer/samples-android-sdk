package com.theoplayer.demo.simpleott.datamodel;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.theoplayer.android.api.cache.CachingTask;
import com.theoplayer.android.api.cache.CachingTaskStatus;
import com.theoplayer.android.api.event.cache.task.CachingTaskEventTypes;
import com.theoplayer.demo.simpleott.FullScreenPlayerActivity;

public class OfflineSource extends AssetItem {

    private static final String TAG = OfflineSource.class.getSimpleName();

    private CachingTask cachingTask;
    private final MutableLiveData<CachingTaskStatus> cachingTaskStatus;
    private final MutableLiveData<Double> cachingTaskProgress;
    private final MutableLiveData<Boolean> uiEnabled;

    public OfflineSource(AssetItem item) {
        this.name = item.name;
        this.description = item.description;
        this.imageUrl = item.imageUrl;
        this.videoSource = item.videoSource;
        this.cachingTask = null;
        this.cachingTaskStatus = new MutableLiveData<>();
        this.cachingTaskProgress = new MutableLiveData<>();
        this.uiEnabled = new MutableLiveData<>();
    }

    public void setCachingTask(@Nullable CachingTask cachingTask) {
        this.cachingTask = cachingTask;
        cachingTaskStatus.setValue(cachingTask == null ? CachingTaskStatus.EVICTED : cachingTask.getStatus());
        cachingTaskProgress.setValue(cachingTask == null ? 0.0D : cachingTask.getPercentageCached());

        if (cachingTask != null) {

            cachingTask.addEventListener(CachingTaskEventTypes.CACHING_TASK_PROGRESS,
                    event -> {
                        Log.i(TAG, "Event: CACHING_TASK_PROGRESS, title='" + name + "', progress=" + cachingTask.getPercentageCached());
                        cachingTaskProgress.setValue(cachingTask.getPercentageCached());
                    });
            // Changing the task status is asynchronous and the code has to react on the status change
            cachingTask.addEventListener(CachingTaskEventTypes.CACHING_TASK_STATE_CHANGE,
                    event -> {
                        Log.i(TAG, "Event: CACHING_TASK_STATE_CHANGE, title='" + name + "', status=" + cachingTask.getStatus() + ", progress=" + cachingTask.getPercentageCached());
                        cachingTaskStatus.setValue(cachingTask.getStatus());
                        cachingTaskProgress.setValue(cachingTask.getPercentageCached());
                        uiEnabled.setValue(true);
                    });
        }
    }

    public LiveData<CachingTaskStatus> getCachingTaskStatusLiveData() {
        return cachingTaskStatus;
    }

    public CachingTaskStatus getCachingTaskStatus() {
        return cachingTask != null ? cachingTask.getStatus() : null;
    }

    public LiveData<Boolean> getUiEnabledLiveData() {
        return uiEnabled;
    }

    public LiveData<Double> getCachingTaskProgressLiveData() {
        return cachingTaskProgress;
    }

    public void startCachingTask() {
        uiEnabled.setValue(false);
        if (cachingTask != null) {
            Log.i(TAG, "Starting caching task, title='" + name + "'");
            cachingTask.start();
        }
    }

    public void pauseCachingTask() {
        uiEnabled.setValue(false);
        if (cachingTask != null) {
            Log.i(TAG, "Pausing caching task, title='" + name + "'");
            cachingTask.pause();
        }
    }

    public void removeCachingTask() {
        uiEnabled.setValue(false);
        if (cachingTask != null) {
            Log.i(TAG, "Removing caching task, title='" + name + "'");
            cachingTask.remove();
        }
    }

    public void play(Context context) {
        FullScreenPlayerActivity.play(context, videoSource);
    }

}
