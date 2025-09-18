package com.theoplayer.sample.surface

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.player.AspectRatio
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.RenderingTarget
import com.theoplayer.sample.common.SourceManager
import com.theoplayer.sample.surface.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayer: Player
    private var aspectRatio = AspectRatio.FIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // See basic-playback's PlayerActivity for more information about basic setup.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        // Enable all debug logs from THEOplayer.
        val theoDebugLogger = THEOplayerGlobal.getSharedInstance(this).logger
        theoDebugLogger.enableAllTags()

        theoPlayer = viewBinding.theoPlayerView.player
        setSupportActionBar(viewBinding.toolbar)
        viewBinding.theoPlayerView.fullScreenManager.isFullScreenOrientationCoupled = true
        theoPlayer.source = SourceManager.BIG_BUCK_BUNNY_HLS
        theoPlayer.isAutoplay = true
        theoPlayer.setAspectRatio(aspectRatio)

        // Set onClickListeners to allow surface switching.
        viewBinding.btnSetSurfaceView.setOnClickListener(::setSurfaceView)
        viewBinding.btnSetTextureView.setOnClickListener(::setTextureView)
        viewBinding.btnSetCustomSurfaceView.setOnClickListener(::setCustomSurfaceView)
        viewBinding.btnSetCustomTextureView.setOnClickListener(::setCustomTextureView)
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

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        layoutParams.gravity = Gravity.CENTER

        val customSurfaceView = CustomSurfaceView(this, theoPlayer, aspectRatio)
        customSurfaceView.layoutParams = layoutParams

        viewBinding.customSurfaceViewContainer.addView(customSurfaceView)
    }

    private fun setCustomTextureView(view: View) {
        viewBinding.customSurfaceViewContainer.removeAllViews()
        viewBinding.customTextureViewContainer.removeAllViews()

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        layoutParams.gravity = Gravity.CENTER

        val customTextureView = CustomTextureView(this, theoPlayer, aspectRatio)
        customTextureView.layoutParams = layoutParams

        viewBinding.customTextureViewContainer.addView(customTextureView)
    }

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