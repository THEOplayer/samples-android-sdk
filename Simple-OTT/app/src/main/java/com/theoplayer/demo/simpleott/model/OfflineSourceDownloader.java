package com.theoplayer.demo.simpleott.model;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.theoplayer.android.api.THEOplayerGlobal;
import com.theoplayer.android.api.cache.Cache;
import com.theoplayer.android.api.cache.CacheStatus;
import com.theoplayer.android.api.cache.CachingParameters;
import com.theoplayer.android.api.cache.CachingTask;
import com.theoplayer.android.api.cache.CachingTaskStatus;
import com.theoplayer.android.api.event.cache.CacheEventTypes;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.TypedSource;
import com.theoplayer.demo.simpleott.R;
import com.theoplayer.demo.simpleott.ToastUtils;
import com.theoplayer.demo.simpleott.network.WiFiNetworkInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * This class allows to download content of a stream for offline playback.
 * <p/>
 * It uses THEOplayer's <code>Cache</code> instance.
 * <p/>
 * Download possibility depends on WiFi connectivity state. User can decide to allow download only
 * when WiFi is connected.
 */
public class OfflineSourceDownloader {

    private static final String TAG = OfflineSourceDownloader.class.getSimpleName();

    private Context context;
    private Cache theoCache;
    private WiFiNetworkInfo wiFiNetworkInfo;
    private List<OfflineSource> offlineSources;

    public OfflineSourceDownloader(Context context,
                                   StreamSourceRepository streamSourceRepository,
                                   WiFiNetworkInfo wiFiNetworkInfo) {
        this.context = context;
        this.theoCache = THEOplayerGlobal.getSharedInstance(context).getCache();
        this.wiFiNetworkInfo = wiFiNetworkInfo;
        this.offlineSources = new ArrayList<>();

        // Wrapping StreamSource instances with OfflineSource to allow downloading content.
        for (StreamSource streamSource : streamSourceRepository.getOfflineStreamSources()) {
            this.offlineSources.add(new OfflineSource((streamSource)));
        }

        // Recovering any existing THEOplayer's cache state.
        if (this.theoCache != null) {
            if (this.theoCache.getStatus() == CacheStatus.INITIALISED) {
                loadExistingCachingTasks();
            } else {
                this.theoCache.addEventListener(CacheEventTypes.CACHE_STATE_CHANGE,
                        event -> loadExistingCachingTasks());
            }
        }

        // Observing WiFi network info changes
        this.wiFiNetworkInfo.downloadOnlyOnWiFi().observe((LifecycleOwner) context,
                downloadOnlyOnWiFi -> onWiFiNetworkInfoChange(downloadOnlyOnWiFi, wiFiNetworkInfo.isConnectedToWiFi()));
        this.wiFiNetworkInfo.connectedToWiFi().observe((LifecycleOwner) context,
                connectedToWiFi -> onWiFiNetworkInfoChange(wiFiNetworkInfo.isDownloadOnlyOnWiFi(), connectedToWiFi));
    }

    /**
     * Returns offline stream sources wrapped in <code>OfflineSource</code> object.
     *
     * @return - offline sources.
     */
    public List<OfflineSource> getOfflineSources() {
        return offlineSources;
    }

    /**
     * Starts caching task to download stream content for given <code>offlineSource</code>.
     * <p/>
     * If caching task does not exist yet if is created. New caching task is configured to download
     * whole stream content and to expire after 7 days of its creation.
     *
     * @param offlineSource - offline source for which content has to be downloaded.
     */
    public void startCachingTask(OfflineSource offlineSource) {
        if (theoCache != null) {
            CachingTaskStatus cachingTaskStatus = offlineSource.getCachingTaskStatus().getValue();

            if (cachingTaskStatus == null || cachingTaskStatus == CachingTaskStatus.EVICTED
                    || cachingTaskStatus == CachingTaskStatus.ERROR) {
                Log.i(TAG, "Creating caching task, title='" + offlineSource.getTitle() + "'");
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
                SourceDescription sourceDescription = SourceDescription.Builder.sourceDescription(
                        TypedSource.Builder.typedSource(offlineSource.getSource()).build()
                ).build();

                if (sourceDescription != null) {
                    // Creating caching task for given source and adding appropriate event listeners to it.
                    // Newly created caching task does not start downloading automatically.
                    offlineSource.assignCachingTask(theoCache.createTask(
                            sourceDescription,
                            cachingParameters.build()
                    ));
                }
            }

            // If "Download only on wifi" is set to true, then check if WiFi connection is available
            // If it isn't then inform the user that download will not start
            if (wiFiNetworkInfo.isDownloadOnlyOnWiFi() && !wiFiNetworkInfo.isConnectedToWiFi()) {
                ToastUtils.toastMessage(context, R.string.wifiDisconnectedWarning);
            } else {
                // Starting caching task, content is being downloaded.
                offlineSource.startCachingTask();
            }
        } else {
            // Being here means that caching is not supported.
            ToastUtils.toastMessage(context, R.string.cachingNotSupported);
        }
    }

