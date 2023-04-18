package com.theoplayer.sample.playback.offline

import android.content.Context
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.drm.DRMConfiguration
import com.theoplayer.android.api.source.drm.KeySystemConfiguration
import com.theoplayer.android.api.source.drm.LicenseType

object SourceDescriptionRepository {
    fun getBySourceUrl(context: Context, sourceUrl: String?): SourceDescription? {
        if (sourceUrl == context.getString(R.string.bigBuckBunnySourceUrl)) {
            return getBigBuckBunnySourceDescription(context)
        }
        if (sourceUrl == context.getString(R.string.sintelSourceUrl)) {
            return getSintelSourceDescription(context)
        }
        if (sourceUrl == context.getString(R.string.tearsOfStealSourceUrl)) {
            return getTearsOfSteelSourceDescription(context)
        }
        return if (sourceUrl == context.getString(R.string.elephantsDreamSourceUrl)) {
            getElephantsDreamSourceDescription(context)
        } else null
    }

    private fun getBigBuckBunnySourceDescription(context: Context): SourceDescription {
        return SourceDescription.Builder(
            TypedSource.Builder(context.getString(R.string.bigBuckBunnySourceUrl))
                .drm(
                    DRMConfiguration.Builder()
                        .widevine( // Note that license has to have PERSISTENT type configured
                            // to be cached and to allow offline playback.
                            KeySystemConfiguration.Builder(context.getString(R.string.bigBuckBunnyLicenseUrl))
                                .licenseType(LicenseType.PERSISTENT)
                                .build()
                        ).build()
                )
                .build()
        ).build()
    }

    private fun getSintelSourceDescription(context: Context): SourceDescription {
        return SourceDescription.Builder(
            TypedSource.Builder(
                context.getString(R.string.sintelSourceUrl)
            ).build()
        ).build()
    }

    private fun getTearsOfSteelSourceDescription(context: Context): SourceDescription {
        return SourceDescription.Builder(
            TypedSource.Builder(
                context.getString(R.string.tearsOfStealSourceUrl)
            ).build()
        ).build()
    }

    private fun getElephantsDreamSourceDescription(context: Context): SourceDescription {
        return SourceDescription.Builder(
            TypedSource.Builder(
                context.getString(R.string.elephantsDreamSourceUrl)
            ).build()
        ).build()
    }
}