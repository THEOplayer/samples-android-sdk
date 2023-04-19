package com.theoplayer.demo.simpleott.model;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.theoplayer.demo.simpleott.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Provides stream source definitions.
 * <p/>
 * Stream source definitions are loaded from 'raw/stream_source.json' configuration.
 */
public class StreamSourceRepository {

    private StreamSources streamSources;

    public StreamSourceRepository(Context context) {
        InputStream configInputStream = context.getResources().openRawResource(R.raw.stream_sources);
        String jsonString = new Scanner(configInputStream).useDelimiter("\\Z").next();

        streamSources = new GsonBuilder()
                .registerTypeAdapter(StreamSource.class, streamSourceDeserializer(context))
                .create()
                .fromJson(jsonString, StreamSources.class);
    }

    /**
     * Returns stream sources to be displayed on "LIVE" tab.
     *
     * @return live stream sources.
     */
    public List<StreamSource> getLiveStreamSources() {
        return streamSources != null ? streamSources.live : new ArrayList<>();
    }

    /**
     * Returns stream sources to be displayed on "ON DEMAND" tab.
     *
     * @return on demand stream sources.
     */
    public List<StreamSource> getOnDemandStreamSources() {
        return streamSources != null ? streamSources.onDemand : new ArrayList<>();
    }

    /**
     * Returns stream sources to be displayed on "OFFLINE" tab.
     *
     * @return offline stream sources.
     */
    public List<StreamSource> getOfflineStreamSources() {
        return streamSources != null ? streamSources.offline : new ArrayList<>();
    }

    private static JsonDeserializer<StreamSource> streamSourceDeserializer(Context context) {
        return (jsonElement, typeOfT, jsonContext) -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Finding real drawable resource identifier for image reference string.
            int imageResId = 0;
            String image = jsonObject.get("image").getAsString();
            if (image != null) {
                imageResId = context.getResources().getIdentifier(image, "drawable", context.getPackageName());
            }

            return new StreamSource(
                    jsonObject.get("title").getAsString(),
                    jsonObject.get("description").getAsString(),
                    jsonObject.get("source").getAsString(),
                    (imageResId != 0) ? imageResId : R.mipmap.ic_launcher
            );
        };
    }

    private static class StreamSources {
        List<StreamSource> live;
        List<StreamSource> onDemand;
        List<StreamSource> offline;
    }

}
