package com.theoplayer.demo.simpleott;

import android.content.Context;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.theoplayer.android.api.cache.Cache;
import com.theoplayer.android.api.cache.CacheStatus;
import com.theoplayer.android.api.cache.CachingParameters;
import com.theoplayer.android.api.cache.CachingTask;
import com.theoplayer.android.api.cache.CachingTaskStatus;
import com.theoplayer.android.api.event.cache.CacheEventTypes;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.demo.simpleott.datamodel.AssetItem;
import com.theoplayer.demo.simpleott.datamodel.OfflineSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class OfflineHandler {

    private static final String TAG = OfflineHandler.class.getSimpleName();

    private final Cache theoCache;
    private final ArrayList<OfflineSource> offlineSources;
    private final Context context;
    private LiveData<Boolean> onlyOnWifiSetting;
    private LiveData<Boolean> wifiConnected;

    public OfflineHandler(
            Context m,
            Cache theoCache,
            AssetItem[] vods,
            LiveData<Boolean> onlyOnWifi,
            LiveData<Boolean> wifiConnected) {

        this.theoCache = theoCache;
        this.context = m;
        this.offlineSources = new ArrayList<>();
        this.onlyOnWifiSetting = onlyOnWifi;
        this.wifiConnected = wifiConnected;
        this.onlyOnWifiSetting.observe((LifecycleOwner) context, this::pauseWhenOnWifiSettingChanged);
        this.wifiConnected.observe((LifecycleOwner) context, this::pauseOrResumeWhenOnWifiStatusChanged);

        for (AssetItem vod : vods) {
            offlineSources.add(new OfflineSource(vod));
        }
    }

    public void init() {
        // initializing caching tasks from THEO cache
        if (this.theoCache != null) {
            if (this.theoCache.getStatus() == CacheStatus.INITIALISED) {
                loadExistingCachingTasks();
            } else {
                this.theoCache.addEventListener(CacheEventTypes.CACHE_STATE_CHANGE,
                        event -> loadExistingCachingTasks());
            }
        }
        if (this.theoCache != null) {
            loadExistingCachingTasks();
        }
    }

    private void loadExistingCachingTasks() {
        if (theoCache != null) {
            Log.i(TAG, "Event: CACHE_INITIALISED, found " + theoCache.getTasks().length() + " tasks...");
            for (CachingTask cachingTask : theoCache.getTasks()) {
                String cachingTaskSourceUrl = cachingTask.getSource().getSources().get(0).getSrc();
                for (OfflineSource offlineSource : offlineSources) {
                    if (offlineSource.videoSource.equals(cachingTaskSourceUrl)) {
                        Log.i(TAG, "Setting caching task for: " + cachingTaskSourceUrl);
                        offlineSource.setCachingTask(cachingTask);
                        break;
                    }
                }
            }
        }
    }

    public void deleteAllCachedItems() {
        for (OfflineSource offlineSource : offlineSources) {
            offlineSource.removeCachingTask();
        }
    }

    public void startCachingTaskHandler(OfflineSource offlineSource) {
        if (theoCache != null) {
            CachingTaskStatus cachingTaskStatus = offlineSource.getCachingTaskStatusLiveData().getValue();

            if (cachingTaskStatus == null ||
                    cachingTaskStatus == CachingTaskStatus.EVICTED ||
                    cachingTaskStatus == CachingTaskStatus.ERROR) {
                Log.i(TAG, "Creating caching task, title='" + offlineSource.name + "'");
                CachingParameters.Builder cachingParameters = new CachingParameters.Builder();

                // By default whole content is downloaded, but here we are stating that explicitly.
                // An amount of seconds (e.g. "20") or a percentage (e.g. "50%") can be specified
                // to download only part of the content.
                cachingParameters.amount("100%");

                // By default cashing task is evicted after 30 minutes since its creation.
                // Here we want to have it expired after 7 days since creation.
                Calendar in7Days = Calendar.getInstance();
                in7Days.add(Calendar.DAY_OF_MONTH, 7);
                cachingParameters.expirationDate(in7Days.getTime());

                // Getting prepared source description for given source.
                SourceDescription sourceDescription = SourceDescriptionUtil.getBySourceUrl(offlineSource.videoSource);

                if (sourceDescription != null) {
                    // Creating caching task for given source and adding appropriate event listeners to it.
                    // Newly created caching task does not start downloading automatically.
                    offlineSource.setCachingTask(theoCache.createTask(
                            sourceDescription,
                            cachingParameters.build()
                    ));
                }
            }

            // Starting caching task, content is being downloaded.
            startOfflineSourceUnderConditions(offlineSource);
        } else {
            // Being here means that caching is not supported.
            SpannableString toastMessage = SpannableString.valueOf(context.getString(R.string.cachingNotSupported));
            toastMessage.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, toastMessage.length(), 0);
            Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void startOfflineSourceUnderConditions(OfflineSource offlineSource) {
        // If "only on wifi" setting is set to true, then check if WiFi connection is available
        // If it isn't then inform the user that download will not start
        if (onlyOnWifiSetting.getValue() == Boolean.TRUE && wifiConnected.getValue() == Boolean.FALSE) {
            SpannableString toastMessage = SpannableString.valueOf(context.getString(R.string.notOnWifi));
            toastMessage.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, toastMessage.length(), 0);
            Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
        } else {
            offlineSource.startCachingTask();
        }
    }

    private void pauseWhenOnWifiSettingChanged(boolean onlyOnWifiSetting) {
        // When downloading on mobile data (not on wifi) and the user changes the "Only on WiFi" setting, pause the download
        if (onlyOnWifiSetting && wifiConnected.getValue() == Boolean.FALSE) {
            for (OfflineSource offlineSource : this.filterOfflineSourcesByState(CachingTaskStatus.LOADING)) {
                offlineSource.pauseCachingTask();
            }
        }
    }

    private void pauseOrResumeWhenOnWifiStatusChanged(boolean isWifiConnected) {
        // Resume the download when the WiFi connection is restored
        // Mind that some tasks may end up with "ERROR" status after turning off WiFi connection. Those need to be handled as well
        if (onlyOnWifiSetting.getValue() == Boolean.TRUE) {
            if (!isWifiConnected) {
                for (OfflineSource offlineSource : this.filterOfflineSourcesByState(CachingTaskStatus.LOADING)) {
                    offlineSource.pauseCachingTask();
                }
            } else {
                // Handling "ERROR"ed tasks
                for (OfflineSource offlineSource : this.filterOfflineSourcesByState(CachingTaskStatus.ERROR)) {
                    startCachingTaskHandler(offlineSource);
                }
                for (OfflineSource offlineSource : this.filterOfflineSourcesByState(CachingTaskStatus.IDLE)) {
                    offlineSource.startCachingTask();
                }
            }
        }
    }

    private ArrayList<OfflineSource> filterOfflineSourcesByState(CachingTaskStatus... status) {
        ArrayList<OfflineSource> result = new ArrayList<>();
        for (OfflineSource of : offlineSources) {
            if (Arrays.asList(status).indexOf(of.getCachingTaskStatus()) > -1) {
                result.add(of);
            }
        }
        return result;
    }

    void removeCachingTaskHandler(OfflineSource offlineSource) {
        // Before deleting a task, ask the user for confirmation
        if (CachingTaskStatus.DONE == offlineSource.getCachingTaskStatusLiveData().getValue()) {
            new AlertDialog.Builder(this.context)
                    .setTitle(offlineSource.name)
                    .setMessage(R.string.cachingTaskCancelQuestion)
                    .setPositiveButton(R.string.yes, (dialog, buttonType) -> offlineSource.removeCachingTask())
                    .setNegativeButton(R.string.no, (dialog, buttonType) -> dialog.dismiss())
                    .show();
        } else {
            offlineSource.removeCachingTask();
        }
    }

    OfflineSource[] getOfflineSources() {
        return offlineSources.toArray(new OfflineSource[0]);
    }
}
