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
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.pip.PiPType
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.sample.common.SourceManager
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

        // Enable all debug logs from THEOplayer.
        val theoDebugLogger = THEOplayerGlobal.getSharedInstance(this).logger
        theoDebugLogger.enableAllTags()

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)

        // Keep the device screen on.
        viewBinding.theoPlayerView.keepScreenOn = true

        //  Set autoplay to start video whenever player is visible.
        theoPlayer.isAutoplay = true

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer()

        // You can trigger pip using:
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
        viewBinding.theoPlayerView.fullScreenManager.isFullScreenOrientationCoupled = true

        // Configuring THEOplayer with a source.
        theoPlayer.source = SourceManager.BIP_BOP_HLS

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