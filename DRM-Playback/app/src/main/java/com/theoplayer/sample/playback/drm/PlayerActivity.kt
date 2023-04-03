package com.theoplayer.sample.playback.drm

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.drm.DRMConfiguration
import com.theoplayer.android.api.source.drm.KeySystemConfiguration
import com.theoplayer.sample.playback.drm.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayer: Player

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TheoTheme_Base)
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.player

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer()
    }

    private fun configureTHEOplayer() {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.settings.isFullScreenOrientationCoupled = true

        // Creating a KeySystemConfiguration builder that contains license acquisition URL used
        // during the licensing process with a DRM server.
        val keySystemConfig = KeySystemConfiguration.Builder(getString(R.string.defaultLicenseUrl))

        // Creating a DRMConfiguration builder that contains license acquisition parameters
        // for integration with a Widevine license server.
        val drmConfiguration = DRMConfiguration.Builder()
            .widevine(keySystemConfig.build())

        // Creating a TypedSource builder that defines the location of a single stream source
        // and has Widevine DRM parameters applied.
        val typedSource = TypedSource.Builder(getString(R.string.defaultSourceUrl))
            .drm(drmConfiguration.build())

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        val sourceDescription = SourceDescription.Builder(typedSource.build())
            .poster(getString(R.string.defaultPosterUrl))

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.source = sourceDescription.build()
        theoPlayer.isAutoplay = true

        // Adding listeners to THEOplayer basic playback events.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY) {
            Log.i(TAG, "Event: PLAY")
        }
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING) {
            Log.i(TAG, "Event: PLAYING")
        }
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE) {
            Log.i(TAG, "Event: PAUSE")
        }
        theoPlayer.addEventListener(PlayerEventTypes.ENDED) {
            Log.i(TAG, "Event: ENDED")
        }
        theoPlayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
            Log.i(TAG, "Event: ERROR, error=" + event.errorObject)
        }

        // Adding listeners to THEOplayer content protection events.
        theoPlayer.addEventListener(PlayerEventTypes.CONTENTPROTECTIONSUCCESS) { event: ContentProtectionSuccessEvent ->
            Log.i(TAG, "Event: CONTENT_PROTECTION_SUCCESS, mediaTrackType=" + event.mediaTrackType)
        }
        theoPlayer.addEventListener(PlayerEventTypes.CONTENTPROTECTIONERROR) { event: ContentProtectionErrorEvent ->
            Log.i(TAG, "Event: CONTENT_PROTECTION_ERROR, error=" + event.errorObject)
        }
    }

    // In order to work properly and in sync with the activity lifecycle changes (e.g. device
    // is rotated, new activity is started or app is moved to background) we need to call
    // the "onResume", "onPause" and "onDestroy" methods of the THEOplayerView when the matching
    // activity methods are called.
    override fun onPause() {
        super.onPause()
        viewBinding.theoPlayerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        viewBinding.theoPlayerView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding.theoPlayerView.onDestroy()
    }

    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
    }
}