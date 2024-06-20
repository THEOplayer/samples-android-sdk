package com.theoplayer.sample.playback.offline

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.event.player.EndedEvent
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PauseEvent
import com.theoplayer.android.api.event.player.PlayEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.PlayingEvent
import com.theoplayer.android.api.player.Player
import com.theoplayer.sample.playback.offline.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayer: Player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.player

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Configuring THEOplayer playback with parameters from intent.
        configureTHEOplayer(
            intent.getStringExtra(PLAYER_PARAM__SOURCE_URL)
        )
    }

    private fun configureTHEOplayer(sourceUrl: String?) {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.fullScreenManager.isFullScreenOrientationCoupled = true

        // Creating a SourceDescription that contains the settings to be applied as a new
        // THEOplayer source.
        val sourceDescription = SourceDescriptionRepository.getBySourceUrl(this, sourceUrl)

        // Configuring THEOplayer with defined SourceDescription object to be played automatically.
        theoPlayer.source = sourceDescription
        theoPlayer.isAutoplay = true

        // Adding listeners to THEOplayer basic playback events.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY) { event: PlayEvent? ->
            Log.i(TAG, "Event: PLAY")
        }
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING) { event: PlayingEvent? ->
            Log.i(TAG, "Event: PLAYING")
        }
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE) { event: PauseEvent? ->
            Log.i(TAG, "Event: PAUSE")
        }
        theoPlayer.addEventListener(PlayerEventTypes.ENDED) { event: EndedEvent? ->
            Log.i(TAG, "Event: ENDED")
        }
        theoPlayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
            Log.i(TAG, "Event: ERROR, error=" + event.errorObject)
        }
    }

    // In order to work properly and in sync with the activity lifecycle changes (e.g. device
    // is rotated, new activity is started or app is moved to background) we need to call
    // the "onResume", "onPause" and "onDestroy" methods of the THEOplayerView when the matching
    // activity methods are called.
    override fun onPause() {
        super.onPause()
        viewBinding.theoPlayerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        try {
            viewBinding.theoPlayerView.onResume()
        } catch (exception: Exception) {
            Log.i(TAG, "", exception)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding.theoPlayerView.onDestroy()
    }

    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
        private const val PLAYER_PARAM__SOURCE_URL = "SOURCE_URL"
        fun play(context: Context, sourceUrl: String?) {
            val playIntent = Intent(context, PlayerActivity::class.java)
            playIntent.putExtra(PLAYER_PARAM__SOURCE_URL, sourceUrl)
            context.startActivity(playIntent)
        }
    }
}