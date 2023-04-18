package com.theoplayer.sample.playback.offline

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.theoplayer.android.api.cache.CachingTaskStatus
import com.theoplayer.sample.playback.offline.databinding.LayoutOfflineSourceBinding

internal class OfflineSourceAdapter(
    private val offlineSources: List<OfflineSource?>,
    private val onStartCachingTaskHandler: Consumer<OfflineSource?>,
    private val onPauseCachingTaskHandler: Consumer<OfflineSource?>,
    private val onRemoveCacheTaskHandler: Consumer<OfflineSource?>,
    private val onPlaySourceHandler: Consumer<OfflineSource?>
) : RecyclerView.Adapter<OfflineSourceAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewBinding = LayoutOfflineSourceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return offlineSources.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(offlineSources[position])
    }

    internal inner class ViewHolder(private val viewBinding: LayoutOfflineSourceBinding) :
        RecyclerView.ViewHolder(
            viewBinding.root
        ) {
        private val context: Context

        init {
            context = viewBinding.root.context
        }

        fun bind(offlineSource: OfflineSource?) {
            // View tag is used to determine if requested poster already loaded and shown.
            // If so, there's no need to reload it.
            if (offlineSource?.poster != viewBinding.posterImageView.tag) {
                try {
                    context.assets.open(offlineSource!!.poster).use { posterInputStream ->
                        val posterDrawable = Drawable.createFromStream(posterInputStream, null)
                        viewBinding.posterImageView.setImageDrawable(posterDrawable)
                        viewBinding.posterImageView.tag = offlineSource.poster
                    }
                } catch (ignore: Exception) {
                    // Displaying poster placeholder in case of any problems with loading requested poster.
                    viewBinding.posterImageView.setImageResource(R.mipmap.ic_launcher)
                }
            }
            viewBinding.titleTextView.text = offlineSource?.title
            viewBinding.startButton.setOnClickListener { button: View? ->
                onStartCachingTaskHandler.accept(
                    offlineSource
                )
            }
            viewBinding.pauseButton.setOnClickListener { button: View? ->
                onPauseCachingTaskHandler.accept(
                    offlineSource
                )
            }
            viewBinding.removeButton.setOnClickListener { button: View? ->
                onRemoveCacheTaskHandler.accept(
                    offlineSource
                )
            }
            viewBinding.container.setOnClickListener { view: View? ->
                onPlaySourceHandler.accept(
                    offlineSource
                )
            }
            offlineSource!!.isStateUpToDate.observe(context as LifecycleOwner) { isUpToDate: Boolean? ->
                handleStateUpToDateChange(
                    isUpToDate
                )
            }
            handleStateUpToDateChange(offlineSource.isStateUpToDate.value)
            offlineSource.cachingTaskProgress.observe((context as LifecycleOwner)) { progress: Double? ->
                handleProgressChange(
                    progress
                )
            }
            handleProgressChange(offlineSource.cachingTaskProgress.value)
            offlineSource.cachingTaskStatus.observe((context as LifecycleOwner)) { status: CachingTaskStatus? ->
                handleStatusChange(
                    status
                )
            }
            handleStatusChange(offlineSource.cachingTaskStatus.value)
        }

        private fun handleStateUpToDateChange(isUpToDate: Boolean?) {
            var isUpToDate = isUpToDate
            isUpToDate = isUpToDate ?: true
            viewBinding.startButton.isEnabled = isUpToDate
            viewBinding.pauseButton.isEnabled = isUpToDate
            viewBinding.removeButton.isEnabled = isUpToDate
            viewBinding.loadingSpinner.visibility =
                if (isUpToDate) View.GONE else View.VISIBLE
        }

        private fun handleProgressChange(progress: Double?) {
            var progress = progress
            progress = progress ?: 0.0
            val progressInt = Math.round(progress * 100).toInt()
            viewBinding.progressBar.progress = progressInt
            viewBinding.progressTextView.text = context.getString(
                R.string.progressLabel,
                progressInt
            )
        }

        private fun handleStatusChange(status: CachingTaskStatus?) {
            var status = status
            status = status ?: CachingTaskStatus.EVICTED
            when (status) {
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
                        context.resources.getColor(R.color.theoError)
                }
                CachingTaskStatus.EVICTED -> {
                    viewBinding.startButton.visibility = View.VISIBLE
                    viewBinding.pauseButton.visibility = View.GONE
                    viewBinding.removeButton.visibility = View.GONE
                    viewBinding.progressBar.visibility = View.GONE
                    viewBinding.progressTextView.visibility = View.GONE
                    viewBinding.container.strokeColor = 0
                }
            }
        }
    }
}