package com.theoplayer.demo.simpleott.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.theoplayer.android.api.cache.CachingTaskStatus;
import com.theoplayer.demo.simpleott.PlayerActivity;
import com.theoplayer.demo.simpleott.R;
import com.theoplayer.demo.simpleott.databinding.LayoutOfflineSourceBinding;
import com.theoplayer.demo.simpleott.model.OfflineSource;
import com.theoplayer.demo.simpleott.model.OfflineSourceDownloader;

import static android.view.LayoutInflater.from;

public class OfflineSourceAdapter extends ArrayAdapter<OfflineSource> {

    private OfflineSourceDownloader offlineSourceDownloader;

    public OfflineSourceAdapter(Context context, OfflineSourceDownloader offlineSourceDownloader) {
        super(context, 0);
        this.offlineSourceDownloader = offlineSourceDownloader;
        addAll(offlineSourceDownloader.getOfflineSources());
    }

    @NonNull
    @Override
    public View getView(int position, View rowView, @NonNull ViewGroup parent) {
        LayoutOfflineSourceBinding binding;
        if (rowView == null) {
            binding = LayoutOfflineSourceBinding.inflate(from(getContext()), parent, false);
            rowView = binding.getRoot();
            rowView.setTag(binding);
        } else {
            binding = (LayoutOfflineSourceBinding) rowView.getTag();
        }

        OfflineSource offlineSource = getItem(position);

        if (offlineSource != null) {
            rowView.setClickable(true);
            rowView.setOnClickListener(v -> PlayerActivity.play(getContext(), offlineSource));

            // Clicking on progress bar should either start or resume the task
            binding.startButton.setOnClickListener(v -> offlineSourceDownloader.startCachingTask(offlineSource));
            binding.resumeButton.setOnClickListener(v -> offlineSourceDownloader.startCachingTask(offlineSource));
            binding.progressPercent.setOnClickListener(v -> offlineSourceDownloader.pauseCachingTask(offlineSource));
            binding.removeButton.setOnClickListener(v -> offlineSourceDownloader.removeCachingTask(offlineSource));

            binding.setViewModel(offlineSource);

            // Enabling/disabling download buttons preventing multiple clicks when the task is being processed
            offlineSource.isStateUpToDate().observe((LifecycleOwner) getContext(),
                    isUpToDate -> onStateUpToDateChange(binding, isUpToDate));

            // Updating the progress info based on the task progress
            offlineSource.getCachingTaskProgress().observe((LifecycleOwner) getContext(),
                    progress -> onCachingTaskProgressChange(binding, progress));

            // Updating download buttons visibility based on the task status
            offlineSource.getCachingTaskStatus().observe((LifecycleOwner) getContext(),
                    cachingTaskStatus -> onCachingTaskStatusChange(binding, cachingTaskStatus));
        }
        return rowView;
    }

    private void onStateUpToDateChange(LayoutOfflineSourceBinding binding, Boolean isUpToDate) {
        binding.startButton.setEnabled(isUpToDate);
        binding.progressPercent.setEnabled(isUpToDate);
        binding.resumeButton.setEnabled(isUpToDate);
        binding.removeButton.setEnabled(isUpToDate);
    }

    private void onCachingTaskProgressChange(LayoutOfflineSourceBinding binding, Double progress) {
        progress = progress == null ? 0.0D : progress;
        int progressInt = (int) Math.round(progress * 100);
        binding.progressBar.setProgress(progressInt);
        binding.progressPercent.setText(getContext().getString(R.string.progress, progressInt));
    }

    private void onCachingTaskStatusChange(LayoutOfflineSourceBinding binding, CachingTaskStatus status) {
        switch (status) {
            case DONE:
                binding.startButton.setVisibility(View.GONE);
                binding.progressPercent.setVisibility(View.GONE);
                binding.resumeButton.setVisibility(View.GONE);
                binding.removeButton.setVisibility(View.VISIBLE);
                break;
            case LOADING:
                binding.startButton.setVisibility(View.GONE);
                binding.progressPercent.setVisibility(View.VISIBLE);
                binding.resumeButton.setVisibility(View.GONE);
                binding.removeButton.setVisibility(View.GONE);
                break;
            case IDLE:
                binding.startButton.setVisibility(View.GONE);
                binding.progressPercent.setVisibility(View.GONE);
                binding.resumeButton.setVisibility(View.VISIBLE);
                binding.removeButton.setVisibility(View.GONE);
                break;
            case EVICTED:
            case ERROR:
            default:
                binding.startButton.setVisibility(View.VISIBLE);
                binding.progressPercent.setVisibility(View.GONE);
                binding.resumeButton.setVisibility(View.GONE);
                binding.removeButton.setVisibility(View.GONE);
                break;
        }
    }
}
