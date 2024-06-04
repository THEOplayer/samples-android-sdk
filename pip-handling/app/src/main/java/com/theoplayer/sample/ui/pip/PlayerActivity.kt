package com.theoplayer.sample.ui.pip

import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.pip.PiPType
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.sample.ui.pip.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayer: Player
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TheoTheme_Base)
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding class.
        // Pay attention to the app:pip="true" in the activity_player.xml.
        // Programmatically this can be achieved by passing a PiPConfiguration in the THEOplayerConfig.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.player

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer()

        // When using a chromefull player, you can make use of the pip-button in the UI (for devices that support PiP)
        // Otherwise, in the case of chromeless, you can trigger pip using:
        // viewBinding.theoPlayerView.getPiPManager().enterPiP(PiPType.ACTIVITY);
        // viewBinding.theoPlayerView.getPiPManager().exitPiP();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.activity_player, menu)
        return true
    }

    private fun configureTHEOplayer() {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.settings.isFullScreenOrientationCoupled = true

        // Creating a TypedSource builder that defines the location of a single stream source.
        val typedSource = TypedSource.Builder(getString(R.string.defaultSourceUrl))

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        val sourceDescription = SourceDescription.Builder(typedSource.build())
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

        // Adding listeners to THEOplayer basic picture-in-picture changes events.
        theoPlayer.addEventListener(PlayerEventTypes.PRESENTATIONMODECHANGE) {
            Log.i(TAG, "Event: PRESENTATION_MODE_CHANGE")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.pipMenuItem) {
            tryEnterPictureInPictureMode()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onUserLeaveHint() {
        tryEnterPictureInPictureMode()
    }

    private fun tryEnterPictureInPictureMode() {
        if (SUPPORTS_PIP) {
            viewBinding.theoPlayerView.piPManager!!.enterPiP(PiPType.ACTIVITY)
        } else {
            val toastMessage = SpannableString.valueOf(getString(R.string.pipNotSupported))
            toastMessage.setSpan(
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0,
                toastMessage.length,
                0
            )
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
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
        viewBinding.theoPlayerView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding.theoPlayerView.onDestroy()
    }

    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
        private val SUPPORTS_PIP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
}