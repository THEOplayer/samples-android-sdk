package com.theoplayer.sample.playback.background.media

import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.theoplayer.android.api.event.ads.AdIntegrationKind
import com.theoplayer.android.api.player.track.texttrack.TextTrackKind
import com.theoplayer.android.api.source.*
import com.theoplayer.android.api.source.addescription.AdDescription
import com.theoplayer.android.api.source.addescription.GoogleImaAdDescription
import com.theoplayer.android.api.source.drm.DRMConfiguration
import com.theoplayer.android.api.source.drm.DRMIntegrationId
import com.theoplayer.android.api.source.drm.preintegration.*
import com.theoplayer.android.api.source.metadata.MetadataDescription
import com.theoplayer.android.api.source.ssai.SsaiIntegration
import com.theoplayer.android.api.source.ssai.YoSpaceDescription
import com.theoplayer.android.api.source.ssai.dai.GoogleDaiLiveConfiguration
import com.theoplayer.android.api.source.ssai.dai.GoogleDaiVodConfiguration
import org.json.JSONException
import org.json.JSONObject

private const val TAG = "SourceHelper"
private const val PROP_SSAI = "ssai"
private const val PROP_TYPE = "type"
private const val PROP_SRC = "src"
private const val PROP_SOURCES = "sources"
private const val PROP_DEFAULT = "default"
private const val PROP_LABEL = "label"
private const val PROP_KIND = "kind"
private const val PROP_TIME_OFFSET = "timeOffset"
private const val PROP_INTEGRATION = "integration"
private const val PROP_CONTENT_PROTECTION = "contentProtection"
private const val PROP_CONTENT_PROTECTION_INTEGRATION = "integration"
private const val PROP_TEXT_TRACKS = "textTracks"
private const val PROP_POSTER = "poster"
private const val PROP_ADS = "ads"
private const val PROP_AVAILABILITY_TYPE = "availabilityType"
private const val PROP_LIVE_OFFSET = "liveOffset"
private const val PROP_METADATA = "metadata"

/**
 * Source parsing helper class, because we don't support GSON object deserialization currently
 */
class SourceHelper {
    private val gson = Gson()

