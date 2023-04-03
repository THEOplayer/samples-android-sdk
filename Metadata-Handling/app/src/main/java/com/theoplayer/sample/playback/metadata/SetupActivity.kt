package com.theoplayer.sample.playback.metadata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.theoplayer.sample.playback.metadata.PlayerActivity.Companion.play
import com.theoplayer.sample.playback.metadata.databinding.ActivitySetupBinding
import com.theoplayer.sample.playback.metadata.databinding.LayoutMetadataBinding

class SetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TheoTheme_Base)
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding class.
        val viewBinding =
            DataBindingUtil.setContentView<ActivitySetupBinding>(this, R.layout.activity_setup)

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)

        // Configure UI behavior and default values.
        viewBinding.metadataRecyclerView.layoutManager = LinearLayoutManager(this)
        viewBinding.metadataRecyclerView.adapter = MetadataAdapter(
            intArrayOf(
                R.string.hlsWithID3MetadataName,
                R.string.hlsWithProgramDateTimeMetadataName,
                R.string.hlsWithDateRangeMetadataName,
                R.string.dashWithEmsgMetadataName,
                R.string.dashWithEventStreamMetadataName
            )
        )
    }

    private inner class MetadataAdapter constructor(private val metadataIds: IntArray) :
        RecyclerView.Adapter<MetadataAdapter.ViewHolder>() {
        override fun getItemCount(): Int {
            return metadataIds.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val viewBinding = LayoutMetadataBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(viewBinding)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val metadataId = metadataIds[position]
            val metadataName = viewHolder.itemView.context.getString(metadataId)
            viewHolder.viewBinding.metadataNameTextView.text = metadataName
            viewHolder.itemView.setOnClickListener { view: View -> play(view.context, metadataId) }
        }

        inner class ViewHolder(var viewBinding: LayoutMetadataBinding) :
            RecyclerView.ViewHolder(viewBinding.root)
    }
}