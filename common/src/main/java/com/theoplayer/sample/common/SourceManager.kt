package com.theoplayer.sample.common

import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.addescription.GoogleImaAdDescription
import com.theoplayer.android.api.source.drm.DRMConfiguration
import com.theoplayer.android.api.source.drm.KeySystemConfiguration
import com.theoplayer.android.api.source.metadata.ChromecastMetadataDescription


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
    }
}