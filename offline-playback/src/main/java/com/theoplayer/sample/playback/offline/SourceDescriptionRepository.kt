package com.theoplayer.sample.playback.offline

import android.content.Context
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.drm.DRMConfiguration
import com.theoplayer.android.api.source.drm.KeySystemConfiguration
import com.theoplayer.android.api.source.drm.LicenseType

object SourceDescriptionRepository {
    fun getBySourceUrl(context: Context, sourceUrl: String?): SourceDescription? =
        when (sourceUrl) {
            context.getString(R.string.bigBuckBunnySourceUrl) -> getBigBuckBunnySourceDescription(
                context
            )

            context.getString(R.string.sintelSourceUrl) -> getSintelSourceDescription(context)
            context.getString(R.string.tearsOfStealSourceUrl) -> getTearsOfSteelSourceDescription(
                context
            )

            context.getString(R.string.bipBopSourceUrl) -> getBipBopSourceDescription(
                context
            )

            else -> null
        }

    private fun getBigBuckBunnySourceDescription(context: Context): SourceDescription {
        return SourceDescription.Builder(
            TypedSource.Builder(context.getString(R.string.bigBuckBunnySourceUrl))
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
            ).drm(
                DRMConfiguration.Builder()
                    .widevine( // Note that license has to have PERSISTENT type configured
                        // to be cached and to allow offline playback.
                        KeySystemConfiguration.Builder(context.getString(R.string.tearsOfStealLicenseUrl))
                            .licenseType(LicenseType.PERSISTENT)
                            .build()
                    ).build()
            ).build()
        ).build()
    }

    private fun getBipBopSourceDescription(context: Context): SourceDescription {
        return SourceDescription.Builder(
            TypedSource.Builder(
                context.getString(R.string.bipBopSourceUrl)
            ).build()
        ).build()
    }
}