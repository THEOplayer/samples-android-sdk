package com.theoplayer.sample.playback.offline;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.theoplayer.android.api.cache.CachingTask;
import com.theoplayer.android.api.cache.CachingTaskStatus;
import com.theoplayer.android.api.event.cache.task.CachingTaskEventTypes;

public class OfflineSource {

    private static final String TAG = OfflineSource.class.getSimpleName();

    private String title;
    private String poster;
    private String sourceUrl;
    private CachingTask cachingTask;
    private MutableLiveData<CachingTaskStatus> cachingTaskStatus;
    private MutableLiveData<Double> cachingTaskProgress;
    private MutableLiveData<Boolean> stateUpToDate;

    OfflineSource(String title, String poster, String sourceUrl) {
        this.title = title;
        this.poster = poster;
        this.sourceUrl = sourceUrl;
        this.cachingTask = null;
        this.cachingTaskStatus = new MutableLiveData<>();
        this.cachingTaskProgress = new MutableLiveData<>();
        this.stateUpToDate = new MutableLiveData<>();
    }

    public void setCachingTask(@Nullable CachingTask cachingTask) {
        this.cachingTask = cachingTask;
        stateUpToDate.setValue(false);
        cachingTaskStatus.setValue(cachingTask == null ? CachingTaskStatus.EVICTED : cachingTask.getStatus());
        cachingTaskProgress.setValue(cachingTask == null ? 0.0D : cachingTask.getPercentageCached());

        if (cachingTask != null) {
            cachingTask.addEventListener(CachingTaskEventTypes.CACHING_TASK_PROGRESS,
                    event -> {
                        Log.i(TAG, "Event: CACHING_TASK_PROGRESS, title='" + title + "', progress=" + cachingTask.getPercentageCached());
                        cachingTaskProgress.setValue(cachingTask.getPercentageCached());
                    });

            cachingTask.addEventListener(CachingTaskEventTypes.CACHING_TASK_STATE_CHANGE,
                    event -> {
                        Log.i(TAG, "Event: CACHING_TASK_STATE_CHANGE, title='" + title + "', status=" + cachingTask.getStatus() + ", progress=" + cachingTask.getPercentageCached());
                        cachingTaskStatus.setValue(cachingTask.getStatus());
                        cachingTaskProgress.setValue(cachingTask.getPercentageCached());
                        stateUpToDate.setValue(true);
                    });
        }
        stateUpToDate.setValue(true);
    }

    public String getTitle() {
        return title;
    }

    public String getPoster() {
        return poster;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public LiveData<CachingTaskStatus> getCachingTaskStatus() {
        return cachingTaskStatus;
    }

    public LiveData<Double> getCachingTaskProgress() {
        return cachingTaskProgress;
    }

    public MutableLiveData<Boolean> isStateUpToDate() {
        return stateUpToDate;
    }

    public void startCachingTask() {
        if (cachingTask != null) {
            Log.i(TAG, "Starting caching task, title='" + title + "'");
            stateUpToDate.setValue(false);
            cachingTask.start();
        }
    }

    public void pauseCachingTask() {
        if (cachingTask != null) {
            Log.i(TAG, "Pausing caching task, title='" + title + "'");
            stateUpToDate.setValue(false);
            cachingTask.pause();
        }
    }

    public void removeCachingTask() {
        if (cachingTask != null) {
            Log.i(TAG, "Removing caching task, title='" + title + "'");
            stateUpToDate.setValue(false);
            cachingTask.remove();
        }
    }

}
