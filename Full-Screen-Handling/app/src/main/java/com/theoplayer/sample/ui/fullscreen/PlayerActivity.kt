package com.theoplayer.sample.ui.fullscreen

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.fullscreen.FullScreenChangeListener
import com.theoplayer.android.api.fullscreen.FullScreenManager
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.sample.ui.fullscreen.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayer: Player
    private lateinit var theoFullScreenManager: FullScreenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TheoTheme_Base)
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.player
        theoFullScreenManager = viewBinding.theoPlayerView.fullScreenManager

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)

        // Configure UI behavior and default values.
        viewBinding.fullScreenButton.setOnClickListener { theoFullScreenManager.requestFullScreen() }

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer()
    }

    private fun configureTHEOplayer() {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.settings.isFullScreenOrientationCoupled = true

        // Setting custom full screen activity which allows to change behavior
        // and/or look of the full screen activity.
        theoFullScreenManager.fullscreenActivity = CustomFullScreenActivity::class.java

        // Creating a TypedSource builder that defines the location of a single stream source.
        val typedSource = TypedSource.Builder.typedSource(getString(R.string.defaultSourceUrl))

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        val sourceDescription = SourceDescription.Builder.sourceDescription(typedSource.build())
            .poster(getString(R.string.defaultPosterUrl))

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.source = sourceDescription.build()

        // Adding listeners to THEOplayer basic playback events.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY) { Log.i(TAG, "Event: PLAY") }
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING) { Log.i(TAG, "Event: PLAYING") }
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE) { Log.i(TAG, "Event: PAUSE") }
        theoPlayer.addEventListener(PlayerEventTypes.ENDED) { Log.i(TAG, "Event: ENDED") }
        theoPlayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
            Log.i(TAG, "Event: ERROR, error=" + event.errorObject)
        }

        // Adding listeners to THEOplayer basic full screen changes events.
        theoFullScreenManager.addFullScreenChangeListener(object : FullScreenChangeListener {
            override fun onEnterFullScreen() {
                Log.i(TAG, "Event: FULL_SCREEN_ENTERED")
            }

            override fun onExitFullScreen() {
                Log.i(TAG, "Event: FULL_SCREEN_EXITED")
            }
        })
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
        viewBinding.theoPlayerView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding.theoPlayerView.onDestroy()
    }

    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
    }
}