package com.theoplayer.sample.ui.custom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PlayerViewModel : ViewModel() {
    /**
     * Stream duration, indicates max value of progress slider.
     */
    private val duration: MutableLiveData<Float?> = MutableLiveData()

    /**
     * Stream duration as text in user friendly format.
     */
    private val durationText: MutableLiveData<String?> = MutableLiveData()

    /**
     * Stream current position, indicates position of progress slider.
     */
    private val currentTime: MutableLiveData<Float?> = MutableLiveData()

    /**
     * Stream current position as text in user friendly format.
     */
    private val currentTimeText: MutableLiveData<String?> = MutableLiveData()

    /**
     * Indicates UI visibility requirement. If UI is not required, all player controls are hidden.
     */
    private val isUIRequired: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * Indicates possibility of changing stream position. If `true` than controls like
     * skip forward button, skip backward button, progress slider and progress labels can be shown.
     */
    private val isSeekable: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * Indicates that player is fetching data. If `true` than loading spinner is displayed
     * instead of play/pause button. It doesn't influence on seekable controls visibility.
     */
    private val isBuffering: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * Indicates that stream is being played. Used to decide what action should be triggered by
     * big play/pause button.
     */
    private val isPlaying: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * Indicates the user touched and moves progress slider in order to change stream position.
     */
    private var seeking = false

    /**
     * Timer that allows to schedule hiding UI tasks.
     */
    private val hideUITaskScheduler: Timer = Timer()

    /**
     * Hiding UI task to be scheduled.
     */
    private var hideUITask: TimerTask? = null

    //region LiveData getters
    // All LiveData getters are used in activity_player.xml.
    // View binding mechanism is leveraged to update view state accordingly.
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
    //endregion
    /**
     * Resets UI state.
     *
     *
     * Called when the THEOplayer source was changed.
     */
    fun resetUI() {
        duration.value = null
        durationText.value = null
        currentTime.value = null
        currentTimeText.value = null
        isUIRequired.value = true
        isSeekable.value = false
        isPlaying.value = false
        isBuffering.value = false
        seeking = false
    }

    /**
     * Toggle player controls visibility.
     *
     *
     * Called when user make a tap gesture on player clickable view/overlay.
     *
     *
     * If controls are shown and there's no objections about hiding them, then they will be hidden.
     * Otherwise player controls will be shown the the user and it's hiding request will be delayed
     * by given amount of time.
     */
    fun toggleUI() {
        if (java.lang.Boolean.TRUE == isUIRequired.value && canHideUI()) {
            isUIRequired.setValue(false)
        } else {
            isUIRequired.setValue(true)
            scheduleHidingUI()
        }
    }

    /**
     * Sets source duration.
     *
     *
     * Called when the THEOplayer loaded the source to the point that duration is known.
     *
     * @param duration source duration in seconds.
     */
    fun setDuration(duration: Double) {
        this.duration.value = duration.toFloat()
        durationText.value = formatTimeValue(duration)
        isSeekable.value = true
    }

    /**
     * Sets current time (position) of played stream.
     *
     *
     * Called when the THEOplayer changes stream position because of its playback.
     * Can be called also when changing stream position manually, which is by clicking on
     * skip forward/backward buttons or by changing position of progress slider.
     *
     * @param currentTime stream current position in seconds.
     */
    fun setCurrentTime(currentTime: Double) {
        isBuffering.value = false
        if (!seeking) {
            this.currentTime.value = currentTime.toFloat()
        }
        currentTimeText.value = formatTimeValue(currentTime)
    }

    /**
     * Indicates entering buffering state.
     *
     *
     * Called when THEOplayer has not enough data for continuous playback.
     */
    fun markBuffering() {
        isBuffering.value = true
    }

    /**
     * Indicates entering playing state.
     *
     *
     * Called when THEOplayer starts playing stream.
     */
    fun markPlaying() {
        isBuffering.value = false
        isPlaying.value = true
        scheduleHidingUI()
    }

    /**
     * Indicates exiting playing state.
     *
     *
     * Called when the stream playback is paused.
     */
    fun markPaused() {
        isPlaying.value = false
    }

    /**
     * Indicates entering seeking state.
     *
     *
     * Called when the user touches progress slider in order to change stream position.
     */
    fun markSeeking() {
        seeking = true
    }

    /**
     * Indicates exiting seeking state.
     *
     *
     * Called when the user releases progress slider, new stream position is considered as chosen.
     */
    fun markSought() {
        seeking = false
    }

    /**
     * Converts given time to 'HH:mm:ss' or 'mm:ss' format. It depends on how long given time
     * interval is. If it doesn't exceed on hour than 'HH' part is omitted.
     *
     * @param time time interval in seconds.
     * @return Time converted to 'HH:mm:ss' or 'mm:ss' format.
     */
    fun formatTimeValue(time: Double): String {
        val timeInSeconds = Math.round(time)
        val timeFormat = if (TimeUnit.SECONDS.toHours(timeInSeconds) > 0) "HH:mm:ss" else "mm:ss"
        val dateTime = Date(TimeUnit.SECONDS.toMillis(timeInSeconds))
        return SimpleDateFormat(timeFormat, Locale.ENGLISH).format(dateTime)
    }

    /**
     * Tells whether the player controls can be hidden or not.
     *
     *
     * The main rule is that player controls can be hidden when stream is being played.
     *
     *
     * There are exception from the main rule. When the user selects new stream position using
     * progress slider or THEOplayer starts buffering data, then hiding player controls is
     * disallowed.
     *
     * @return `true` if player controls can be hidden; `false` otherwise.
     */
    private fun canHideUI(): Boolean {
        val shouldHideUI = java.lang.Boolean.TRUE == isPlaying.value
        val disallowHidingUI = java.lang.Boolean.TRUE == isBuffering.value || seeking
        return shouldHideUI && !disallowHidingUI
    }

    /**
     * Schedules hiding player controls after specific amount of time.
     *
     *
     * If there such task is already scheduled, than it's canceled and new task is scheduled instead.
     * This means that player controls will be hided after specific amount of time, but the countdown
     * starts after last user interaction with UI.
     */
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

    companion object {
        private const val AUTO_HIDE_UI_DELAY = 5000
    }
}