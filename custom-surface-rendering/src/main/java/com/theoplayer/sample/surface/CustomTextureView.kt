package com.theoplayer.sample.surface

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import com.theoplayer.android.api.player.AspectRatio
import com.theoplayer.android.api.player.Player

class CustomTextureView(
    context: Context,
    private val theoPlayer: Player,
    private var aspectRatio: AspectRatio
) : TextureView(context), SurfaceTextureListener {

    init {
        surfaceTextureListener = this
    }

    fun setAspectRatio(aspectRatio: AspectRatio) {
        this.aspectRatio = aspectRatio
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val pair = AspectRatioHelper.getSizes(widthMeasureSpec, heightMeasureSpec, theoPlayer.videoHeight, theoPlayer.videoWidth, aspectRatio)
        super.onMeasure(pair.first, pair.second)
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        theoPlayer.setCustomSurface(Surface(surfaceTexture), width, height)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }
}