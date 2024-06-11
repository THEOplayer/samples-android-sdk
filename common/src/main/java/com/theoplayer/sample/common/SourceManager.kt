package com.theoplayer.sample.common

import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource


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
    }
}