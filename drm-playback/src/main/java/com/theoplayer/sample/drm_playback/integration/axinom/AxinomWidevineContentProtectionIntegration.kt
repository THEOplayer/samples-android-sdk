package com.theoplayer.sample.drm_playback.integration.axinom

import com.theoplayer.android.api.source.drm.DRMConfiguration
import com.theoplayer.android.api.contentprotection.ContentProtectionIntegration
import com.theoplayer.android.api.contentprotection.LicenseRequestCallback
import com.theoplayer.android.api.contentprotection.Request
import java.lang.NullPointerException

class AxinomWidevineContentProtectionIntegration(private val contentProtectionConfiguration: DRMConfiguration) :
    ContentProtectionIntegration() {
    override fun onLicenseRequest(request: Request, callback: LicenseRequestCallback) {
        val token = contentProtectionConfiguration.integrationParameters["token"]
        if (token == null) {
            callback.error(NullPointerException("The Axinom DRM token cannot be null."))
            return
        }
        var licenseUrl: String? = null
        if (contentProtectionConfiguration.widevine != null) {
            licenseUrl = contentProtectionConfiguration.widevine!!.licenseAcquisitionURL
        }
        if (licenseUrl == null) {
            callback.error(NullPointerException("The Axinom licenseAcquisitionURL cannot be null."))
            return
        }
        request.url = licenseUrl
        request.headers["X-AxDRM-Message"] = "$token"
        callback.request(request)
    }
}