package com.theoplayer.sample.playback.offline;

import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.theoplayer.android.api.THEOplayerGlobal;
import com.theoplayer.android.api.cache.Cache;
import com.theoplayer.android.api.cache.CachingParameters;
import com.theoplayer.android.api.cache.CachingTaskStatus;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.sample.playback.offline.databinding.ActivityOfflineBinding;

import java.util.Calendar;


public class OfflineActivity extends AppCompatActivity {

    private static final String TAG = OfflineActivity.class.getSimpleName();

    private Cache theoCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TheoTheme_Base);
        super.onCreate(savedInstanceState);

        // Inflating view and obtaining an instance of the binding and model classes.
        ActivityOfflineBinding viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_offline);
        OfflineSourceViewModel viewModel = new ViewModelProvider(this).get(OfflineSourceViewModel.class);

        // Gathering THEO objects references.
        theoCache = THEOplayerGlobal.getSharedInstance(this).getCache();

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar);

        // Configure UI behavior and default values.
        viewBinding.downloadableSourcesView.setLayoutManager(new LinearLayoutManager(this));
        viewBinding.downloadableSourcesView.setAdapter(new OfflineSourceAdapter(
                viewModel.getOfflineSources(),
                this::onStartCachingTaskHandler,
                this::onPauseCachingTaskHandler,
                this::onRemoveCachingTaskHandler,
                this::onPlaySourceHandler
        ));
    }

    private void onStartCachingTaskHandler(OfflineSource offlineSource) {
        if (theoCache != null) {
            CachingTaskStatus cachingTaskStatus = offlineSource.getCachingTaskStatus().getValue();

            if (cachingTaskStatus == null || cachingTaskStatus == CachingTaskStatus.EVICTED) {
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
                SourceDescription sourceDescription = SourceDescriptionRepository
                        .getBySourceUrl(OfflineActivity.this, offlineSource.getSourceUrl());

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
            offlineSource.startCachingTask();
        } else {
            // Being here means that caching is not supported.
            SpannableString toastMessage = SpannableString.valueOf(this.getString(R.string.cachingNotSupported));
            toastMessage.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, toastMessage.length(), 0);
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void onPauseCachingTaskHandler(OfflineSource offlineSource) {
        offlineSource.pauseCachingTask();
    }

    private void onRemoveCachingTaskHandler(OfflineSource offlineSource) {
        if (CachingTaskStatus.LOADING == offlineSource.getCachingTaskStatus().getValue()) {
            // Caching task to be removed is right now downloading content.
            // Asking user first if he/she really wants to cancel it.
            new AlertDialog.Builder(this)
                    .setTitle(offlineSource.getTitle())
                    .setMessage(R.string.cachingTaskCancelQuestion)
                    .setPositiveButton(R.string.yes, (dialog, buttonType) -> offlineSource.removeCachingTask())
                    .setNegativeButton(R.string.no, (dialog, buttonType) -> dialog.dismiss())
                    .show();
        } else {
            offlineSource.removeCachingTask();
        }
    }

    private void onPlaySourceHandler(OfflineSource offlineSource) {
        Log.i(TAG, "Playing source, title='" + offlineSource.getTitle() + "'");

        // There's no need to configure THEOplayer source with any caching task.
        // THEOplayer will find automatically caching task for played source if any exists.
        PlayerActivity.play(this, offlineSource.getSourceUrl());
    }

}
