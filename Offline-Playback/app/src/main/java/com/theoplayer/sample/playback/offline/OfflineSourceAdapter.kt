package com.theoplayer.sample.playback.offline;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.theoplayer.android.api.cache.CachingTaskStatus;
import com.theoplayer.sample.playback.offline.databinding.LayoutOfflineSourceBinding;

import java.io.InputStream;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

class OfflineSourceAdapter extends RecyclerView.Adapter<OfflineSourceAdapter.ViewHolder> {

    private List<OfflineSource> offlineSources;

    private Consumer<OfflineSource> onStartCachingTaskHandler;
    private Consumer<OfflineSource> onPauseCachingTaskHandler;
    private Consumer<OfflineSource> onRemoveCacheTaskHandler;
    private Consumer<OfflineSource> onPlaySourceHandler;

    public OfflineSourceAdapter(@NonNull List<OfflineSource> offlineSources,
                                @NonNull Consumer<OfflineSource> onStartCachingTaskHandler,
                                @NonNull Consumer<OfflineSource> onPauseCachingTaskHandler,
                                @NonNull Consumer<OfflineSource> onRemoveCacheTaskHandler,
                                @NonNull Consumer<OfflineSource> onPlaySourceHandler) {
        this.offlineSources = offlineSources;
        this.onStartCachingTaskHandler = onStartCachingTaskHandler;
        this.onPauseCachingTaskHandler = onPauseCachingTaskHandler;
        this.onRemoveCacheTaskHandler = onRemoveCacheTaskHandler;
        this.onPlaySourceHandler = onPlaySourceHandler;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutOfflineSourceBinding viewBinding = LayoutOfflineSourceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(viewBinding);
    }

    @Override
    public int getItemCount() {
        return offlineSources.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.bind(offlineSources.get(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private LayoutOfflineSourceBinding viewBinding;
        private Context context;

        ViewHolder(LayoutOfflineSourceBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
            this.context = viewBinding.getRoot().getContext();
        }

        void bind(OfflineSource offlineSource) {
            // View tag is used to determine if requested poster already loaded and shown.
            // If so, there's no need to reload it.
            if (!offlineSource.getPoster().equals(viewBinding.posterImageView.getTag())) {
                try (InputStream posterInputStream = context.getAssets().open(offlineSource.getPoster())) {
                    Drawable posterDrawable = Drawable.createFromStream(posterInputStream, null);
                    viewBinding.posterImageView.setImageDrawable(posterDrawable);
                    viewBinding.posterImageView.setTag(offlineSource.getPoster());
                } catch (Exception ignore) {
                    // Displaying poster placeholder in case of any problems with loading requested poster.
                    viewBinding.posterImageView.setImageResource(R.mipmap.ic_launcher);
                }
            }

            viewBinding.titleTextView.setText(offlineSource.getTitle());
            viewBinding.startButton.setOnClickListener(button -> onStartCachingTaskHandler.accept(offlineSource));
            viewBinding.pauseButton.setOnClickListener(button -> onPauseCachingTaskHandler.accept(offlineSource));
            viewBinding.removeButton.setOnClickListener(button -> onRemoveCacheTaskHandler.accept(offlineSource));
            viewBinding.container.setOnClickListener(view -> onPlaySourceHandler.accept(offlineSource));

            offlineSource.isStateUpToDate().observe((LifecycleOwner) context, this::handleStateUpToDateChange);
            handleStateUpToDateChange(offlineSource.isStateUpToDate().getValue());

            offlineSource.getCachingTaskProgress().observe((LifecycleOwner) context, this::handleProgressChange);
            handleProgressChange(offlineSource.getCachingTaskProgress().getValue());

            offlineSource.getCachingTaskStatus().observe((LifecycleOwner) context, this::handleStatusChange);
            handleStatusChange(offlineSource.getCachingTaskStatus().getValue());
        }

        private void handleStateUpToDateChange(Boolean isUpToDate) {
            isUpToDate = isUpToDate == null ? true : isUpToDate;
            viewBinding.startButton.setEnabled(isUpToDate);
            viewBinding.pauseButton.setEnabled(isUpToDate);
            viewBinding.removeButton.setEnabled(isUpToDate);
            viewBinding.loadingSpinner.setVisibility(isUpToDate ? GONE : VISIBLE);
        }

        private void handleProgressChange(Double progress) {
            progress = progress == null ? 0.0D : progress;
            int progressInt = (int) Math.round(progress * 100);
            viewBinding.progressBar.setProgress(progressInt);
            viewBinding.progressTextView.setText(context.getString(R.string.progressLabel, progressInt));
        }

        private void handleStatusChange(CachingTaskStatus status) {
            status = status == null ? CachingTaskStatus.EVICTED : status;
            switch (status) {
                case IDLE:
                    viewBinding.startButton.setVisibility(VISIBLE);
                    viewBinding.pauseButton.setVisibility(GONE);
                    viewBinding.removeButton.setVisibility(VISIBLE);
                    viewBinding.progressBar.setVisibility(VISIBLE);
                    viewBinding.progressTextView.setVisibility(VISIBLE);
                    viewBinding.container.setStrokeColor(0);
                    break;
                case LOADING:
                    viewBinding.startButton.setVisibility(GONE);
                    viewBinding.pauseButton.setVisibility(VISIBLE);
                    viewBinding.removeButton.setVisibility(VISIBLE);
                    viewBinding.progressBar.setVisibility(VISIBLE);
                    viewBinding.progressTextView.setVisibility(VISIBLE);
                    viewBinding.container.setStrokeColor(0);
                    break;
                case DONE:
                    viewBinding.startButton.setVisibility(GONE);
                    viewBinding.pauseButton.setVisibility(GONE);
                    viewBinding.removeButton.setVisibility(VISIBLE);
                    viewBinding.progressBar.setVisibility(VISIBLE);
                    viewBinding.progressTextView.setVisibility(VISIBLE);
                    viewBinding.container.setStrokeColor(0);
                    break;
                case ERROR:
                    viewBinding.startButton.setVisibility(GONE);
                    viewBinding.pauseButton.setVisibility(GONE);
                    viewBinding.removeButton.setVisibility(VISIBLE);
                    viewBinding.progressBar.setVisibility(VISIBLE);
                    viewBinding.progressTextView.setVisibility(VISIBLE);
                    viewBinding.container.setStrokeColor(context.getResources().getColor(R.color.theoError));
                    break;
                case EVICTED:
                    viewBinding.startButton.setVisibility(VISIBLE);
                    viewBinding.pauseButton.setVisibility(GONE);
                    viewBinding.removeButton.setVisibility(GONE);
                    viewBinding.progressBar.setVisibility(GONE);
                    viewBinding.progressTextView.setVisibility(GONE);
                    viewBinding.container.setStrokeColor(0);
                    break;
            }
        }
    }

}
