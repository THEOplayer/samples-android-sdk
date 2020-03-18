package com.theoplayer.demo.simpleott;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.theoplayer.demo.simpleott.databinding.ListItemBinding;
import com.theoplayer.demo.simpleott.datamodel.AssetItem;


class AssetListAdapter extends ArrayAdapter<AssetItem> {

    private final Activity context;
    private AssetItem[] items;

    public AssetListAdapter(Activity context,
                            AssetItem[] items) {
        super(context, R.layout.list_item, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ListItemBinding binding;
        if (convertView == null) {
            binding = DataBindingUtil.inflate(
                    LayoutInflater.from(getContext()),
                    R.layout.list_item, parent, false);
            convertView = binding.getRoot();
        } else {
            binding = (ListItemBinding) convertView.getTag();
        }

        items[position].imageId = items[position].imageUrl != null ?
                context.getResources().getIdentifier(items[position].imageUrl, "drawable", context.getPackageName()) :
                R.drawable.live;

        binding.setViewmodel(items[position]);
        return convertView;
    }
}
