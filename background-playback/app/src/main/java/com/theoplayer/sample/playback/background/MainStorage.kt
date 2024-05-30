package com.theoplayer.sample.playback.background

import android.content.Context
import android.content.SharedPreferences

private const val MY_PREFERENCES = "THEOplayerMediaSessionDemoPrefs"
private const val RESTART_SERVICE = "restart_service"
private const val DEFAULT_RESTART_SERVICE = true

class MainStorage constructor(context: Context) {
    private val mSharedPref: SharedPreferences

    init {
        mSharedPref = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
    }

    fun setRestartService(restartService: Boolean) {
        val editor = mSharedPref.edit()
        editor.putBoolean(RESTART_SERVICE, restartService)
        editor.apply()
    }

    fun shouldRestartService(): Boolean {
        return mSharedPref.getBoolean(RESTART_SERVICE, DEFAULT_RESTART_SERVICE)
    }
}