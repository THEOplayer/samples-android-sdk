package com.theoplayer.demo.simpleott.view

import android.content.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.LifecycleOwner
import com.theoplayer.android.api.cache.CachingTaskStatus
import com.theoplayer.demo.simpleott.*
import com.theoplayer.demo.simpleott.databinding.LayoutOfflineSourceBinding
import com.theoplayer.demo.simpleott.model.OfflineSource
import com.theoplayer.demo.simpleott.model.OfflineSourceDownloader

class OfflineSourceAdapter(
    context: Context?,
    private val offlineSourceDownloader: OfflineSourceDownloader?
) : ArrayAdapter<OfflineSource?>(
    context!!, 0
) {
    init {
        addAll(offlineSourceDownloader!!.offlineSources)
    }

    override fun getView(position: Int, rowView: View?, parent: ViewGroup): View {
        var rowView = rowView
        val binding: LayoutOfflineSourceBinding
        if (rowView == null) {
            binding = LayoutOfflineSourceBinding.inflate(
                LayoutInflater.from(
                    context
                ), parent, false
            )
            rowView = binding.root
            rowView.tag = binding
        } else {
            binding = rowView.tag as LayoutOfflineSourceBinding
        }
        val offlineSource = getItem(position)
        if (offlineSource != null) {
            rowView.isClickable = true
            rowView.setOnClickListener(View.OnClickListener { v: View? ->
                PlayerActivity.Companion.play(
                    context, offlineSource
                )
            })

            // Clicking on progress bar should either start or resume the task
            binding.startButton.setOnClickListener { v: View? ->
                offlineSourceDownloader!!.startCachingTask(
                    offlineSource
                )
            }
            binding.resumeButton.setOnClickListener { v: View? ->
                offlineSourceDownloader!!.startCachingTask(
                    offlineSource
                )
            }
            binding.progressPercent.setOnClickListener { v: View? ->
                offlineSourceDownloader!!.pauseCachingTask(
                    offlineSource
                )
            }
            binding.removeButton.setOnClickListener { v: View? ->
                offlineSourceDownloader!!.removeCachingTask(
                    offlineSource
                )
            }
            binding.viewModel = offlineSource

            // Enabling/disabling download buttons preventing multiple clicks when the task is being processed
            offlineSource.isStateUpToDate.observe(
                (context as LifecycleOwner)
            ) { isUpToDate: Boolean? -> onStateUpToDateChange(binding, isUpToDate) }

            // Updating the progress info based on the task progress
            offlineSource.cachingTaskProgress.observe(
                (context as LifecycleOwner)
            ) { progress: Double? -> onCachingTaskProgressChange(binding, progress) }

            // Updating download buttons visibility based on the task status
            offlineSource.cachingTaskStatus.observe(
                (context as LifecycleOwner)
            ) { cachingTaskStatus: CachingTaskStatus? ->
                onCachingTaskStatusChange(
                    binding,
                    cachingTaskStatus
                )
            }
        }
        return rowView
    }

    private fun onStateUpToDateChange(binding: LayoutOfflineSourceBinding, isUpToDate: Boolean?) {
        binding.startButton.isEnabled = isUpToDate!!
        binding.progressPercent.isEnabled = isUpToDate
        binding.resumeButton.isEnabled = isUpToDate
        binding.removeButton.isEnabled = isUpToDate
    }

    private fun onCachingTaskProgressChange(
        binding: LayoutOfflineSourceBinding,
        progress: Double?
    ) {
        var progress = progress
        progress = progress ?: 0.0
        val progressInt = Math.round(progress * 100).toInt()
        binding.progressBar.progress = progressInt
        binding.progressPercent.text =
            context.getString(R.string.progress, progressInt)
    }

    private fun onCachingTaskStatusChange(
        binding: LayoutOfflineSourceBinding,
        status: CachingTaskStatus?
    ) {
        when (status) {
            CachingTaskStatus.DONE -> {
                binding.startButton.visibility = View.GONE
                binding.progressPercent.visibility = View.GONE
                binding.resumeButton.visibility = View.GONE
                binding.removeButton.visibility = View.VISIBLE
            }
            CachingTaskStatus.LOADING -> {
                binding.startButton.visibility = View.GONE
                binding.progressPercent.visibility = View.VISIBLE
                binding.resumeButton.visibility = View.GONE
                binding.removeButton.visibility = View.GONE
            }
            CachingTaskStatus.IDLE -> {
                binding.startButton.visibility = View.GONE
                binding.progressPercent.visibility = View.GONE
                binding.resumeButton.visibility = View.VISIBLE
                binding.removeButton.visibility = View.GONE
            }
            CachingTaskStatus.EVICTED, CachingTaskStatus.ERROR -> {
                binding.startButton.visibility = View.VISIBLE
                binding.progressPercent.visibility = View.GONE
                binding.resumeButton.visibility = View.GONE
                binding.removeButton.visibility = View.GONE
            }
            else -> {
                binding.startButton.visibility = View.VISIBLE
                binding.progressPercent.visibility = View.GONE
                binding.resumeButton.visibility = View.GONE
                binding.removeButton.visibility = View.GONE
            }
        }
    }
}