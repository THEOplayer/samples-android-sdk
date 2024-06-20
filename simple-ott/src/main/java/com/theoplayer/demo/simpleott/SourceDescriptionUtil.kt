package com.theoplayer.demo.simpleott

import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource

internal object SourceDescriptionUtil {
    fun getBySourceUrl(sourceUrl: String?): SourceDescription {
        return getSimpleSourceDescription(sourceUrl)
    }

    private fun getSimpleSourceDescription(url: String?): SourceDescription {
        return SourceDescription.Builder(
            TypedSource.Builder(
                url!!
            ).build()
        ).build()
    }
}