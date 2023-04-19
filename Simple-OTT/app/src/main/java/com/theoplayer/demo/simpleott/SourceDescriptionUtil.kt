package com.theoplayer.demo.simpleott;

import com.theoplayer.android.api.source.SourceDescription;

import static com.theoplayer.android.api.source.SourceDescription.Builder.sourceDescription;
import static com.theoplayer.android.api.source.TypedSource.Builder.typedSource;

final class SourceDescriptionUtil {

    public static SourceDescription getBySourceUrl(String sourceUrl) {
        return getSimpleSourceDescription(sourceUrl);
    }

    private static SourceDescription getSimpleSourceDescription(String url) {
        return sourceDescription(typedSource(url).build()).build();
    }

}
