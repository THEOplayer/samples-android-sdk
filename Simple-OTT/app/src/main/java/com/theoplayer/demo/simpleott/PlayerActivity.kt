package com.theoplayer.demo.simpleott

import android.annotation.TargetApi
import android.app.PictureInPictureParams
import android.content.*
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.metadata.ChromecastMetadataDescription
import com.theoplayer.demo.simpleott.databinding.ActivityPlayerBinding
import com.theoplayer.demo.simpleott.model.StreamSource

class PlayerActivity : AppCompatActivity() {
    private var viewBinding: ActivityPlayerBinding? = null
    private var theoPlayer: Player? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.player

        // Configuring THEOplayer playback with parameters from intent.
        configureTHEOplayer(
            intent.getStringExtra(PLAYER_PARAM__SOURCE),
            intent.getStringExtra(PLAYER_PARAM__TITLE)
        )
    }

    private fun configureTHEOplayer(source: String?, title: String?) {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding!!.theoPlayerView.settings.isFullScreenOrientationCoupled = true

        // Creating a TypedSource builder that defines the location of a single stream source.
        val typedSource = TypedSource.Builder.typedSource(source!!)

        // Creating a ChromecastMetadataDescription builder that defines stream metadata to be
        // displayed on cast sender and receiver while casting.
        val chromecastMetadata = ChromecastMetadataDescription.Builder
            .chromecastMetadata()
            .title(title!!)

        // Creating a SourceDescription that contains the tab_settings to be applied as a new
        // THEOplayer source.
        val sourceDescription = SourceDescription.Builder
            .sourceDescription(typedSource.build())
            .metadata(chromecastMetadata.build())
        theoPlayer!!.isAutoplay = true

        // Configuring THEOplayer with defined SourceDescription object to be played automatically.
        theoPlayer!!.source = sourceDescription.build()

        // Adding listeners to THEOplayer basic playback events.
        theoPlayer!!.addEventListener(PlayerEventTypes.PLAY) { event: PlayEvent? ->
            Log.i(
                TAG,
                "Event: PLAY"
            )
        }
        theoPlayer!!.addEventListener(PlayerEventTypes.PLAYING) { event: PlayingEvent? ->
            Log.i(
                TAG,
                "Event: PLAYING"
            )
        }
        theoPlayer!!.addEventListener(PlayerEventTypes.PAUSE) { event: PauseEvent? ->
            Log.i(
                TAG,
                "Event: PAUSE"
            )
        }
        theoPlayer!!.addEventListener(PlayerEventTypes.ENDED) { event: EndedEvent? ->
            Log.i(
                TAG,
                "Event: ENDED"
            )
        }
        theoPlayer!!.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
            Log.i(
                TAG,
                "Event: ERROR, error=" + event.errorObject
            )
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        viewBinding!!.theoPlayerView.settings.isFullScreenOrientationCoupled =
            !isInPictureInPictureMode
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }

    override fun onUserLeaveHint() {
        if (SUPPORTS_PIP) {
            if (!theoPlayer!!.isPaused) {
                enterPictureInPictureMode(PictureInPictureParams.Builder().build())
            }
        } else {
            ToastUtils.toastMessage(this, R.string.pipNotSupported)
        }
    }

    // In order to work properly and in sync with the activity lifecycle changes (e.g. device
    // is rotated, new activity is started or app is moved to background) we need to call
    // the "onResume", "onPause" and "onDestroy" methods of the THEOplayerView when the matching
    // activity methods are called.
    override fun onPause() {
        super.onPause()
        if (SUPPORTS_PIP && !isInPictureInPictureMode) {
            viewBinding!!.theoPlayerView.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (SUPPORTS_PIP && !isInPictureInPictureMode) {
            try {
                viewBinding!!.theoPlayerView.onResume()
            } catch (exception: Exception) {
                Log.i(TAG, "", exception)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding!!.theoPlayerView.onDestroy()
    }

    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
        private const val PLAYER_PARAM__SOURCE = "SOURCE"
        private const val PLAYER_PARAM__TITLE = "TITLE"
        private val SUPPORTS_PIP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

        /**
         * Allows to start playback of given `streamSource`.
         *
         *
         * There's no need to configure THEOplayer source with any caching task. THEOplayer will find
         * automatically caching task for played source if any exists.
         *
         * @param context - The current context.
         * @param streamSource - The stream source to be played.
         */
        fun play(context: Context, streamSource: StreamSource) {
            val playIntent = Intent(context, PlayerActivity::class.java)
            playIntent.putExtra(PLAYER_PARAM__SOURCE, streamSource.source)
            playIntent.putExtra(PLAYER_PARAM__TITLE, streamSource.title)
            context.startActivity(playIntent)
        }
    }
}