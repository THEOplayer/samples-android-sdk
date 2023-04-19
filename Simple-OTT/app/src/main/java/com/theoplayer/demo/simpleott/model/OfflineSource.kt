package com.theoplayer.demo.simpleott.model;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.theoplayer.android.api.cache.CachingTask;
import com.theoplayer.android.api.cache.CachingTaskStatus;
import com.theoplayer.android.api.event.cache.task.CachingTaskEventTypes;

/**
 * Wrapper for stream source definition. It is flavored with caching task so stream source can be
 * downloaded and UI can be updated with download state and progress.
 */
public class OfflineSource extends StreamSource {

    private static final String TAG = OfflineSource.class.getSimpleName();

    private CachingTask cachingTask;
    private final MutableLiveData<CachingTaskStatus> cachingTaskStatus = new MutableLiveData<>();
    private final MutableLiveData<Double> cachingTaskProgress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> stateUpToDate = new MutableLiveData<>();

    OfflineSource(StreamSource streamSource) {
        super(
                streamSource.getTitle(),
                streamSource.getDescription(),
                streamSource.getSource(),
                streamSource.getImageResId()
        );
    }

    /**
     * Provides holder of this <code>OfflineSource</code> instance state up-to-dateness that can be observed.
     *
     * @return this <code>OfflineSource</code> instance state up-to-dateness holder.
     */
    public LiveData<Boolean> isStateUpToDate() {
        return stateUpToDate;
    }

    /**
     * Provides caching task status holder that can be observed.
     *
     * @return caching task status holder.
     */
    public LiveData<CachingTaskStatus> getCachingTaskStatus() {
        return cachingTaskStatus;
    }

    /**
     * Provides caching task progress holder that can be observed.
     *
     * @return caching task progress holder.
     */
    public LiveData<Double> getCachingTaskProgress() {
        return cachingTaskProgress;
    }

    /**
     * Assigns <code>CachingTask</code> instance and configures current <code>OfflineSource</code>
     * instances with it.
     *
     * @param cachingTask - The corresponding caching task.
     */
    void assignCachingTask(@Nullable CachingTask cachingTask) {
        this.cachingTask = cachingTask;
        stateUpToDate.setValue(false);
        cachingTaskStatus.setValue(cachingTask == null ? CachingTaskStatus.EVICTED : cachingTask.getStatus());
        cachingTaskProgress.setValue(cachingTask == null ? 0.0D : cachingTask.getPercentageCached());

        if (cachingTask != null) {
            cachingTask.addEventListener(CachingTaskEventTypes.CACHING_TASK_PROGRESS,
                    event -> {
                        Log.i(TAG, "Event: CACHING_TASK_PROGRESS, title='" + getTitle() + "', progress=" + cachingTask.getPercentageCached());
                        cachingTaskProgress.setValue(cachingTask.getPercentageCached());
                    });
            cachingTask.addEventListener(CachingTaskEventTypes.CACHING_TASK_STATE_CHANGE,
                    event -> {
                        Log.i(TAG, "Event: CACHING_TASK_STATE_CHANGE, title='" + getTitle() + "', status=" + cachingTask.getStatus() + ", progress=" + cachingTask.getPercentageCached());
                        cachingTaskStatus.setValue(cachingTask.getStatus());
                        cachingTaskProgress.setValue(cachingTask.getPercentageCached());
                        stateUpToDate.setValue(true);
                    });
        }
        stateUpToDate.setValue(true);
    }

    /**
     * Checks if assigned caching task is in given status.
     *
     * @param cachingTaskStatus - caching task status to check
     * @return <code>true</code> - if assigned caching task is in queried status; <code>false</code> otherwise.
     */
    boolean hasStatus(CachingTaskStatus cachingTaskStatus) {
        return cachingTask != null && cachingTask.getStatus() == cachingTaskStatus;
    }

    /**
     * Allows to start assigned caching task updating this <code>OfflineSource</code> instance
     * state accordingly. Source is being downloaded.
     */
    void startCachingTask() {
        if (cachingTask != null) {
            Log.i(TAG, "Starting caching task, title='" + getTitle() + "'");
            stateUpToDate.setValue(false);
            cachingTask.start();
        }
    }

    /**
     * Allows to pause assigned caching task updating this <code>OfflineSource</code> instance
     * state accordingly. Source downloading is on-hold.
     */
    void pauseCachingTask() {
        if (cachingTask != null) {
            Log.i(TAG, "Pausing caching task, title='" + getTitle() + "'");
            stateUpToDate.setValue(false);
            cachingTask.pause();
        }
    }

    /**
     * Allows to remove assigned caching task updating this <code>OfflineSource</code> instance
     * state accordingly. Active caching task is cancelled. Any downloaded content is purged.
     */
    void removeCachingTask() {
        if (cachingTask != null) {
            Log.i(TAG, "Removing caching task, title='" + getTitle() + "'");
            stateUpToDate.setValue(false);
            cachingTask.remove();
        }
    }

}
