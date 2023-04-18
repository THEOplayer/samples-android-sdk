package com.theoplayer.sample.playback.offline;

import android.content.Context;

import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.TypedSource;
import com.theoplayer.android.api.source.drm.DRMConfiguration;
import com.theoplayer.android.api.source.drm.KeySystemConfiguration;
import com.theoplayer.android.api.source.drm.LicenseType;

public final class SourceDescriptionRepository {

    public static SourceDescription getBySourceUrl(Context context, String sourceUrl) {
        if (sourceUrl.equals(context.getString(R.string.bigBuckBunnySourceUrl))) {
            return getBigBuckBunnySourceDescription(context);
        }
        if (sourceUrl.equals(context.getString(R.string.sintelSourceUrl))) {
            return getSintelSourceDescription(context);
        }
        if (sourceUrl.equals(context.getString(R.string.tearsOfStealSourceUrl))) {
            return getTearsOfSteelSourceDescription(context);
        }
        if (sourceUrl.equals(context.getString(R.string.elephantsDreamSourceUrl))) {
            return getElephantsDreamSourceDescription(context);
        }
        return null;
    }

    private static SourceDescription getBigBuckBunnySourceDescription(Context context) {
        return new SourceDescription.Builder(
                new TypedSource.Builder(context.getString(R.string.bigBuckBunnySourceUrl))
                        .drm(
                                new DRMConfiguration.Builder().widevine(
                                        // Note that license has to have PERSISTENT type configured
                                        // to be cached and to allow offline playback.
                                        new KeySystemConfiguration
                                                .Builder(context.getString(R.string.bigBuckBunnyLicenseUrl))
                                                .licenseType(LicenseType.PERSISTENT)
                                                .build()
                                ).build()
                        )
                        .build()
        ).build();
    }

    private static SourceDescription getSintelSourceDescription(Context context) {
        return new SourceDescription.Builder(
                new TypedSource.Builder(
                        context.getString(R.string.sintelSourceUrl)
                ).build()
        ).build();
    }

    private static SourceDescription getTearsOfSteelSourceDescription(Context context) {
        return new SourceDescription.Builder(
                new TypedSource.Builder(
                        context.getString(R.string.tearsOfStealSourceUrl)
                ).build()
        ).build();
    }

    private static SourceDescription getElephantsDreamSourceDescription(Context context) {
        return new SourceDescription.Builder(
                new TypedSource.Builder(
                        context.getString(R.string.elephantsDreamSourceUrl)
                ).build()
        ).build();
    }

}
