package com.theoplayer.demo.simpleott.model

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.theoplayer.demo.simpleott.R
import java.lang.reflect.Type
import java.util.*

/**
 * Provides stream source definitions.
 *
 *
 * Stream source definitions are loaded from 'raw/stream_source.json' configuration.
 */
class StreamSourceRepository(context: Context) {
    private val streamSources: StreamSources?

    init {
        val configInputStream = context.resources.openRawResource(R.raw.stream_sources)
        val jsonString = Scanner(configInputStream).useDelimiter("\\Z").next()
        streamSources = GsonBuilder()
            .registerTypeAdapter(StreamSource::class.java, streamSourceDeserializer(context))
            .create()
            .fromJson(jsonString, StreamSources::class.java)
    }

    /**
     * Returns stream sources to be displayed on "LIVE" tab.
     *
     * @return live stream sources.
     */
    val liveStreamSources: List<StreamSource>?
        get() = streamSources?.live ?: ArrayList()

    /**
     * Returns stream sources to be displayed on "ON DEMAND" tab.
     *
     * @return on demand stream sources.
     */
    val onDemandStreamSources: List<StreamSource>
        get() = streamSources?.onDemand ?: ArrayList()

    /**
     * Returns stream sources to be displayed on "OFFLINE" tab.
     *
     * @return offline stream sources.
     */
    val offlineStreamSources: List<StreamSource>
        get() = if (streamSources != null) streamSources.offline else ArrayList()

    private class StreamSources {
        lateinit var live: List<StreamSource>
        lateinit var onDemand: List<StreamSource>
        lateinit var offline: List<StreamSource>
    }

    companion object {
        private fun streamSourceDeserializer(context: Context): JsonDeserializer<StreamSource> {
            return JsonDeserializer { jsonElement: JsonElement, typeOfT: Type?, jsonContext: JsonDeserializationContext? ->
                val jsonObject = jsonElement.asJsonObject

                // Finding real drawable resource identifier for image reference string.
                var imageResId = 0
                val image = jsonObject["image"].asString
                if (image != null) {
                    imageResId =
                        context.resources.getIdentifier(image, "drawable", context.packageName)
                }
                StreamSource(
                    jsonObject["title"].asString,
                    jsonObject["description"].asString,
                    jsonObject["source"].asString,
                    if (imageResId != 0) imageResId else R.mipmap.ic_launcher
                )
            }
        }
    }
}