package com.theoplayer.sample.surface

import android.view.View.MeasureSpec
import com.theoplayer.android.api.player.AspectRatio

object AspectRatioHelper {

    fun getSizes(widthMeasureSpec: Int, heightMeasureSpec: Int, contentHeight: Int, contentWidth: Int, aspectRatio: AspectRatio): Pair<Int, Int> {
        if (contentWidth == 0 || contentHeight == 0 || aspectRatio == AspectRatio.FILL) {
            return widthMeasureSpec to heightMeasureSpec
        }

        val viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        val viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        val calculatedHeight = viewWidth * contentHeight / contentWidth
        val finalWidth: Int
        val finalHeight: Int

        // case: AspectRatio.FIT -> some black bars (left/right OR top/bottom)
        if (aspectRatio == AspectRatio.FIT) {
            if (calculatedHeight > viewHeight) {
                finalHeight = viewHeight
                finalWidth = finalHeight * contentWidth / contentHeight
            } else {
                finalWidth = viewWidth
                finalHeight = calculatedHeight
            }

            return MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY) to
                    MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY)
        }

        // case: AspectRatio.ASPECT_FILL -> some content outside of the view (keeping ratio)
        if (calculatedHeight > viewHeight) {
            finalWidth = viewWidth
            finalHeight = calculatedHeight
        } else {
            finalHeight = viewHeight
            finalWidth = finalHeight * contentWidth / contentHeight
        }

        return MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.AT_MOST) to
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.AT_MOST)
    }
}
