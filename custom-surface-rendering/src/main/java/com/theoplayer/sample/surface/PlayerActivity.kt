package com.theoplayer.sample.surface

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            viewBinding.btnSetSurfaceControl.setOnClickListener(::setSurfaceControl)
        }
        viewBinding.btnSetCustomSurfaceView.setOnClickListener(::setCustomSurfaceView)
        viewBinding.btnSetCustomTextureView.setOnClickListener(::setCustomTextureView)
    }

    private fun setSurfaceView(view: View) {
        // Stop the playback to avoid decoder issues on some devices when switching between rendering targets at runtime.
        theoPlayer.stop()

        viewBinding.customSurfaceViewContainer.removeAllViews()
        viewBinding.customTextureViewContainer.removeAllViews()

        // Set the rendering target to SurfaceView.
        theoPlayer.setRenderingTarget(RenderingTarget.SURFACE_VIEW)

        // Set the source.
        theoPlayer.source = SourceManager.BIG_BUCK_BUNNY_HLS
    }


    private fun setTextureView(view: View) {
        // Stop the playback to avoid decoder issues on some devices when switching between rendering targets at runtime.
        theoPlayer.stop()

        viewBinding.customSurfaceViewContainer.removeAllViews()
        viewBinding.customTextureViewContainer.removeAllViews()

        // Set the rendering target to TextureView.
        theoPlayer.setRenderingTarget(RenderingTarget.TEXTURE_VIEW)

        // Set the source.
        theoPlayer.source = SourceManager.BIG_BUCK_BUNNY_HLS
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setSurfaceControl(view: View) {
        // Stop the playback to avoid decoder issues on some devices when switching between rendering targets at runtime.
        theoPlayer.stop()

        viewBinding.customSurfaceViewContainer.removeAllViews()
        viewBinding.customTextureViewContainer.removeAllViews()

        // Set the rendering target to SurfaceControl. This requires API 29 or later.
        theoPlayer.setRenderingTarget(RenderingTarget.SURFACE_CONTROL)

        // Set the source.
        theoPlayer.source = SourceManager.BIG_BUCK_BUNNY_HLS
    }

    private fun setCustomSurfaceView(view: View) {
        // Stop the playback to avoid decoder issues on some devices when switching between rendering targets at runtime.
        theoPlayer.stop()

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

        // Set the source.
        theoPlayer.source = SourceManager.BIG_BUCK_BUNNY_HLS
    }

    private fun setCustomTextureView(view: View) {
        // Stop the playback to avoid decoder issues on some devices when switching between rendering targets at runtime.
        theoPlayer.stop()

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

        // Set the source.
        theoPlayer.source = SourceManager.BIG_BUCK_BUNNY_HLS
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