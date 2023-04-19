package com.theoplayer.demo.simpleott

import android.app.Activity
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.cache.CachingTaskStatus
import com.theoplayer.demo.simpleott.databinding.OfflineListItemBinding
import com.theoplayer.demo.simpleott.datamodel.OfflineSource

class OfflineAssetListAdapter(
    context: AppCompatActivity,
    private val items: Array<OfflineSource>,
    offlineHandler: OfflineHandler
) : ArrayAdapter<OfflineSource?>(context, R.layout.list_item, items) {
    private val fontAwesome: Typeface
    private val context: Activity
    private val offlineHandler: OfflineHandler

    init {
        this.context = context
        this.offlineHandler = offlineHandler
        fontAwesome = Typeface.createFromAsset(context.assets, "fa.otf")
    }

    override fun getView(position: Int, rowView: View?, parent: ViewGroup): View {
        var rowView = rowView
        val binding: OfflineListItemBinding
        if (rowView == null) {
            binding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.offline_list_item, parent, false
            )
            rowView = binding.root
        } else {
            binding = rowView.tag as OfflineListItemBinding
        }
        rowView.isClickable = true
        rowView.setOnClickListener(View.OnClickListener { v: View? ->
            items[position].play(
                getContext()
            )
        })

        // Clicking on progress bar should either start or resume the task
        binding.progressBar.setOnClickListener { v: View? ->
            if (items[position].cachingTaskStatus == CachingTaskStatus.LOADING) {
                items[position].pauseCachingTask()
            } else {
                items[position].startCachingTask()
            }
        }
        binding.downloadButton.typeface = fontAwesome
        binding.downloadButton.setOnClickListener { v: View? ->
            offlineHandler.startCachingTaskHandler(
                items[position]
            )
        }
        binding.pauseButton.typeface = fontAwesome
        binding.pauseButton.setOnClickListener { v: View? ->
            offlineHandler.startCachingTaskHandler(
                items[position]
            )
        }
        binding.deleteButton.typeface = fontAwesome
        binding.deleteButton.setOnClickListener { v: View? ->
            offlineHandler.removeCachingTaskHandler(
                items[position]
            )
        }
        items[position].imageId =
            if (items[position].imageUrl != null) context.resources.getIdentifier(
                items[position].imageUrl, "drawable", context.packageName
            ) else R.drawable.live
        binding.viewmodel = items[position]
        val lifeCycleOwner = context as AppCompatActivity

        // Updating UI state based on the task status
        items[position].cachingTaskStatusLiveData.observe(lifeCycleOwner) { cachingTaskStatus: CachingTaskStatus? ->
            updateUiState(
                binding,
                cachingTaskStatus
            )
        }
        // Updating the progress bar based on the task progress
        items[position].cachingTaskProgressLiveData.observe(lifeCycleOwner) { progress: Double ->
            updateProgress(
                binding,
                progress
            )
        }
        // Enabling/disabling UI preventing multiple clicks when the task is being processed
        items[position].uiEnabledLiveData.observe(lifeCycleOwner) { progress: Boolean? ->
            toggleUi(
                binding,
                progress
            )
        }
        return rowView
    }

    private fun updateProgress(binding: OfflineListItemBinding, pr: Double) {
        val progress = Math.ceil(pr * 100).toInt()
        if (progress == 100) {
            binding.progressBar.progress = 0
        } else {
            binding.progressBar.progress = progress
        }
        binding.progressPercent.text = String.format(
            context.getString(R.string.progress),
            progress
        )
    }

    private fun toggleUi(binding: OfflineListItemBinding, enabled: Boolean?) {
        binding.downloadButton.isEnabled = enabled!!
        binding.deleteButton.isEnabled = enabled
        binding.pauseButton.isEnabled = enabled
        binding.progressBar.isEnabled = enabled
    }

    private fun updateUiState(binding: OfflineListItemBinding, status: CachingTaskStatus?) {
        val downloadBtn = binding.downloadButton
        val deleteBtn = binding.deleteButton
        val pauseBtn = binding.pauseButton
        val progressTxt = binding.progressPercent
        when (status) {
            CachingTaskStatus.DONE -> {
                downloadBtn.visibility = View.GONE
                progressTxt.visibility = View.GONE
                pauseBtn.visibility = View.GONE
                deleteBtn.visibility = View.VISIBLE
            }
            CachingTaskStatus.LOADING -> {
                downloadBtn.visibility = View.GONE
                deleteBtn.visibility = View.GONE
                progressTxt.visibility = View.VISIBLE
                pauseBtn.visibility = View.GONE
            }
            CachingTaskStatus.IDLE -> {
                pauseBtn.visibility = View.VISIBLE
                downloadBtn.visibility = View.GONE
                progressTxt.visibility = View.GONE
                deleteBtn.visibility = View.GONE
            }
            CachingTaskStatus.EVICTED, CachingTaskStatus.ERROR -> {
                pauseBtn.visibility = View.GONE
                downloadBtn.visibility = View.VISIBLE
                deleteBtn.visibility = View.GONE
                progressTxt.visibility = View.GONE
            }
            else -> {
                pauseBtn.visibility = View.GONE
                downloadBtn.visibility = View.VISIBLE
                deleteBtn.visibility = View.GONE
                progressTxt.visibility = View.GONE
            }
        }
    }
}