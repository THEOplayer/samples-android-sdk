package com.theoplayer.sample.playback.offline

import android.content.DialogInterface
import android.os.Bundle
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.cache.*
import com.theoplayer.sample.playback.offline.databinding.ActivityOfflineBinding
import java.util.*

class OfflineActivity : AppCompatActivity() {
    private var theoCache: Cache? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TheoTheme_Base)
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding and model classes.
        val viewBinding =
            DataBindingUtil.setContentView<ActivityOfflineBinding>(this, R.layout.activity_offline)
        val viewModel = ViewModelProvider(this).get(
            OfflineSourceViewModel::class.java
        )

        // Gathering THEO objects references.
        theoCache = THEOplayerGlobal.getSharedInstance(this).cache

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)

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
        if (theoCache != null) {
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

                // Getting prepared source description for given source.
                val sourceDescription = SourceDescriptionRepository.getBySourceUrl(
                    this@OfflineActivity,
                    offlineSource.sourceUrl
                )
                if (sourceDescription != null) {
                    // Creating caching task for given source and adding appropriate event listeners to it.
                    // Newly created caching task does not start downloading automatically.
                    offlineSource.setCachingTask(
                        theoCache!!.createTask(
                            sourceDescription,
                            cachingParameters.build()
                        )
                    )
                }
            }

            // Starting caching task, content is being downloaded.
            offlineSource.startCachingTask()
        } else {
            // Being here means that caching is not supported.
            val toastMessage = SpannableString.valueOf(this.getString(R.string.cachingNotSupported))
            toastMessage.setSpan(
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0,
                toastMessage.length,
                0
            )
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
        }
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
        PlayerActivity.play(this, offlineSource?.sourceUrl)
    }

    companion object {
        private val TAG = OfflineActivity::class.java.simpleName
    }
}