package com.theoplayer.sample.playback.background.media

import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.connector.mediasession.QueueNavigator

private const val TAG = "MediaQueueNavigator"

class MediaQueueNavigator(private val mediaLibrary: MediaLibrary): QueueNavigator {
    override fun getSupportedQueueNavigatorActions(player: Player): Long =
        PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

    override fun getActiveQueueItemId(player: Player): Long {
        return mediaLibrary.getId(mediaLibrary.currentAsset) ?: -1
    }

    override fun onSkipToPrevious(player: Player) {
        Log.d(TAG, "MediaQueueNavigator\$onSkipToPrevious")
        mediaLibrary.currentAsset = mediaLibrary.getNextById(mediaLibrary.getId(mediaLibrary.currentAsset))
        if (mediaLibrary.currentAsset != null) {
            player.source = mediaLibrary.currentAsset?.sourceDescription
        }
    }

    override fun onSkipToQueueItem(player: Player, id: Long) {
        Log.d(TAG, "MediaQueueNavigator\$onSkipToQueueItem")
        mediaLibrary.currentAsset = mediaLibrary.getById(id)
        if (mediaLibrary.currentAsset != null) {
            player.source = mediaLibrary.currentAsset?.sourceDescription
        }
    }

    override fun onSkipToNext(player: Player) {
        Log.d(TAG, "MediaQueueNavigator\$onSkipToNext")
        mediaLibrary.currentAsset = mediaLibrary.getPrevById(mediaLibrary.getId(mediaLibrary.currentAsset))
        if (mediaLibrary.currentAsset != null) {
            player.source = mediaLibrary.currentAsset?.sourceDescription
        }
    }
}