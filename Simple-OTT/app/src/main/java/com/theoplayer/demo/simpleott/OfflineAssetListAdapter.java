package com.theoplayer.demo.simpleott;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.databinding.DataBindingUtil;

import com.theoplayer.android.api.cache.CachingTaskStatus;
import com.theoplayer.demo.simpleott.databinding.OfflineListItemBinding;
import com.theoplayer.demo.simpleott.datamodel.OfflineSource;


public class OfflineAssetListAdapter extends ArrayAdapter<OfflineSource> {

    private final Typeface fontAwesome;
    private final Activity context;
    private OfflineSource[] items;
    private OfflineHandler offlineHandler;

    public OfflineAssetListAdapter(AppCompatActivity context,
                                   OfflineSource[] items,
                                   OfflineHandler offlineHandler) {
        super(context, R.layout.list_item, items);
        this.items = items;
        this.context = context;
        this.offlineHandler = offlineHandler;
        fontAwesome = Typeface.createFromAsset(context.getAssets(), "fa.otf");
    }

    @NonNull
    @Override
    public View getView(int position, View rowView, @NonNull ViewGroup parent) {
        OfflineListItemBinding binding;

        if (rowView == null) {
            binding = DataBindingUtil.inflate(
                    LayoutInflater.from(getContext()),
                    R.layout.offline_list_item, parent, false);
            rowView = binding.getRoot();
        } else {
            binding = (OfflineListItemBinding) rowView.getTag();
        }

        rowView.setClickable(true);
        rowView.setOnClickListener(v -> items[position].play(this.getContext()));

        // Clicking on progress bar should either start or resume the task
        binding.progressBar.setOnClickListener(v -> {
            if (items[position].getCachingTaskStatus() == CachingTaskStatus.LOADING) {
                items[position].pauseCachingTask();
            } else {
                items[position].startCachingTask();
            }
        });

        binding.downloadButton.setTypeface(fontAwesome);
        binding.downloadButton.setOnClickListener(v -> offlineHandler.startCachingTaskHandler(items[position]));

        binding.pauseButton.setTypeface(fontAwesome);
        binding.pauseButton.setOnClickListener(v -> offlineHandler.startCachingTaskHandler(items[position]));

        binding.deleteButton.setTypeface(fontAwesome);
        binding.deleteButton.setOnClickListener(v -> offlineHandler.removeCachingTaskHandler(items[position]));

        items[position].imageId = items[position].imageUrl != null ?
                context.getResources().getIdentifier(items[position].imageUrl, "drawable", context.getPackageName()) :
                R.drawable.live;

        binding.setViewmodel(items[position]);

        AppCompatActivity lifeCycleOwner = (AppCompatActivity) context;

        // Updating UI state based on the task status
        items[position].getCachingTaskStatusLiveData().observe(lifeCycleOwner, cachingTaskStatus -> updateUiState(binding, cachingTaskStatus));
        // Updating the progress bar based on the task progress
        items[position].getCachingTaskProgressLiveData().observe(lifeCycleOwner, progress -> updateProgress(binding, progress));
        // Enabling/disabling UI preventing multiple clicks when the task is being processed
        items[position].getUiEnabledLiveData().observe(lifeCycleOwner, progress -> toggleUi(binding, progress));

        return rowView;
    }

    private void updateProgress(OfflineListItemBinding binding, double pr) {
        int progress = (int) Math.ceil(pr * 100);
        if (progress == 100) {
            binding.progressBar.setProgress(0);
        } else {
            binding.progressBar.setProgress(progress);
        }
        binding.progressPercent.setText(String.format(context.getString(R.string.progress), progress));
    }

    private void toggleUi(OfflineListItemBinding binding, Boolean enabled) {
        binding.downloadButton.setEnabled(enabled);
        binding.deleteButton.setEnabled(enabled);
        binding.pauseButton.setEnabled(enabled);
        binding.progressBar.setEnabled(enabled);
    }

    private void updateUiState(OfflineListItemBinding binding, CachingTaskStatus status) {
        AppCompatButton downloadBtn = binding.downloadButton;
        AppCompatButton deleteBtn = binding.deleteButton;
        AppCompatButton pauseBtn = binding.pauseButton;
        TextView progressTxt = binding.progressPercent;
        switch (status) {
            case DONE:
                downloadBtn.setVisibility(View.GONE);
                progressTxt.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.GONE);
                deleteBtn.setVisibility(View.VISIBLE);
                break;
            case LOADING:
                downloadBtn.setVisibility(View.GONE);
                deleteBtn.setVisibility(View.GONE);
                progressTxt.setVisibility(View.VISIBLE);
                pauseBtn.setVisibility(View.GONE);
                break;
            case IDLE:
                pauseBtn.setVisibility(View.VISIBLE);
                downloadBtn.setVisibility(View.GONE);
                progressTxt.setVisibility(View.GONE);
                deleteBtn.setVisibility(View.GONE);
                break;
            case EVICTED:
            case ERROR:
            default:
                pauseBtn.setVisibility(View.GONE);
                downloadBtn.setVisibility(View.VISIBLE);
                deleteBtn.setVisibility(View.GONE);
                progressTxt.setVisibility(View.GONE);
                break;
        }
    }
}
