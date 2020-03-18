package com.theoplayer.sample.playback.offline;

import android.content.Context;

import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.drm.LicenseType;

import static com.theoplayer.android.api.source.SourceDescription.Builder.sourceDescription;
import static com.theoplayer.android.api.source.TypedSource.Builder.typedSource;
import static com.theoplayer.android.api.source.drm.DRMConfiguration.Builder.widevineDrm;
import static com.theoplayer.android.api.source.drm.KeySystemConfiguration.Builder.keySystemConfiguration;

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
        return sourceDescription(
                typedSource(context.getString(R.string.bigBuckBunnySourceUrl))
                        .drm(
                                widevineDrm(
                                        // Note that license has to have PERSISTENT type configured
                                        // to be cached and to allow offline playback.
                                        keySystemConfiguration(context.getString(R.string.bigBuckBunnyLicenseUrl))
                                                .licenseType(LicenseType.PERSISTENT)
                                                .build()
                                ).build()
                        )
                        .setExperimentalRenderingEnabled(true)
                        .build()
        ).build();
    }

    private static SourceDescription getSintelSourceDescription(Context context) {
        return sourceDescription(
                typedSource(context.getString(R.string.sintelSourceUrl)).build()
        ).build();
    }

    private static SourceDescription getTearsOfSteelSourceDescription(Context context) {
        return sourceDescription(
                typedSource(context.getString(R.string.tearsOfStealSourceUrl)).build()
        ).build();
    }

    private static SourceDescription getElephantsDreamSourceDescription(Context context) {
        return sourceDescription(
                typedSource(context.getString(R.string.elephantsDreamSourceUrl)).build()
        ).build();
    }

}
