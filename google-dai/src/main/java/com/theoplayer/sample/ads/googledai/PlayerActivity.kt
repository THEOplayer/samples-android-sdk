package com.theoplayer.sample.ads.googledai

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.android.api.event.ads.AdsEventTypes
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.sample.ads.googledai.databinding.ActivityPlayerBinding
import com.theoplayer.sample.common.SourceManager

class PlayerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPlayerBinding
    private lateinit var theoPlayer: Player
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.theoplayer.sample.common.R.style.TheoTheme_Base)
        super.onCreate(savedInstanceState)

        // See basic-playback's PlayerActivity for more information about basic setup.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player)
        theoPlayer = viewBinding.theoPlayerView.player
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)
        viewBinding.theoPlayerView.fullScreenManager.isFullScreenOrientationCoupled = true
        theoPlayer.isAutoplay = true

        // THEOplayer automatically adds all available integrations to the player. Alternatively, you can set autoIntegrations(false) on your player configuration and add them manually.
//        val googleDaiIntegration: GoogleDaiIntegration = GoogleDaiIntegrationFactory.createGoogleDaiIntegration(viewBinding.theoPlayerView)
//        theoPlayer.addIntegration(googleDaiIntegration)

        // Set a source with ads on the player.
        theoPlayer.source = SourceManager.DAI_HLS_LIVE

        attachEventListeners()
    }

    private fun attachEventListeners() {
        // Adding listeners to THEOplayer basic playback events.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY) {
            Log.i(TAG,"Event: PLAY")
        }
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING) {
            Log.i(TAG, "Event: PLAYING")
        }
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE) {
            Log.i(TAG, "Event: PAUSE")
        }
        theoPlayer.addEventListener(PlayerEventTypes.ENDED) {
            Log.i(TAG, "Event: ENDED")
        }
        theoPlayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
            Log.i(TAG,"Event: ERROR, error=" + event.errorObject)
        }

        // Adding listeners to THEOplayer basic ad events.
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_BEGIN) {
            Log.i(TAG, "Event: AD_BEGIN")
        }
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_END) {
            Log.i(TAG,"Event: AD_END")
        }
        theoPlayer.ads.addEventListener(AdsEventTypes.AD_ERROR) {
            Log.i(TAG, "Event: AD_ERROR")
        }
    }

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