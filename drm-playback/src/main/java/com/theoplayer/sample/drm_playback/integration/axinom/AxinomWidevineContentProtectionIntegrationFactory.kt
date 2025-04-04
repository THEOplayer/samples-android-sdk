package com.theoplayer.sample.drm_playback.integration.axinom

import com.theoplayer.android.api.contentprotection.ContentProtectionIntegrationFactory
import com.theoplayer.android.api.source.drm.DRMConfiguration
import com.theoplayer.android.api.contentprotection.ContentProtectionIntegration

class AxinomWidevineContentProtectionIntegrationFactory : ContentProtectionIntegrationFactory {
    override fun build(configuration: DRMConfiguration): ContentProtectionIntegration {
        return AxinomWidevineContentProtectionIntegration(configuration)
    }
}