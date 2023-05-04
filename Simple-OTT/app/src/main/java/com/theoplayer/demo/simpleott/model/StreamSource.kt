package com.theoplayer.demo.simpleott.model

import androidx.annotation.DrawableRes

/**
 * Stream source definition.
 */
open class StreamSource internal constructor(
    /**
     * Returns stream source title.
     *
     * @return the title of stream source.
     */
    val title: String?,
    /**
     * Returns stream source description.
     *
     * @return the description of stream source.
     */
    val description: String?,
    /**
     * Returns stream source content URL.
     *
     * @return the source URL of stream source.
     */
    val source: String,
    /**
     * Returns stream source image drawable resource identifier.
     *
     * @return the drawable resource identifier of stream source image.
     */
    @field:DrawableRes @param:DrawableRes val imageResId: Int
)