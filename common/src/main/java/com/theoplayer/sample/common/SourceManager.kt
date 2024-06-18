package com.theoplayer.sample.common

import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.addescription.GoogleImaAdDescription


class SourceManager private constructor() {
    companion object {
        val ELEPHANTS_DREAM_HLS: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/elephants-dream/playlist.m3u8")
                    .build()
            )
                .poster("https://cdn.theoplayer.com/video/elephants-dream/playlist.png")
                .build()

        }
        val HLS_WITH_VMAP: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/elephants-dream/playlist.m3u8")
                    .build()
            )
                .ads(
                    GoogleImaAdDescription
                        .Builder("https://cdn.theoplayer.com/demos/ads/vmap/single-pre-mid-post-no-skip.xml")
                        .build()
                )
                .poster("https://cdn.theoplayer.com/video/elephants-dream/playlist.png")
                .build()

        }
        val HLS_WITH_VAST_PRE_ROLL: SourceDescription by lazy {
            SourceDescription.Builder(
                TypedSource.Builder("https://cdn.theoplayer.com/video/elephants-dream/playlist.m3u8")
                    .build()
            )
                .ads(
                    GoogleImaAdDescription
                        .Builder("https://cdn.theoplayer.com/demos/ads/vast/dfp-preroll-no-skip.xml")
                        .build()
                )
                .poster("https://cdn.theoplayer.com/video/elephants-dream/playlist.png")
                .build()

        }
    }
}