    fun parseSourceFromJSON(jsonSourceObject: JSONObject): SourceDescription? {
        try {
            // typed sources
            val typedSources = ArrayList<TypedSource>()

            // sources can be an array or single object
            val jsonSources = jsonSourceObject.optJSONArray(PROP_SOURCES)
            if (jsonSources != null) {
                for (i in 0 until jsonSources.length()) {
                    val typedSource = parseTypedSource(jsonSources[i] as JSONObject)
                    if (typedSource != null) {
                        typedSources.add(typedSource)
                    }
                }
            } else {
                val typedSource = parseTypedSource(jsonSourceObject.getJSONObject(PROP_SOURCES))
                if (typedSource != null) {
                    typedSources.add(typedSource)
                }
            }

            // poster
            val poster = jsonSourceObject.optString(PROP_POSTER)

            // metadata
            var metadataDescription: MetadataDescription? = null
            val jsonMetadata = jsonSourceObject.optJSONObject(PROP_METADATA)
            if (jsonMetadata != null) {
                metadataDescription = parseMetadataDescription(jsonMetadata)
            }

            // ads
            val jsonAds = jsonSourceObject.optJSONArray(PROP_ADS)
            val ads = ArrayList<AdDescription>()
            if (jsonAds != null) {
                for (i in 0 until jsonAds.length()) {
                    val jsonAdDescription = jsonAds[i] as JSONObject

                    // Currently only ima-ads are supported.
                    val ad = parseAdFromJS(jsonAdDescription)
                    if (ad != null) {
                        ads.add(ad)
                    }
                }
            }

            // Side-loaded text tracks
            val textTracks = jsonSourceObject.optJSONArray(PROP_TEXT_TRACKS)
            val sideLoadedTextTracks = ArrayList<TextTrackDescription>()
            if (textTracks != null) {
                for (i in 0 until textTracks.length()) {
                    val jsonTextTrack = textTracks[i] as JSONObject
                    sideLoadedTextTracks.add(parseTextTrackFromJS(jsonTextTrack))
                }
            }
            val builder = SourceDescription.Builder(*typedSources.toArray(arrayOf<TypedSource>()))
                .poster(poster)
                .ads(*ads.toArray(arrayOf<AdDescription>()))
                .textTracks(*sideLoadedTextTracks.toArray(arrayOf<TextTrackDescription>()))
            if (metadataDescription != null) {
                builder.metadata(metadataDescription)
            }
            return builder.build()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    private fun parseTypedSource(jsonTypedSource: JSONObject): TypedSource? {
        try {
            var tsBuilder = TypedSource.Builder.typedSource()
            if (jsonTypedSource.has(PROP_SSAI)) {
                val ssaiJson = jsonTypedSource.getJSONObject(PROP_SSAI)

                // Check for valid SsaiIntegration
                val ssaiIntegration = SsaiIntegration.from(ssaiJson.optString(PROP_INTEGRATION))
                if (ssaiIntegration != null) {
                    when (ssaiIntegration) {
                        SsaiIntegration.GOOGLE_DAI -> tsBuilder = if (ssaiJson.optString(
                                PROP_AVAILABILITY_TYPE
                            ) == "vod"
                        ) {
                            GoogleDaiTypedSource.Builder(
                                gson.fromJson(
                                    ssaiJson.toString(),
                                    GoogleDaiVodConfiguration::class.java
                                )
                            )
                        } else {
                            GoogleDaiTypedSource.Builder(
                                gson.fromJson(
                                    ssaiJson.toString(),
                                    GoogleDaiLiveConfiguration::class.java
                                )
                            )
                        }
                        SsaiIntegration.YOSPACE -> tsBuilder.ssai(
                            gson.fromJson(
                                ssaiJson.toString(),
                                YoSpaceDescription::class.java
                            )
                        )
                        else -> Log.e(TAG, "SSAI integration not supported: $ssaiIntegration")
                    }
                } else {
                    Log.e(TAG, "Missing SSAI integration")
                }
            }
            tsBuilder.src(jsonTypedSource.optString(PROP_SRC))
            val sourceType = parseSourceType(jsonTypedSource)
            if (sourceType != null) {
                tsBuilder.type(sourceType)
            }
            if (jsonTypedSource.has(PROP_LIVE_OFFSET)) {
                tsBuilder.liveOffset(jsonTypedSource.getDouble(PROP_LIVE_OFFSET))
            }
            if (jsonTypedSource.has(PROP_CONTENT_PROTECTION)) {
                val contentProtection = jsonTypedSource.getJSONObject(PROP_CONTENT_PROTECTION)

                // Look for specific DRM pre-integration, otherwise use default.
                val integration = contentProtection.optString(PROP_CONTENT_PROTECTION_INTEGRATION)
                var integrationId: DRMIntegrationId? = null
                if (!TextUtils.isEmpty(integration)) {
                    integrationId = DRMIntegrationId.from(integration)
                    if (integrationId == null) {
                        Log.e(TAG, "ContentProtection integration not supported: $integration")
                    }
                }
                if (integrationId != null) {
                    when (integrationId) {
                        DRMIntegrationId.AXINOM -> tsBuilder.drm(
                            gson.fromJson(
                                contentProtection.toString(),
                                AxinomDRMConfiguration::class.java
                            )
                        )
                        DRMIntegrationId.AZURE -> tsBuilder.drm(
                            gson.fromJson(
                                contentProtection.toString(),
                                AzureDRMConfiguration::class.java
                            )
                        )
                        DRMIntegrationId.CONAX -> tsBuilder.drm(
                            gson.fromJson(
                                contentProtection.toString(),
                                ConaxDRMConfiguration::class.java
                            )
                        )
                        DRMIntegrationId.DRMTODAY -> tsBuilder.drm(
                            gson.fromJson(
                                contentProtection.toString(),
                                DRMTodayConfiguration::class.java
                            )
                        )
                        DRMIntegrationId.IRDETO -> tsBuilder.drm(
                            gson.fromJson(
                                contentProtection.toString(),
                                IrdetoConfiguration::class.java
                            )
                        )
                        DRMIntegrationId.KEYOS -> tsBuilder.drm(
                            gson.fromJson(
                                contentProtection.toString(),
                                KeyOSDRMConfiguration::class.java
                            )
                        )
                        DRMIntegrationId.TITANIUM -> tsBuilder.drm(
                            gson.fromJson(
                                contentProtection.toString(),
                                TitaniumDRMConfiguration::class.java
                            )
                        )
                        DRMIntegrationId.VUDRM -> tsBuilder.drm(
                            gson.fromJson(
                                contentProtection.toString(),
                                VudrmDRMConfiguration::class.java
                            )
                        )
                        DRMIntegrationId.XSTREAM -> tsBuilder.drm(
                            gson.fromJson(
                                contentProtection.toString(),
                                XstreamConfiguration::class.java
                            )
                        )
                        else -> Log.e(
                            TAG,
                            "ContentProtection integration not supported: $integration"
                        )
                    }
                } else {
                    tsBuilder.drm(
                        gson.fromJson(
                            jsonTypedSource[PROP_CONTENT_PROTECTION].toString(),
                            DRMConfiguration::class.java
                        )
                    )
                }
            }
            return tsBuilder.build()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    fun parseAdFromJSON(json: String?): AdDescription? {
        return try {
            val jsonAdDescription = JSONObject(json)
            parseAdFromJS(jsonAdDescription)
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    private fun parseSourceType(jsonTypedSource: JSONObject): SourceType? {
        val type = jsonTypedSource.optString(PROP_TYPE)
        if (type.isNotEmpty()) {
            if ("application/dash+xml" == type) {
                return SourceType.DASH
            }
            if ("application/x-mpegurl" == type) {
                return SourceType.HLSX
            }
            if ("application/vnd.theo.hesp+json" == type) {
                return SourceType.HESP
            }
            if ("application/vnd.apple.mpegurl" == type) {
                return SourceType.HLS
            }
            if ("video/mp4" == type) {
                return SourceType.MP4
            }
            if ("audio/mpeg" == type) {
                return SourceType.MP3
            }
        } else {
            // No type given, check for known extension.
            val src = jsonTypedSource.optString(PROP_SRC)
            if (src.endsWith(".mpd")) {
                return SourceType.DASH
            }
            if (src.endsWith(".m3u8")) {
                return SourceType.HLSX
            }
            if (src.endsWith(".mp4")) {
                return SourceType.MP4
            }
            if (src.endsWith(".mp3")) {
                return SourceType.MP3
            }
        }
        return null
    }

    @Throws(JSONException::class)
    fun parseAdFromJS(jsonAdDescription: JSONObject): AdDescription? {
        val integrationKind = AdIntegrationKind.from(
            jsonAdDescription.optString(
                PROP_INTEGRATION
            )
        )
        return when (integrationKind) {
            AdIntegrationKind.GOOGLE_IMA -> parseImaAdFromJS(jsonAdDescription)
            AdIntegrationKind.DEFAULT, AdIntegrationKind.THEO, AdIntegrationKind.FREEWHEEL, AdIntegrationKind.SPOTX -> {
                Log.e(
                    TAG,
                    "Ad integration not supported: $integrationKind"
                )
                null
            }
            else -> {
                Log.e(
                    TAG,
                    "Ad integration not supported: $integrationKind"
                )
                null
            }
        }
    }

    private fun parseImaAdFromJS(jsonAdDescription: JSONObject): GoogleImaAdDescription {
        val source: String
        // Property `sources` is of type string | AdSource.
        val sourceObj = jsonAdDescription.optJSONObject(PROP_SOURCES)
        source = if (sourceObj != null) {
            sourceObj.optString(PROP_SRC)
        } else {
            jsonAdDescription.optString(PROP_SOURCES)
        }
        return GoogleImaAdDescription.Builder.googleImaAdDescription()
            .source(source)
            .timeOffset(jsonAdDescription.optString(PROP_TIME_OFFSET))
            .build()
    }

    @Throws(JSONException::class)
    private fun parseTextTrackFromJS(jsonTextTrack: JSONObject): TextTrackDescription {
        val builder = TextTrackDescription.Builder.textTrackDescription()
            .isDefault(jsonTextTrack.optBoolean(PROP_DEFAULT))
            .src(jsonTextTrack.optString(PROP_SRC))
            .label(jsonTextTrack.optString(PROP_LABEL))
            .kind(parseTextTrackKind(jsonTextTrack.optString(PROP_KIND))!!)
        return builder.build()
    }

    private fun parseTextTrackKind(kind: String?): TextTrackKind? {
        if (kind == null) {
            return null
        }
        when (kind) {
            "subtitles" -> return TextTrackKind.SUBTITLES
            "metadata" -> return TextTrackKind.METADATA
            "captions" -> return TextTrackKind.CAPTIONS
            "chapters" -> return TextTrackKind.CHAPTERS
            "descriptions" -> return TextTrackKind.DESCRIPTIONS
        }
        return null
    }

    private fun parseMetadataDescription(metadataDescription: JSONObject): MetadataDescription? {
        val metadata = HashMap<String, Any>()
        val keys = metadataDescription.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            try {
                metadata[key] = metadataDescription[key]
            } catch (e: JSONException) {
                Log.w(TAG, "Failed to parse metadata key $key")
            }
        }
        return MetadataDescription(metadata)
    }
}