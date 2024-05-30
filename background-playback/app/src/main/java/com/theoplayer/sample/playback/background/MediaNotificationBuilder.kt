package com.theoplayer.sample.playback.background

import android.app.Notification
import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.connector.mediasession.MediaSessionConnector

class MediaNotificationBuilder(
    private val context: Context,
    private val player: Player,
    private val mediaSessionConnector: MediaSessionConnector
) {

    private val prevAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.theo_icon_previous, context.getString(R.string.prev),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
    )

    private val nextAction: NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.theo_icon_next, context.getString(R.string.next),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )
    )

    private val playAction = NotificationCompat.Action(
        R.drawable.theo_icon_play, context.getString(R.string.play),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_PLAY_PAUSE
        )
    )

    private val pauseAction = NotificationCompat.Action(
        R.drawable.theo_icon_pause, context.getString(R.string.pause),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_PLAY_PAUSE
        )
    )

    fun build(channelId: String): Notification {
        val builder = NotificationCompat.Builder(context, channelId).apply {

            // Add the metadata for the currently playing track
            val metadata = mediaSessionConnector.getMediaSessionMetadata()
            setContentTitle(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
            setContentText(metadata.getString(MediaMetadataCompat.METADATA_KEY_COMPOSER))
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_media_large))

            // Enable launching the player by clicking the notification
            setContentIntent(mediaSessionConnector.mediaSession.controller.sessionActivity)

            // Stop the service when the notification is swiped away
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            // The UI may choose to show these items smaller, or at a different position in the
            // list, compared with your app's PRIORITY_DEFAULT items
            priority = NotificationCompat.PRIORITY_LOW

            // Make the transport controls visible on the lockscreen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Add an app icon and set its accent color
            setSmallIcon(R.drawable.ic_music_note)

            // Be careful when you set the background color. In an ordinary notification in
            // Android version 5.0 or later, the color is applied only to the background of the
            // small app icon. But for MediaStyle notifications prior to Android 7.0, the color is
            // used for the entire notification background. Test your background color. Go gentle
            // on the eyes and avoid extremely bright or fluorescent colors.
            color = ContextCompat.getColor(context, R.color.theoLightningYellow)

            // Take advantage of MediaStyle features
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    // Associate the notification with your session. This allows third-party apps and
                    // companion devices to access and control the session.
                    .setMediaSession(mediaSessionConnector.mediaSession.sessionToken)

                    // Add up to 3 actions to be shown in the notification's standard-sized contentView.
                    .setShowActionsInCompactView(0, 1, 2)

                    // In Android 5.0 (API level 21) and later you can swipe away a notification to
                    // stop the player once the service is no longer running in the foreground.
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )

            // Add a go to previous button
            addAction(prevAction)

            // Add a play/pause button
            if (player.isPaused) {
                addAction(playAction)
            } else {
                addAction(pauseAction)
            }

            // Add a skip to next button
            addAction(nextAction)
        }

        return builder.build()
    }
}