    /**
     * Pauses caching task so stream content download for given <code>offlineSource</code> is on-hold.
     *
     * @param offlineSource - offline source for which to pause content download.
     */
    public void pauseCachingTask(OfflineSource offlineSource) {
        offlineSource.pauseCachingTask();
    }

    /**
     * Removes caching task so download stream content for given <code>offlineSource</code> is purged.
     *
     * @param offlineSource - offline source for which to remove downloaded content.
     */
    public void removeCachingTask(OfflineSource offlineSource) {
        // Before deleting a task, ask the user for confirmation
        if (offlineSource.hasStatus(CachingTaskStatus.DONE)) {
            new MaterialAlertDialogBuilder(this.context)
                    .setTitle(offlineSource.getTitle())
                    .setMessage(R.string.removeCachingTaskQuestion)
                    .setNegativeButton(R.string.no, (dialog, buttonType) -> dialog.dismiss())
                    .setPositiveButton(R.string.yes, (dialog, buttonType) -> removeCachingTaskInternal(offlineSource))
                    .show();
        } else {
            removeCachingTaskInternal(offlineSource);
        }
    }

    private void removeCachingTaskInternal(OfflineSource offlineSource) {
        offlineSource.removeCachingTask();
        if (theoCache != null) {
            // In case there exist some detached caching tasks for the same source
            for (CachingTask cachingTask : theoCache.getTasks()) {
                if (cachingTask.getSource().getSources().get(0).getSrc().equals(offlineSource.getSource())) {
                    cachingTask.remove();
                }
            }
        }
    }

    /**
     * Removes all existing caching tasks. Any downloaded content of all <code>OfflineSource</code>
     * instances is purged.
     */
    public void removeAllCachingTasks() {
        new MaterialAlertDialogBuilder(this.context)
                .setTitle(R.string.removeAllCachingTasks)
                .setMessage(R.string.removeAllCachingTasksQuestion)
                .setNegativeButton(R.string.no, (dialog, buttonType) -> dialog.dismiss())
                .setPositiveButton(R.string.yes, (dialog, buttonType) -> removeAllCachingTasksInternal())
                .show();
    }

    private void removeAllCachingTasksInternal() {
        for (OfflineSource offlineSource : offlineSources) {
            offlineSource.removeCachingTask();
        }
        // In case there exist some detached caching tasks
        if (theoCache != null) {
            for (CachingTask cachingTask : theoCache.getTasks()) {
                cachingTask.remove();
            }
        }
    }

    /**
     * Updates offline sources by corresponding caching tasks if exists.
     * <p/>
     * Note that, there can be cases when content is being cached (or is cached), but app was
     * destroyed by system (or by user). After launching app again, offline sources will be
     * updated with those caching tasks and caching progress will be again presented to the user.
     */
    private void loadExistingCachingTasks() {
        if (theoCache != null && theoCache.getStatus() == CacheStatus.INITIALISED) {
            Log.i(TAG, "Event: CACHE_INITIALISED, found " + theoCache.getTasks().length() + " tasks...");
            for (CachingTask cachingTask : theoCache.getTasks()) {
                String cachingTaskSourceUrl = cachingTask.getSource().getSources().get(0).getSrc();
                for (OfflineSource offlineSource : offlineSources) {
                    if (offlineSource.getSource().equals(cachingTaskSourceUrl)) {
                        Log.i(TAG, "Setting caching task for: " + cachingTaskSourceUrl);
                        offlineSource.assignCachingTask(cachingTask);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Manages existing caching task according to current WiFi connectivity state.
     * <p/>
     * When user wants to download only when WiFi is connected then when WiFi connection is lost
     * all ongoing downloads are paused and when WiFi connection is restored then all halted
     * caching tasks are started again.
     *
     * @param downloadOnlyOnWiFi - <code>true</code> if download is allowed when WiFi is connected;
     *                           <code>false</code> otherwise.
     * @param connectedToWiFi    - <code>true</code> if WiFi is connected; <code>false</code> otherwise.
     */
    private void onWiFiNetworkInfoChange(boolean downloadOnlyOnWiFi, boolean connectedToWiFi) {
        if (downloadOnlyOnWiFi) {
            for (OfflineSource offlineSource : offlineSources) {
                if (connectedToWiFi) {
                    // Start all halted downloads, some tasks may end up with "ERROR" status after turning off WiFi connection
                    if (offlineSource.hasStatus(CachingTaskStatus.ERROR) || offlineSource.hasStatus(CachingTaskStatus.IDLE)) {
                        startCachingTask(offlineSource);
                    }
                } else {
                    // Pause all ongoing downloads
                    if (offlineSource.hasStatus(CachingTaskStatus.LOADING)) {
                        pauseCachingTask(offlineSource);
                    }
                }
            }
        }
    }

}
