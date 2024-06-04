package com.theoplayer.sample.playback.mediasession.ui

import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class PlayerViewModel : ViewModel() {
    companion object {
        private const val AUTO_HIDE_UI_DELAY = 5000
    }

    private val duration: MutableLiveData<Float?> = MutableLiveData()

    private val durationText: MutableLiveData<String?> = MutableLiveData()

    private val currentTime: MutableLiveData<Float?> = MutableLiveData()

    private val currentTimeText: MutableLiveData<String?> = MutableLiveData()

    private val isUIRequired: MutableLiveData<Boolean> = MutableLiveData()

    private val isSeekable: MutableLiveData<Boolean> = MutableLiveData()

    private val isBuffering: MutableLiveData<Boolean> = MutableLiveData()

    private val isPlaying: MutableLiveData<Boolean> = MutableLiveData()

    private val metadataTitle: MutableLiveData<String?> = MutableLiveData()

    private val metadataSubtitle: MutableLiveData<String?> = MutableLiveData()

    private val error: MutableLiveData<String?> = MutableLiveData()

    private var seeking = false

    private val hideUITaskScheduler: Timer = Timer()

    private var hideUITask: TimerTask? = null

    init {
        resetUI()
    }

    fun getDuration(): LiveData<Float?> {
        return duration
    }

    fun getDurationText(): LiveData<String?> {
        return durationText
    }

    fun getCurrentTime(): LiveData<Float?> {
        return currentTime
    }

    fun getCurrentTimeText(): LiveData<String?> {
        return currentTimeText
    }

    fun getIsUIRequired(): LiveData<Boolean> {
        return isUIRequired
    }

    fun getIsSeekable(): LiveData<Boolean> {
        return isSeekable
    }

    fun getIsBuffering(): LiveData<Boolean> {
        return isBuffering
    }

    fun getIsPlaying(): LiveData<Boolean> {
        return isPlaying
    }

    fun getMetadataTitle(): LiveData<String?> {
        return metadataTitle
    }

    fun getMetadataSubtitle(): LiveData<String?> {
        return metadataSubtitle
    }

    fun getError(): LiveData<String?> {
        return error
    }

    fun resetUI() {
        duration.value = null
        durationText.value = null
        currentTime.value = null
        currentTimeText.value = null
        isUIRequired.value = true
        isSeekable.value = false
        isPlaying.value = false
        isBuffering.value = false
        metadataTitle.value = null
        metadataSubtitle.value = null
        error.value = null
        seeking = false
    }

    fun toggleUI() {
        if (!hasError() && java.lang.Boolean.TRUE == isUIRequired.value && canHideUI()) {
            isUIRequired.setValue(false)
        } else {
            isUIRequired.value = true
            scheduleHidingUI()
        }
    }

    fun setDuration(durationMs: Long) {
        setDuration((1e-03 * durationMs).toFloat())
    }

    fun setDuration(duration: Float) {
        this.duration.value = duration
        durationText.value = formatTimeValue(duration)
        isSeekable.value = true
    }

    fun setCurrentTime(currentTimeMs: Long) {
        setCurrentTime((1e-03 * currentTimeMs).toFloat())
    }

    fun setCurrentTime(currentTime: Float) {
        isBuffering.value = false
        if (!seeking) {
            this.currentTime.value = currentTime
        }
        currentTimeText.value = formatTimeValue(currentTime)
    }

    fun markBuffering() {
        isBuffering.value = true
    }

    fun markPlaying() {
        if (java.lang.Boolean.TRUE != isPlaying.value) {
            isBuffering.value = false
            isPlaying.value = true
            scheduleHidingUI()
        }
    }

    fun markPaused() {
        isPlaying.value = false
        toggleUI()
    }

    fun markSeeking() {
        seeking = true
    }

    fun markSought() {
        seeking = false
    }

    fun formatTimeValue(time: Float): String {
        val timeInSeconds = time.roundToInt()
        val timeFormat = if (TimeUnit.SECONDS.toHours(timeInSeconds.toLong()) > 0) "HH:mm:ss" else "mm:ss"
        val dateTime = Date(TimeUnit.SECONDS.toMillis(timeInSeconds.toLong()))
        return SimpleDateFormat(timeFormat, Locale.ENGLISH).format(dateTime)
    }

    fun setMetadata(title: String?, subtitle: String?) {
        metadataTitle.value = title
        metadataSubtitle.value = subtitle
    }

    fun setError(message: String?) {
        error.value = message
        if (hasError()) {
            isUIRequired.setValue(false)
        }
    }

    fun hasError(): Boolean {
        return !TextUtils.isEmpty(error.value)
    }

    private fun canHideUI(): Boolean {
        val shouldHideUI = java.lang.Boolean.TRUE == isPlaying.value
        val disallowHidingUI = java.lang.Boolean.TRUE == isBuffering.value || seeking
        return shouldHideUI && !disallowHidingUI
    }

    private fun scheduleHidingUI() {
        if (hideUITask != null) {
            hideUITask!!.cancel()
        }
        hideUITask = object : TimerTask() {
            override fun run() {
                if (canHideUI()) {
                    isUIRequired.postValue(false)
                }
            }
        }
        hideUITaskScheduler.schedule(hideUITask, AUTO_HIDE_UI_DELAY.toLong())
        hideUITaskScheduler.purge()
    }
}