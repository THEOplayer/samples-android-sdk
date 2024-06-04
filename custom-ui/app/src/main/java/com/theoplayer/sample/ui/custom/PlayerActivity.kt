package com.theoplayer.sample.ui.custom

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.slider.Slider
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.ReadyState
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.sample.ui.custom.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var viewModel: PlayerViewModel
    private lateinit var theoPlayer: Player
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TheoTheme_Base)
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding and model classes.
        viewModel = ViewModelProvider(this)[PlayerViewModel::class.java]
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)
        viewBinding.lifecycleOwner = this
        viewBinding.viewModel = viewModel

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.player

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)

        // Configure UI behavior and default values.
        configureUI()

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer()
    }

    private fun configureUI() {
        // Listening to player overlay click events to toggle player controls visibility.
        viewBinding.playerClickableOverlay.setOnClickListener { view: View? -> viewModel.toggleUI() }

        // Listening to play/pause button click events to play/pause stream playback.
        viewBinding.playPauseButton.setOnClickListener {
            if (theoPlayer.isPaused) {
                theoPlayer.play()
            } else {
                theoPlayer.pause()
            }
        }

        // Listening to skipForward button click events to move stream forward by given tine interval.
        viewBinding.skipForwardButton.setOnClickListener {
            val skipForwardInSeconds = resources.getInteger(R.integer.skipForwardInSeconds)
            theoPlayer.currentTime = theoPlayer.currentTime + skipForwardInSeconds
        }

        // Listening to skipBackward button click events to move stream backward by given tine interval.
        viewBinding.skipBackwardButton.setOnClickListener {
            val skipBackwardInSeconds = resources.getInteger(R.integer.skipBackwardInSeconds)
            theoPlayer.currentTime = theoPlayer.currentTime - skipBackwardInSeconds
        }

        // Listening to slider seeking events to change stream position to selected time interval
        // and to display stream progress while seeking.
        viewBinding.progressSlider.setLabelFormatter { time: Float ->
            viewModel.formatTimeValue(
                time.toDouble()
            )
        }
        viewBinding.progressSlider.addOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                viewModel.markSeeking()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                viewModel.markSought()
                theoPlayer.currentTime = slider.value.toDouble()
            }
        })
    }

    private fun configureTHEOplayer() {
        // Creating a TypedSource builder that defines the location of a single stream source.
        val typedSource = TypedSource.Builder(getString(R.string.defaultSourceUrl))

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        val sourceDescription = SourceDescription.Builder(typedSource.build())
            .poster(getString(R.string.defaultPosterUrl))

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.source = sourceDescription.build()

        // Listening to 'sourcechange' event which indicates resetting UI and displaying only big
        // play button that loads defined source.
        theoPlayer.addEventListener(PlayerEventTypes.SOURCECHANGE) {
            Log.i(TAG, "Event: SOURCECHANGE")
            viewModel.resetUI()
        }

        // Listening to 'play' event which indicates the intent of playing source. Depending on
        // actual state, source will be loaded first and/or played.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY) {
            Log.i(TAG, "Event: PLAY")
            viewModel.markBuffering()
        }

        // Listening to 'duration` event which indicates that the source is loaded to the point
        // that its duration is known.
        theoPlayer.addEventListener(PlayerEventTypes.DURATIONCHANGE) { event: DurationChangeEvent ->
            Log.i(TAG, "Event: DURATIONCHANGE, " + event.duration)
            viewModel.setDuration(event.duration)
        }

        // Listening to 'playing' event which indicates that source is being played.
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING) {
            Log.i(TAG, "Event: PLAYING")
            viewModel.markPlaying()
        }

        // Listening to 'pause' event which indicates that the source was paused.
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE) {
            Log.i(TAG, "Event: PAUSE")
            viewModel.markPaused()
        }

        // Listening to 'readystatechange' event which indicates the ability of playing the source.
        // This is the most general way of getting stream state. There are more specific events like
        // 'canplay', 'canplaythrough', 'waiting', 'seeking', 'seeked' that allows to design more
        // advanced flows.
        theoPlayer.addEventListener(PlayerEventTypes.READYSTATECHANGE) { event: ReadyStateChangeEvent ->
            Log.i(TAG, "Event: READYSTATECHANGE, readyState=" + event.readyState)
            if (event.readyState != ReadyState.HAVE_ENOUGH_DATA) {
                viewModel.markBuffering()
            }
        }

        // Listening to 'timeupdate' event which indicates source playback position change.
        theoPlayer.addEventListener(PlayerEventTypes.TIMEUPDATE) { event: TimeUpdateEvent ->
            Log.i(TAG, "Event: TIMEUPDATE, currentTime=" + event.currentTime)
            viewModel.setCurrentTime(event.currentTime)
        }

        // Listening to 'error' event which indicates that something went wrong.
        theoPlayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
            Log.i(TAG, "Event: ERROR, error=" + event.errorObject)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        var uiVisibilityFlags = View.SYSTEM_UI_FLAG_VISIBLE
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // With landscape orientation window (activity) display mode is changed to full screen
            // with status, navigation and action bars hidden.
            // Note that status and navigation bars are still available on swiping screen edge.
            supportActionBar?.hide()
            uiVisibilityFlags = (uiVisibilityFlags
                    or View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            // With portrait orientation window (activity) display mode is changed back to default
            // with status, navigation and action bars shown.
            supportActionBar?.show()
        }
        window.decorView.systemUiVisibility = uiVisibilityFlags
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