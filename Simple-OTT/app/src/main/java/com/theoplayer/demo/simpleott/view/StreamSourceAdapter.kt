package com.theoplayer.demo.simpleott.view

import android.content.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.theoplayer.demo.simpleott.PlayerActivity
import com.theoplayer.demo.simpleott.databinding.LayoutStreamSourceBinding
import com.theoplayer.demo.simpleott.model.StreamSource

class StreamSourceAdapter(context: Context?, streamSources: List<StreamSource?>?) :
    ArrayAdapter<StreamSource?>(
        context!!, 0
    ) {
    init {
        addAll(streamSources!!)
    }

    override fun getView(position: Int, rowView: View?, parent: ViewGroup): View {
        var rowView = rowView
        val binding: LayoutStreamSourceBinding
        if (rowView == null) {
            binding = LayoutStreamSourceBinding.inflate(
                LayoutInflater.from(
                    context
                ), parent, false
            )
            rowView = binding.root
            rowView.tag = binding
        } else {
            binding = rowView.tag as LayoutStreamSourceBinding
        }
        val streamSource = getItem(position)
        if (streamSource != null) {
            rowView.setOnClickListener(View.OnClickListener { v: View? ->
                PlayerActivity.Companion.play(
                    context, streamSource
                )
            })
            binding.viewModel = streamSource
        }
        return rowView
    }
}