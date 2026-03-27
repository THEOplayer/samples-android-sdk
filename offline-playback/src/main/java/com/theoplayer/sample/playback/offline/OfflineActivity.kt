package com.theoplayer.sample.playback.offline

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.cache.*
import com.theoplayer.sample.common.AppTopBar
import com.theoplayer.sample.playback.offline.databinding.ActivityOfflineBinding
import java.util.*

class OfflineActivity : AppCompatActivity() {
    private lateinit var theoCache: Cache
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DolbyTheme_Offline)
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding and model classes.
        val viewBinding =
            DataBindingUtil.setContentView<ActivityOfflineBinding>(this, R.layout.activity_offline)
        val viewModel = ViewModelProvider(this)[OfflineSourceViewModel::class.java]

        // Request the notification permission.
        requestNotificationPermission()

        // Gathering THEO objects references.
        theoCache = THEOplayerGlobal.getSharedInstance(this).cache!!

        viewBinding.composeToolbar.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { AppTopBar() }
        }

        // Configure UI behavior and default values.
        viewBinding.downloadableSourcesView.layoutManager = LinearLayoutManager(this)
        viewBinding.downloadableSourcesView.adapter = viewModel.offlineSources?.let {
            OfflineSourceAdapter(
                it,
                this::onStartCachingTaskHandler,
                this::onPauseCachingTaskHandler,
                this::onRenewLicenseTaskHandler,
                this::onRemoveCachingTaskHandler,
                this::onPlaySourceHandler)
        }
    }

    private fun onStartCachingTaskHandler(offlineSource: OfflineSource?) {
        val cachingTaskStatus = offlineSource!!.cachingTaskStatus.value
        if (cachingTaskStatus == null || cachingTaskStatus == CachingTaskStatus.EVICTED) {
            Log.i(TAG, "Creating caching task, title='" + offlineSource.title + "'")
            val cachingParameters = CachingParameters.Builder()

            // By default whole content is downloaded, but here we are stating that explicitly.
            // An amount of seconds (e.g. "20") or a percentage (e.g. "50%") can be specified
            // to download only part of the content.
            cachingParameters.amount("100%")

            // By default cashing task is evicted after 30 minutes since its creation.
            // Here we want to have it expired after 7 days since creation.
            val in7Days = Calendar.getInstance()
            in7Days.add(Calendar.DAY_OF_MONTH, 7)
            cachingParameters.expirationDate(in7Days.time)

            offlineSource.setCachingTask(
                theoCache.createTask(
                    offlineSource.sourceDescription,
                    cachingParameters.build()
                )
            )
        }

        offlineSource.startCachingTask()
    }

    private fun onPauseCachingTaskHandler(offlineSource: OfflineSource?) {
        offlineSource!!.pauseCachingTask()
    }

    private fun onRemoveCachingTaskHandler(offlineSource: OfflineSource?) {
        if (CachingTaskStatus.LOADING == offlineSource!!.cachingTaskStatus.value) {
            // Caching task to be removed is right now downloading content.
            // Asking user first if he/she really wants to cancel it.
            AlertDialog.Builder(this)
                .setTitle(offlineSource.title)
                .setMessage(R.string.cachingTaskCancelQuestion)
                .setPositiveButton(R.string.yes) { dialog: DialogInterface?, buttonType: Int -> offlineSource.removeCachingTask() }
                .setNegativeButton(R.string.no) { dialog: DialogInterface, buttonType: Int -> dialog.dismiss() }
                .show()
        } else {
            offlineSource.removeCachingTask()
        }
    }

    private fun onRenewLicenseTaskHandler(offlineSource: OfflineSource?) {
        offlineSource?.renewLicense()
    }

    private fun onPlaySourceHandler(offlineSource: OfflineSource?) {
        Log.i(TAG, "Playing source, title='" + offlineSource?.title + "'")

        // There's no need to configure THEOplayer source with any caching task.
        // THEOplayer will find automatically caching task for played source if any exists.
        if (offlineSource != null) {
            PlayerActivity.play(this, offlineSource.sourceDescription)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
    }

    companion object {
        private val TAG = OfflineActivity::class.java.simpleName
    }
}