package com.theoplayer.sample.surface

import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.theoplayer.android.api.player.AspectRatio
import com.theoplayer.android.api.player.Player

class CustomSurfaceView(
    context: Context,
    private val theoPlayer: Player,
    private var aspectRatio: AspectRatio
) : SurfaceView(context), SurfaceHolder.Callback {

    init {
        holder.addCallback(this)
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

    override fun surfaceCreated(holder: SurfaceHolder) {
        theoPlayer.setCustomSurface(holder.surface, width, height)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }
}