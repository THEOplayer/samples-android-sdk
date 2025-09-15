package com.theoplayer.sample.common

import com.theoplayer.android.api.source.GoogleDaiTypedSource
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.SourceType
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.addescription.GoogleImaAdDescription
import com.theoplayer.android.api.source.drm.DRMConfiguration
import com.theoplayer.android.api.source.drm.KeySystemConfiguration
import com.theoplayer.android.api.source.metadata.ChromecastMetadataDescription
import com.theoplayer.android.api.source.ssai.dai.GoogleDaiLiveConfiguration
import com.theoplayer.android.api.source.ssai.dai.GoogleDaiVodConfiguration
import java.util.Collections


class SourceManager private constructor() {
    companion object {
        val BIP_BOP_HLS: SourceDescription by lazy {
            SourceDescription
                .Builder(
                    TypedSource
                        .Builder("https://cdn.theoplayer.com/video/bipbop_16x9_variant/index-sample.m3u8")
                        .build()
                )
                .build()
        }
        val BIG_BUCK_BUNNY_HLS: SourceDescription by lazy {
            SourceDescription
                .Builder(
                    TypedSource
                        .Builder("https://cdn.theoplayer.com/video/big_buck_bunny/big_buck_bunny.m3u8")
                        .build()
                )
                .build()
        }
        val COSMOS_DASH: SourceDescription by lazy {
            SourceDescription
                .Builder(
                    TypedSource
                        .Builder("https://cdn.theoplayer.com/video/cosmos/cmaf.mpd")
                        .build()
                )
                .build()
        }
        val BIG_BUCK_BUNNY_HLS_WITH_CAST_METADATA: SourceDescription by lazy {
            SourceDescription
                .Builder(
                    TypedSource
                        .Builder("https://cdn.theoplayer.com/video/big_buck_bunny/big_buck_bunny.m3u8")
                        .build()
                )
                .metadata(
                    ChromecastMetadataDescription
                        .Builder()
                        .title("Big Buck Bunny")
                        .images("https://cdn.theoplayer.com/video/big_buck_bunny/poster.jpg")
                        .build()
                )
                .poster("https://cdn.theoplayer.com/video/big_buck_bunny/poster.jpg")
                .build()
        }
        val HLS_WITH_VMAP: SourceDescription by lazy {
            SourceDescription
                .Builder(
                    TypedSource
                        .Builder("https://cdn.theoplayer.com/video/big_buck_bunny/big_buck_bunny.m3u8")
                        .build()
                )
                .ads(
                    GoogleImaAdDescription
                        .Builder("https://cdn.theoplayer.com/demos/ads/vmap/single-pre-mid-post-no-skip.xml")
                        .build()
                )
                .poster("https://cdn.theoplayer.com/video/big_buck_bunny/poster.jpg")
                .build()
        }
        val HLS_WITH_VAST_PRE_ROLL: SourceDescription by lazy {
            SourceDescription
                .Builder(
                    TypedSource
                        .Builder("https://cdn.theoplayer.com/video/big_buck_bunny/big_buck_bunny.m3u8")
                        .build()
                )
                .ads(
                    GoogleImaAdDescription
                        .Builder("https://cdn.theoplayer.com/demos/ads/vast/dfp-preroll-no-skip.xml")
                        .build()
                )
                .poster("https://cdn.theoplayer.com/video/big_buck_bunny/poster.jpg")
                .build()
        }
        val CUSTOM_AXINOM_DRM: SourceDescription by lazy {
            val integrationParams = HashMap<String, Any?>()
            integrationParams["token"] = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ2ZXJzaW9uIjoxLCJjb21fa2V5X2lkIjoiNjllNTQwODgtZTllMC00NTMwLThjMWEtMWViNmRjZDBkMTRlIiwibWVzc2FnZSI6eyJ0eXBlIjoiZW50aXRsZW1lbnRfbWVzc2FnZSIsImtleXMiOlt7ImlkIjoiOWViNDA1MGQtZTQ0Yi00ODAyLTkzMmUtMjdkNzUwODNlMjY2In1dfX0.WmCnXIxbLNW0iMEWp4SL4I_yCDpwUtefevc2symqOTQ"

            SourceDescription.Builder(
                TypedSource.Builder("https://media.axprod.net/TestVectors/v7-MultiDRM-SingleKey/Manifest_1080p.mpd")
                    .drm(
                        DRMConfiguration.Builder()
                            .customIntegrationId("axinom")
                            .integrationParameters(integrationParams)
                            .widevine(
                                KeySystemConfiguration.Builder("https://drm-widevine-licensing.axtest.net/AcquireLicense")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            ).build()
        }
        val DRMTODAY_HEADERS: SourceDescription by lazy {
            val drmToken = "ewogICAgInVzZXJJZCI6ICJhd3MtZWxlbWVudGFsOjpzcGVrZS10ZXN0aW5nIiwKICAgICJzZXNzaW9uSWQiOiAiZWxlbWVudGFsLXJlZnN0cmVhbSIsCiAgICAibWVyY2hhbnQiOiAiYXdzLWVsZW1lbnRhbCIKfQo="
            val headers: Map<String, String> = mapOf("x-dt-custom-data" to drmToken)

            SourceDescription.Builder(
            TypedSource.Builder("https://d24rwxnt7vw9qb.cloudfront.net/v1/dash/e6d234965645b411ad572802b6c9d5a10799c9c1/All_Reference_Streams//6e16c26536564c2f9dbc5f725a820cff/index.mpd")
                .drm(
                    DRMConfiguration.Builder().widevine(
                        KeySystemConfiguration.Builder("https://lic.staging.drmtoday.com/license-proxy-widevine/cenc/?specConform=true")
                            .headers(headers)
                            .build()
                    ).build()
                ).build()
            ).build()
        }
        val WIDEVINE: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd")
                    .drm(
                        DRMConfiguration.Builder().widevine(
                            KeySystemConfiguration.Builder("https://proxy.uat.widevine.com/proxy?video_id=GTS_SW_SECURE_CRYPTO&provider=widevine_test")
                                .build()
                        ).build()
                    ).build()
            ).build()
        }

        val DAI_DASH_VOD: SourceDescription by lazy {
            val vodDashConfiguration = GoogleDaiVodConfiguration.Builder("", "2559737", "tos-dash")
            SourceDescription.Builder(
                GoogleDaiTypedSource.Builder(vodDashConfiguration.build())
                    .type(SourceType.DASH)
                    .build()
            ).build()
        }
        val DAI_DASH_LIVE: SourceDescription by lazy {
            val liveDashConfiguration = GoogleDaiLiveConfiguration.Builder("", "PSzZMzAkSXCmlJOWDmRj8Q")
            SourceDescription.Builder(
                GoogleDaiTypedSource.Builder(liveDashConfiguration.build())
                    .type(SourceType.DASH)
                    .build()
            ).build()
        }
        val DAI_HLS_VOD: SourceDescription by lazy {
            val vodHlsConfiguration = GoogleDaiVodConfiguration.Builder("", "2548831", "tears-of-steel")
            vodHlsConfiguration.adTagParameters(Collections.singletonMap("npa", "1"))

            SourceDescription.Builder(
                GoogleDaiTypedSource.Builder(vodHlsConfiguration.build())
                    .type(SourceType.HLS)
                    .build()
            ).build()
        }
        val DAI_HLS_LIVE: SourceDescription by lazy {
            val liveHlsConfiguration = GoogleDaiLiveConfiguration.Builder("", "c-rArva4ShKVIAkNfy6HUQ")
            liveHlsConfiguration.adTagParameters(Collections.singletonMap("npa", "1"))

            SourceDescription.Builder(
                GoogleDaiTypedSource.Builder(liveHlsConfiguration.build())
                    .type(SourceType.HLS)
                    .build()
            ).build()
        }
    }
}