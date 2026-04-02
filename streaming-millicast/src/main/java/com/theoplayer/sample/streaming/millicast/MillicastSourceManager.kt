package com.theoplayer.sample.streaming.millicast

import com.theoplayer.android.api.millicast.MillicastSource
import com.theoplayer.android.api.source.SourceDescription

object MillicastSourceManager {
    val MILLICAST: SourceDescription by lazy {
        SourceDescription.Builder(
            MillicastSource(
                src = "multiview",
                streamAccountId = "k9Mwad",
                apiUrl = "https://director.millicast.com/api/director/subscribe",
//                    subscriberToken = "<token>" // This is only required for subscribing to secure streams and should be omitted otherwise.
            )
        ).build()
    }
}