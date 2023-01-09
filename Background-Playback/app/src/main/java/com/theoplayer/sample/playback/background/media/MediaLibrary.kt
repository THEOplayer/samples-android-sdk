package com.theoplayer.sample.playback.background.media

import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.theoplayer.android.api.source.SourceDescription
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.nio.charset.StandardCharsets

private const val PROP_NAME = "name"
private const val PROP_SOURCE = "source"

class MediaLibrary(private val context: Context) {

    data class MediaAsset(
        val mediaId: String,
        val mediaItem: MediaBrowserCompat.MediaItem,
        val sourceDescription: SourceDescription
    )

    private val sources = ArrayList<MediaAsset>()

    init {
        loadSources()
    }

    var currentAsset: MediaAsset? = null

    fun getByMediaId(mediaId: String?): MediaAsset? {
        if (mediaId == null) {
            return null
        }
        return sources.find { asset -> mediaId == asset.mediaId }
    }

    fun getId(asset: MediaAsset?): Long? {
        if (asset == null) {
            return null
        }
        return sources.indexOf(asset).toLong()
    }

    fun getById(id: Long?): MediaAsset? {
        if (id == null) {
            return null
        }
        return sources[id.toInt()]
    }

    fun getNextById(id: Long?) : MediaAsset? {
        if (id == null) {
            return null
        }
        return sources[if (id + 1 < sources.size) { (id + 1).toInt() } else { 0 }]
    }

    fun getPrevById(id: Long?) : MediaAsset? {
        if (id == null) {
            return null
        }
        return sources[if (id - 1 >= 0) { (id - 1).toInt() } else { sources.size - 1 }]
    }

    fun getMediaItems(): List<MediaBrowserCompat.MediaItem> {
        return sources.map {
            source -> source.mediaItem
        }
    }

    fun search(query: String?): MediaAsset? {
        if (query == null) {
            return null
        }
        return sources.find { asset ->
            asset.mediaId.lowercase().contains(query) ||
            (asset.mediaItem.description.title?.toString()?.lowercase()?.contains(query) ?: false)
        }
    }

    private fun loadSources() {
        try {
            val sourceHelper = SourceHelper()
            val jsonSources = JSONArray(loadJSONFromAsset(context))
            for (i in 0 until jsonSources.length()) {
                val jsonSource = jsonSources.getJSONObject(i)
                val sourceDescription = sourceHelper.parseSourceFromJSON(jsonSource.getJSONObject(
                    PROP_SOURCE
                ))
                if (sourceDescription != null) {
                    val mediaId = jsonSource.getString(PROP_NAME)
                    sources.add(
                        MediaAsset(
                        mediaId,
                        mediaItemFromSource(mediaId, sourceDescription),
                        sourceDescription
                    )
                    )
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun mediaItemFromSource(mediaId: String, sourceDescription: SourceDescription): MediaBrowserCompat.MediaItem {
        val title = sourceDescription.metadata?.get<String>("title")
        return MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(title)
                .build(),
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }

    private fun loadJSONFromAsset(context: Context): String? {
        val json: String = try {
            val data = context.assets.open("sources.json")
            val size = data.available()
            val buffer = ByteArray(size)
            data.read(buffer)
            data.close()
            String(buffer, StandardCharsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }
}
