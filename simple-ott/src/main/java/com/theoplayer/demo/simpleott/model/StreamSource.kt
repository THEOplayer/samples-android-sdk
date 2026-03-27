package com.theoplayer.demo.simpleott.model

import androidx.annotation.DrawableRes

data class StreamSource(
    val title: String,
    val description: String,
    val source: String,
    @DrawableRes val imageResId: Int
)
