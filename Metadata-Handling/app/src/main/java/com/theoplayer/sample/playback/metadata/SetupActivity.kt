package com.theoplayer.sample.playback.metadata;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.theoplayer.sample.playback.metadata.databinding.ActivitySetupBinding;
import com.theoplayer.sample.playback.metadata.databinding.LayoutMetadataBinding;


public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TheoTheme_Base);
        super.onCreate(savedInstanceState);

        // Inflating view and obtaining an instance of the binding class.
        ActivitySetupBinding viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup);

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar);

        // Configure UI behavior and default values.
        viewBinding.metadataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        viewBinding.metadataRecyclerView.setAdapter(new MetadataAdapter(new int[]{
                R.string.hlsWithID3MetadataName,
                R.string.hlsWithProgramDateTimeMetadataName,
                R.string.hlsWithDateRangeMetadataName,
                R.string.dashWithEmsgMetadataName,
                R.string.dashWithEventStreamMetadataName
        }));
    }


    private class MetadataAdapter extends RecyclerView.Adapter<MetadataAdapter.ViewHolder> {

        private int[] metadataIds;

        MetadataAdapter(int[] metadataIds) {
            this.metadataIds = metadataIds;
        }

        @Override
        public int getItemCount() {
            return metadataIds.length;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutMetadataBinding viewBinding = LayoutMetadataBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(viewBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            int metadataId = metadataIds[position];
            String metadataName = viewHolder.itemView.getContext().getString(metadataId);

            viewHolder.viewBinding.metadataNameTextView.setText(metadataName);
            viewHolder.itemView.setOnClickListener(view -> PlayerActivity.play(view.getContext(), metadataId));
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            LayoutMetadataBinding viewBinding;

            ViewHolder(LayoutMetadataBinding viewBinding) {
                super(viewBinding.getRoot());
                this.viewBinding = viewBinding;
            }
        }
    }
}
