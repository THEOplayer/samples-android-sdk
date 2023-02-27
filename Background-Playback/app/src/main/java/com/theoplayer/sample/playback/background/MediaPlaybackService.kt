package com.theoplayer.sample.playback.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.connector.mediasession.MediaSessionConnector
import com.theoplayer.android.connector.mediasession.PlaybackPreparer
import com.theoplayer.sample.playback.background.media.MediaLibrary
import com.theoplayer.sample.playback.background.media.MediaQueueNavigator

private const val TAG = "MediaPlaybackService"
private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
private const val NOTIFICATION_ID = 1

private const val STOP_SERVICE_IF_APP_REMOVED = false

class MediaPlaybackService : MediaBrowserServiceCompat() {

    private lateinit var mediaLibrary: MediaLibrary
    private lateinit var player: Player
    private lateinit var playerView: THEOplayerView
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var notificationBuilder: MediaNotificationBuilder
    private lateinit var notificationManager: NotificationManager

    companion object {
        private lateinit var mediaSession: MediaSessionCompat
        private lateinit var storage: MainStorage
        lateinit var instance: MediaPlaybackService
    }

    init {
        instance = this
    }

    /**
     * Broadcast Receiver registered to receive the MEDIA_BUTTON intent coming from clients.
     */
    class MediaReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "MediaReceiver\$onReceive" + intent.action)
            MediaButtonReceiver.handleIntent(mediaSession, intent)
        }
    }

    class StopServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "StopServiceBroadcastReceiver\$onReceive")
            storage.setRestartService(false)
            context.stopService(Intent(context, MediaPlaybackService::class.java))
        }
    }

    class RestartServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "RestartServiceBroadcastReceiver\$onReceive")
            val startIntent = Intent(context, MediaPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent)
            }
            context.startService(startIntent)
        }
    }

    private inner class MediaPlaybackPreparer: PlaybackPreparer {
        override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

        override fun onPrepare(playWhenReady: Boolean) {
            Log.d(TAG, "MediaPlaybackPreparer\$onPrepare")
        }

        override fun onPrepareFromMediaId(mediaId: String?, playWhenReady: Boolean, extras: Bundle?) {
            Log.d(TAG, "MediaPlaybackPreparer\$onPrepareFromMediaId")
            applyAsset(mediaLibrary.getByMediaId(mediaId), playWhenReady)
        }

        override fun onPrepareFromSearch(query: String?, playWhenReady: Boolean, extras: Bundle?) {
            Log.d(TAG, "MediaPlaybackPreparer\$onPrepareFromSearch")
            applyAsset(mediaLibrary.search(query), playWhenReady)
        }

        override fun onPrepareFromUri(uri: Uri?, playWhenReady: Boolean, extras: Bundle?) {
            Log.d(TAG, "MediaPlaybackPreparer\$onPrepareFromUri")
        }

        private fun applyAsset(asset: MediaLibrary.MediaAsset?, playWhenReady: Boolean) {
            if (asset != null) {
                mediaLibrary.currentAsset = asset
                player.source = asset.sourceDescription
                mediaSessionConnector.invalidatePlaybackState()
                if (playWhenReady) {
                    player.play()
                }
            }
        }
    }

    fun getTHEOplayerView(): THEOplayerView {
        return playerView
    }

    override fun onCreate() {
        super.onCreate()
        storage = MainStorage(this)

        initializePlayer()

        // Build a PendingIntent that can be used to launch the UI.
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        mediaLibrary = MediaLibrary(this)

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext,
            TAG
        ).apply {

            setSessionActivity(sessionActivityPendingIntent)

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }

        // Create a MediaSessionConnector and attach the THEOplayer instance.
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.debug = true
        mediaSessionConnector.player = player
        mediaSessionConnector.playbackPreparer = MediaPlaybackPreparer()
        mediaSessionConnector.queueNavigator = MediaQueueNavigator(mediaLibrary)
        mediaSessionConnector.setActive(true)

        notificationManager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        notificationBuilder = MediaNotificationBuilder(this, player, mediaSessionConnector)
    }

    /**
     * Release Player.
     */
    private fun releasePlayer() {
        playerView.onDestroy()
    }

    /**
     * Release the player when the service is destroyed.
     */
    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
        releasePlayer()
        mediaSessionConnector.destroy()
        if (storage.shouldRestartService()) {
            val intent = Intent(this, RestartServiceBroadcastReceiver::class.java)
            sendBroadcast(intent)
        } else {
            storage.setRestartService(true)
        }
    }

    /**
     * Initialize Player.
     */
    private fun initializePlayer() {
        playerView = THEOplayerView(this,
            THEOplayerConfig.Builder().build()
        )
        player = playerView.player

        addListeners()
    }

    private fun addListeners() {
        player.addEventListener(PlayerEventTypes.SOURCECHANGE) { updateNotification() }
        player.addEventListener(PlayerEventTypes.LOADEDMETADATA) { updateNotification() }
        player.addEventListener(PlayerEventTypes.TIMEUPDATE) { }
        player.addEventListener(PlayerEventTypes.ENDED) { updateNotification() }
        player.addEventListener(PlayerEventTypes.PAUSE) { updateNotification() }
        player.addEventListener(PlayerEventTypes.PLAY) {
            // This ensures that the service starts and continues to run, even when all
            // UI MediaBrowser activities that are bound to it unbind.
            startService(Intent(this, MediaPlaybackService::class.java))
            updateNotification()
        }
    }

    private fun updateNotification() {
        val channelId = getString(R.string.notificationChannelId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.notificationChannelName),
                NotificationManager.IMPORTANCE_LOW)
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
        }

        // When a service is playing, it should be running in the foreground.
        // This lets the system know that the service is performing a useful function and should
        // not be killed if the system is low on memory.
        val notification = notificationBuilder.build(channelId)
        if (player.isPaused) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH)
            } else {
                stopForeground(false)
            }
            notificationManager.notify(NOTIFICATION_ID, notification)
        } else {
            // When a service runs in the foreground, it must display a notification, ideally
            // with one or more transport controls. The notification should also include useful
            // information from the session's metadata.
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "onTaskRemoved()")
        super.onTaskRemoved(rootIntent)
        if (STOP_SERVICE_IF_APP_REMOVED) {
            notificationManager.cancel(NOTIFICATION_ID)
            storage.setRestartService(false)
            stopSelf()
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {

        // (Optional) Control the level of access for the specified package name.
        // You'll need to write your own logic to do this.
        return if (allowBrowsing(clientPackageName, clientUid)) {
            // Returns a root ID that clients can use with onLoadChildren() to retrieve
            // the content hierarchy.
            BrowserRoot(MY_MEDIA_ROOT_ID, null)
        } else {
            // Clients can connect, but this BrowserRoot is an empty hierachy
            // so onLoadChildren returns nothing. This disables the ability to browse for content.
            BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
        }
    }

    private fun allowBrowsing(clientPackageName: String, clientUid: Int): Boolean {
        // Always allow
        return true
    }

    override fun onLoadChildren(
        parentMediaId: String,
        result: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        //  Browsing not allowed
        if (MY_EMPTY_MEDIA_ROOT_ID == parentMediaId) {
            result.sendResult(null)
            return
        }

        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID == parentMediaId) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }

        // Assume for example that the catalog is already loaded/cached.
        result.sendResult(mediaLibrary.getMediaItems())
    }
}
