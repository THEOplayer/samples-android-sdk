package com.theoplayer.sample.surface

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.AspectRatio
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.RenderingTarget
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.sample.surface.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayer: Player
    private var aspectRatio = AspectRatio.FIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.player

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbar)

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer()

        viewBinding.btnSetSurfaceView.setOnClickListener(::setSurfaceView)
        viewBinding.btnSetTextureView.setOnClickListener(::setTextureView)
        viewBinding.btnSetCustomSurfaceView.setOnClickListener(::setCustomSurfaceView)
        viewBinding.btnSetCustomTextureView.setOnClickListener(::setCustomTextureView)
    }

    private fun configureTHEOplayer() {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.fullScreenManager.isFullScreenOrientationCoupled = true

        // Creating a TypedSource builder that defines the location of a single stream source.
        val typedSource = TypedSource.Builder(getString(R.string.defaultSourceUrl))

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        val sourceDescription = SourceDescription.Builder(typedSource.build())
            .poster(getString(R.string.defaultPosterUrl))

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.source = sourceDescription.build()

        //  Set autoplay to start video whenever player is visible
        theoPlayer.isAutoplay = true

        // Set the initial aspect ratio of the player
        theoPlayer.setAspectRatio(aspectRatio)

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
    }

    private fun setSurfaceView(view: View) {
        viewBinding.customSurfaceViewContainer.removeAllViews()
        viewBinding.customTextureViewContainer.removeAllViews()
        // Set the rendering target to SurfaceView provided by THEOplayer
        theoPlayer.setRenderingTarget(RenderingTarget.SURFACE_VIEW)
    }

    private fun setTextureView(view: View) {
        viewBinding.customSurfaceViewContainer.removeAllViews()
        viewBinding.customTextureViewContainer.removeAllViews()
        // Set the rendering target to TextureView provided by THEOplayer
        theoPlayer.setRenderingTarget(RenderingTarget.TEXTURE_VIEW)
    }

    private fun setCustomSurfaceView(view: View) {
        viewBinding.customSurfaceViewContainer.removeAllViews()
        viewBinding.customTextureViewContainer.removeAllViews()

        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layoutParams.gravity = Gravity.CENTER

        val customSurfaceView = CustomSurfaceView(this, theoPlayer, aspectRatio)
        customSurfaceView.layoutParams = layoutParams

        viewBinding.customSurfaceViewContainer.addView(customSurfaceView)
    }

    private fun setCustomTextureView(view: View) {
        viewBinding.customSurfaceViewContainer.removeAllViews()
        viewBinding.customTextureViewContainer.removeAllViews()

        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layoutParams.gravity = Gravity.CENTER

        val customTextureView = CustomTextureView(this, theoPlayer, aspectRatio)
        customTextureView.layoutParams = layoutParams

        viewBinding.customTextureViewContainer.addView(customTextureView)
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
        private val TAG: String = PlayerActivity::class.java.simpleName
    }
}