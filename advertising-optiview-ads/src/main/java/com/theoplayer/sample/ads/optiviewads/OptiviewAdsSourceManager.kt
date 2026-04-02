package com.theoplayer.sample.ads.optiviewads

import com.theoplayer.android.api.ads.theoads.TheoAdDescription
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.SourceType
import com.theoplayer.android.api.source.TypedSource

object OptiviewAdsSourceManager {
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
}