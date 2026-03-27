package com.theoplayer.demo.simpleott.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.theoplayer.android.api.cache.CachingTaskStatus
import com.theoplayer.demo.simpleott.R
import com.theoplayer.demo.simpleott.databinding.LayoutOfflineSourceBinding
import com.theoplayer.demo.simpleott.model.OfflineSource
import kotlin.math.roundToInt

internal class OfflineSourceAdapter(
    private val offlineSources: List<OfflineSource>,
    private val onStartCachingTaskHandler: Consumer<OfflineSource>,
    private val onPauseCachingTaskHandler: Consumer<OfflineSource>,
    private val onRemoveCacheTaskHandler: Consumer<OfflineSource>,
    private val onPlaySourceHandler: Consumer<OfflineSource>
) : RecyclerView.Adapter<OfflineSourceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewBinding = LayoutOfflineSourceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(viewBinding)
    }

    override fun getItemCount(): Int = offlineSources.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(offlineSources[position])
    }

    internal inner class ViewHolder(private val viewBinding: LayoutOfflineSourceBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        private val context: Context = viewBinding.root.context

        fun bind(offlineSource: OfflineSource) {
            viewBinding.posterImageView.setImageResource(offlineSource.imageResId)
            viewBinding.titleTextView.text = offlineSource.title
            viewBinding.startButton.setOnClickListener {
                onStartCachingTaskHandler.accept(offlineSource)
            }
            viewBinding.pauseButton.setOnClickListener {
                onPauseCachingTaskHandler.accept(offlineSource)
            }
            viewBinding.removeButton.setOnClickListener {
                onRemoveCacheTaskHandler.accept(offlineSource)
            }
            viewBinding.container.setOnClickListener {
                onPlaySourceHandler.accept(offlineSource)
            }
            offlineSource.isStateUpToDate.observe(context as LifecycleOwner) { isUpToDate: Boolean? ->
                handleStateUpToDateChange(isUpToDate)
            }
            handleStateUpToDateChange(offlineSource.isStateUpToDate.value)
            offlineSource.cachingTaskProgress.observe((context as LifecycleOwner)) { progress: Double? ->
                handleProgressChange(progress)
            }
            handleProgressChange(offlineSource.cachingTaskProgress.value)
            offlineSource.cachingTaskStatus.observe((context as LifecycleOwner)) { status: CachingTaskStatus? ->
                handleStatusChange(status)
            }
            handleStatusChange(offlineSource.cachingTaskStatus.value)
            offlineSource.cachingTaskSizeText.observe((context as LifecycleOwner)) { sizeText: String? ->
                handleSizeTextChange(sizeText)
            }
            handleSizeTextChange(offlineSource.cachingTaskSizeText.value)
        }

        private fun handleStateUpToDateChange(isUpToDate: Boolean?) {
            val upToDate = isUpToDate ?: true
            viewBinding.startButton.isEnabled = upToDate
            viewBinding.pauseButton.isEnabled = upToDate
            viewBinding.removeButton.isEnabled = upToDate
            viewBinding.loadingSpinner.visibility =
                if (upToDate) View.GONE else View.VISIBLE
        }

        private fun handleProgressChange(progress: Double?) {
            val progressInt = ((progress ?: 0.0) * 100).roundToInt()
            viewBinding.progressBar.progress = progressInt
            viewBinding.progressTextView.text = context.getString(
                R.string.progressLabel,
                progressInt
            )
        }

        private fun handleSizeTextChange(sizeText: String?) {
            if (sizeText != null) {
                viewBinding.sizeOverlayTextView.text = sizeText
                viewBinding.sizeOverlayTextView.visibility = View.VISIBLE
            } else {
                viewBinding.sizeOverlayTextView.visibility = View.GONE
            }
        }

        private fun handleStatusChange(status: CachingTaskStatus?) {
            val currentStatus = status ?: CachingTaskStatus.EVICTED
            when (currentStatus) {
                CachingTaskStatus.IDLE -> {
                    viewBinding.startButton.visibility = View.VISIBLE
                    viewBinding.pauseButton.visibility = View.GONE
                    viewBinding.removeButton.visibility = View.VISIBLE
                    viewBinding.progressBar.visibility = View.VISIBLE
                    viewBinding.progressTextView.visibility = View.VISIBLE
                    viewBinding.container.strokeColor = 0
                }
                CachingTaskStatus.LOADING -> {
                    viewBinding.startButton.visibility = View.GONE
                    viewBinding.pauseButton.visibility = View.VISIBLE
                    viewBinding.removeButton.visibility = View.VISIBLE
                    viewBinding.progressBar.visibility = View.VISIBLE
                    viewBinding.progressTextView.visibility = View.VISIBLE
                    viewBinding.container.strokeColor = 0
                }
                CachingTaskStatus.DONE -> {
                    viewBinding.startButton.visibility = View.GONE
                    viewBinding.pauseButton.visibility = View.GONE
                    viewBinding.removeButton.visibility = View.VISIBLE
                    viewBinding.progressBar.visibility = View.VISIBLE
                    viewBinding.progressTextView.visibility = View.VISIBLE
                    viewBinding.container.strokeColor = 0
                }
                CachingTaskStatus.ERROR -> {
                    viewBinding.startButton.visibility = View.GONE
                    viewBinding.pauseButton.visibility = View.GONE
                    viewBinding.removeButton.visibility = View.VISIBLE
                    viewBinding.progressBar.visibility = View.VISIBLE
                    viewBinding.progressTextView.visibility = View.VISIBLE
                    viewBinding.container.strokeColor =
                        ContextCompat.getColor(context, R.color.dolbyError)
                }
                CachingTaskStatus.EVICTED -> {
                    viewBinding.startButton.visibility = View.VISIBLE
                    viewBinding.pauseButton.visibility = View.GONE
                    viewBinding.removeButton.visibility = View.GONE
                    viewBinding.progressBar.visibility = View.GONE
                    viewBinding.progressTextView.visibility = View.GONE
                    viewBinding.sizeOverlayTextView.visibility = View.GONE
                    viewBinding.container.strokeColor = 0
                }
            }
        }
    }
}
