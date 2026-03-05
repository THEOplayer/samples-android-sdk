package com.theoplayer.sample.common

import com.theoplayer.android.api.ads.theoads.TheoAdDescription
import com.theoplayer.android.api.millicast.MillicastSource
import com.theoplayer.android.api.source.GoogleDaiTypedSource
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.SourceType
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.addescription.GoogleImaAdDescription
import com.theoplayer.android.api.source.drm.ClearkeyKeySystemConfiguration
import com.theoplayer.android.api.source.drm.DRMConfiguration
import com.theoplayer.android.api.source.drm.KeySystemConfiguration
import com.theoplayer.android.api.source.drm.LicenseType
import com.theoplayer.android.api.source.metadata.ChromecastMetadataDescription
import com.theoplayer.android.api.source.ssai.dai.GoogleDaiLiveConfiguration
import com.theoplayer.android.api.source.ssai.dai.GoogleDaiVodConfiguration
import com.theoplayer.android.api.theolive.TheoLiveSource
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
        val CLEARKEY: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://storage.googleapis.com/shaka-demo-assets/angel-one-clearkey/dash.mpd")
                    .drm(
                        DRMConfiguration.Builder().clearkey(
                            ClearkeyKeySystemConfiguration.Builder("https://cwip-shaka-proxy.appspot.com/clearkey?_u3wDe7erb7v8Lqt8A3QDQ=ABEiM0RVZneImaq7zN3u_w")
                                .build()
                        ).build()
                    ).build()
            ).build()
        }
        val HLS_RADIO_WITH_ID3_METADATA: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/indexcom/index.m3u8")
                    .build()
            ).build()
        }
        val HLS_WITH_PROGRAM_DATE_TIME: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/star_wars_episode_vii-the_force_awakens_official_comic-con_2015_reel_(2015)/index-daterange.m3u8")
                    .build()
            ).build()
        }
        val HLS_WITH_DATE_RANGE: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/star_wars_episode_vii-the_force_awakens_official_comic-con_2015_reel_(2015)/index-daterange.m3u8")
                    .hlsDateRange(true)
                    .build()
            ).build()
        }
        val DASH_WITH_EMSG: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://refapp.hbbtv.org/videos/00_llama_multiperiod_v1/manifest.mpd")
                    .build()
            ).build()
        }
        val DASH_WITH_SCTE35: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://demo.unified-streaming.com/k8s/live/scte35.isml/.mpd")
                    .build()
            ).build()
        }
        val BIG_BUCK_BUNNY_DASH: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/dash/big_buck_bunny/BigBuckBunny_10s_simple_2014_05_09.mpd")
                    .build()
            ).build()
        }
        val SINTEL_HLS: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/sintel/nosubs.m3u8")
                    .build()
            ).build()
        }
        val STAR_WARS_HLS: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/star_wars_episode_vii-the_force_awakens_official_comic-con_2015_reel_(2015)/index.m3u8")
                    .build()
            ).build()
        }
        val ELEPHANTS_DREAM_HLS: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/elephants-dream/playlist.m3u8")
                    .build()
            ).build()
        }
        val TEARS_OF_STEEL_HLS: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/tears_of_steel/index.m3u8")
                    .build()
            ).build()
        }
        val TEARS_OF_STEEL_DRM_PERSISTENT: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/dash/tos-dash-widevine/tos_h264_main.mpd")
                    .drm(
                        DRMConfiguration.Builder()
                            .widevine(
                                // Note that license has to have PERSISTENT type configured
                                // to be cached and to allow offline playback.
                                KeySystemConfiguration.Builder("https://widevine-dash.ezdrm.com/proxy?pX=62448C")
                                    .licenseType(LicenseType.PERSISTENT)
                                    .build()
                            ).build()
                    ).build()
            ).build()
        }
        // This video is provided by cottonbro studio (https://www.pexels.com/video/the-art-of-skateboarding-2791956/)
        val SKATING_PORTRAIT_MP4: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/skating-portrait.mp4")
                    .build()
            ).build()
        }
        val THEOADS: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://example.com/manifest.m3u8")
                    .type(SourceType.HLS)
                    .hlsDateRange(true) // The flag needs to be set to `true` as the ad markers are done using `EXT-X-DATERANGE` tags.
                    .build()
            ).ads(
                TheoAdDescription(
                    networkCode = "network-code-here",
                    customAssetKey = "asset-key-here",
                    backdropDoubleBox = "https://example.com/double.box.svg",
                    backdropLShape = "https://example.com/L-shape.svg"
                )
            ).build()
        }
        val THEOLIVE: SourceDescription by lazy {
            SourceDescription.Builder(
                TheoLiveSource(
                    src = "ar5c53uzm3si4h4zgkzrju44h",
                )
            ).build()
        }
        val MILLICAST: SourceDescription by lazy {
            SourceDescription.Builder(
                MillicastSource(
                    src = "multiview",
                    streamAccountId = "k9Mwad",
                    apiUrl = "https://director.millicast.com/api/director/subscribe",
//                    subscriberToken = "<token>" // This is only required for subscribing to secure streams and should be omitted otherwise.
                )
            ).build()
        }
    }
}