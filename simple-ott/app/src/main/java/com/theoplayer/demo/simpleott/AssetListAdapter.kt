package com.theoplayer.demo.simpleott

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import com.theoplayer.demo.simpleott.databinding.ListItemBinding
import com.theoplayer.demo.simpleott.datamodel.AssetItem

internal class AssetListAdapter(
    private val context: Activity,
    private val items: Array<AssetItem>
) : ArrayAdapter<AssetItem?>(context, R.layout.list_item, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val binding: ListItemBinding
        if (convertView == null) {
            binding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.list_item, parent, false
            )
            convertView = binding.root
        } else {
            binding = convertView.tag as ListItemBinding
        }
        items[position].imageId =
            if (items[position].imageUrl != null) context.resources.getIdentifier(
                items[position].imageUrl, "drawable", context.packageName
            ) else R.drawable.live
        binding.viewmodel = items[position]
        return convertView
    }
}