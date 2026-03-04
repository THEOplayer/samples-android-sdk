package com.theoplayer.demo.simpleott.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.theoplayer.demo.simpleott.PlayerActivity
import com.theoplayer.demo.simpleott.databinding.LayoutStreamSourceBinding
import com.theoplayer.demo.simpleott.model.StreamSource

class StreamSourceAdapter(
    private val streamSources: List<StreamSource>
) : RecyclerView.Adapter<StreamSourceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewBinding = LayoutStreamSourceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(viewBinding)
    }

    override fun getItemCount(): Int = streamSources.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(streamSources[position])
    }

    inner class ViewHolder(private val viewBinding: LayoutStreamSourceBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {

        fun bind(streamSource: StreamSource) {
            viewBinding.sourceImageView.setImageResource(streamSource.imageResId)
            viewBinding.titleTextView.text = streamSource.title
            viewBinding.descriptionTextView.text = streamSource.description
            viewBinding.container.setOnClickListener {
                PlayerActivity.play(
                    viewBinding.root.context,
                    streamSource.source,
                    streamSource.title,
                    streamSource.description
                )
            }
        }
    }
}
