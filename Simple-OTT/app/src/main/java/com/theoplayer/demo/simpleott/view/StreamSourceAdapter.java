package com.theoplayer.demo.simpleott.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.theoplayer.demo.simpleott.PlayerActivity;
import com.theoplayer.demo.simpleott.databinding.LayoutStreamSourceBinding;
import com.theoplayer.demo.simpleott.model.StreamSource;

import java.util.List;

import static android.view.LayoutInflater.from;

public class StreamSourceAdapter extends ArrayAdapter<StreamSource> {

    public StreamSourceAdapter(Context context, List<StreamSource> streamSources) {
        super(context, 0);
        addAll(streamSources);
    }

    @NonNull
    @Override
    public View getView(int position, View rowView, @NonNull ViewGroup parent) {
        LayoutStreamSourceBinding binding;
        if (rowView == null) {
            binding = LayoutStreamSourceBinding.inflate(from(getContext()), parent, false);
            rowView = binding.getRoot();
            rowView.setTag(binding);
        } else {
            binding = (LayoutStreamSourceBinding) rowView.getTag();
        }

        StreamSource streamSource = getItem(position);

        if (streamSource != null) {
            rowView.setOnClickListener(v -> PlayerActivity.play(getContext(), streamSource));
            binding.setViewModel(streamSource);
        }

        return rowView;
    }
}
