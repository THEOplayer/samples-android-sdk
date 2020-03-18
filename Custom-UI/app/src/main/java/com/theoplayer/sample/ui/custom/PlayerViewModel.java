package com.theoplayer.sample.ui.custom;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Boolean.TRUE;
import static java.util.Locale.ENGLISH;
import static java.util.concurrent.TimeUnit.SECONDS;

public class PlayerViewModel extends ViewModel {

    private static final int AUTO_HIDE_UI_DELAY = 5_000;

    /**
     * Stream duration, indicates max value of progress slider.
     */
    private MutableLiveData<Float> duration;

    /**
     * Stream duration as text in user friendly format.
     */
    private MutableLiveData<String> durationText;

    /**
     * Stream current position, indicates position of progress slider.
     */
    private MutableLiveData<Float> currentTime;

    /**
     * Stream current position as text in user friendly format.
     */
    private MutableLiveData<String> currentTimeText;

    /**
     * Indicates UI visibility requirement. If UI is not required, all player controls are hidden.
     */
    private MutableLiveData<Boolean> isUIRequired;

    /**
     * Indicates possibility of changing stream position. If <code>true</code> than controls like
     * skip forward button, skip backward button, progress slider and progress labels can be shown.
     */
    private MutableLiveData<Boolean> isSeekable;

    /**
     * Indicates that player is fetching data. If <code>true</code> than loading spinner is displayed
     * instead of play/pause button. It doesn't influence on seekable controls visibility.
     */
    private MutableLiveData<Boolean> isBuffering;

    /**
     * Indicates that stream is being played. Used to decide what action should be triggered by
     * big play/pause button.
     */
    private MutableLiveData<Boolean> isPlaying;

    /**
     * Indicates the user touched and moves progress slider in order to change stream position.
     */
    private boolean seeking;

    /**
     * Timer that allows to schedule hiding UI tasks.
     */
    private Timer hideUITaskScheduler;

    /**
     * Hiding UI task to be scheduled.
     */
    private TimerTask hideUITask;

    public PlayerViewModel() {
        this.duration = new MutableLiveData<>();
        this.durationText = new MutableLiveData<>();
        this.currentTime = new MutableLiveData<>();
        this.currentTimeText = new MutableLiveData<>();

        this.isUIRequired = new MutableLiveData<>();
        this.isSeekable = new MutableLiveData<>();
        this.isBuffering = new MutableLiveData<>();
        this.isPlaying = new MutableLiveData<>();

        this.hideUITaskScheduler = new Timer();
    }


    //region LiveData getters

    // All LiveData getters are used in activity_player.xml.
    // View binding mechanism is leveraged to update view state accordingly.

    public LiveData<Float> getDuration() {
        return duration;
    }

    public LiveData<String> getDurationText() {
        return durationText;
    }

    public LiveData<Float> getCurrentTime() {
        return currentTime;
    }

    public LiveData<String> getCurrentTimeText() {
        return currentTimeText;
    }

    public LiveData<Boolean> getIsUIRequired() {
        return isUIRequired;
    }

    public LiveData<Boolean> getIsSeekable() {
        return isSeekable;
    }

    public LiveData<Boolean> getIsBuffering() {
        return isBuffering;
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    //endregion


    /**
     * Resets UI state.
     * <p/>
     * Called when the THEOplayer source was changed.
     */
    void resetUI() {
        this.duration.setValue(null);
        this.durationText.setValue(null);
        this.currentTime.setValue(null);
        this.currentTimeText.setValue(null);
        this.isUIRequired.setValue(true);
        this.isSeekable.setValue(false);
        this.isPlaying.setValue(false);
        this.isBuffering.setValue(false);
        this.seeking = false;
    }

    /**
     * Toggle player controls visibility.
     * <p/>
     * Called when user make a tap gesture on player clickable view/overlay.
     * <p/>
     * If controls are shown and there's no objections about hiding them, then they will be hidden.
     * Otherwise player controls will be shown the the user and it's hiding request will be delayed
     * by given amount of time.
     */
    void toggleUI() {
        if (TRUE.equals(isUIRequired.getValue()) && canHideUI()) {
            isUIRequired.setValue(false);
        } else {
            isUIRequired.setValue(true);
            scheduleHidingUI();
        }
    }

    /**
     * Sets source duration.
     * <p/>
     * Called when the THEOplayer loaded the source to the point that duration is known.
     *
     * @param duration source duration in seconds.
     */
    void setDuration(double duration) {
        this.duration.setValue((float) duration);
        this.durationText.setValue(formatTimeValue(duration));
        this.isSeekable.setValue(true);
    }

    /**
     * Sets current time (position) of played stream.
     * <p/>
     * Called when the THEOplayer changes stream position because of its playback.
     * Can be called also when changing stream position manually, which is by clicking on
     * skip forward/backward buttons or by changing position of progress slider.
     *
     * @param currentTime stream current position in seconds.
     */
    void setCurrentTime(double currentTime) {
        this.isBuffering.setValue(false);
        if (!seeking) {
            this.currentTime.setValue((float) currentTime);
        }
        this.currentTimeText.setValue(formatTimeValue(currentTime));
    }

    /**
     * Indicates entering buffering state.
     * <p/>
     * Called when THEOplayer has not enough data for continuous playback.
     */
    void markBuffering() {
        this.isBuffering.setValue(true);
    }

    /**
     * Indicates entering playing state.
     * <p/>
     * Called when THEOplayer starts playing stream.
     */
    void markPlaying() {
        this.isBuffering.setValue(false);
        this.isPlaying.setValue(true);
        scheduleHidingUI();
    }

    /**
     * Indicates exiting playing state.
     * <p/>
     * Called when the stream playback is paused.
     */
    void markPaused() {
        this.isPlaying.setValue(false);
    }

    /**
     * Indicates entering seeking state.
     * <p/>
     * Called when the user touches progress slider in order to change stream position.
     */
    void markSeeking() {
        this.seeking = true;
    }

    /**
     * Indicates exiting seeking state.
     * <p/>
     * Called when the user releases progress slider, new stream position is considered as chosen.
     */
    void markSought() {
        this.seeking = false;
    }

    /**
     * Converts given time to 'HH:mm:ss' or 'mm:ss' format. It depends on how long given time
     * interval is. If it doesn't exceed on hour than 'HH' part is omitted.
     *
     * @param time time interval in seconds.
     * @return Time converted to 'HH:mm:ss' or 'mm:ss' format.
     */
    @NonNull
    String formatTimeValue(double time) {
        long timeInSeconds = Math.round(time);
        String timeFormat = (SECONDS.toHours(timeInSeconds) > 0) ? "HH:mm:ss" : "mm:ss";
        Date dateTime = new Date(SECONDS.toMillis(timeInSeconds));
        return new SimpleDateFormat(timeFormat, ENGLISH).format(dateTime);
    }

    /**
     * Tells whether the player controls can be hidden or not.
     * <p/>
     * The main rule is that player controls can be hidden when stream is being played.
     * <p/>
     * There are exception from the main rule. When the user selects new stream position using
     * progress slider or THEOplayer starts buffering data, then hiding player controls is
     * disallowed.
     *
     * @return <code>true</code> if player controls can be hidden; <code>false</code> otherwise.
     */
    private boolean canHideUI() {
        boolean shouldHideUI = TRUE.equals(isPlaying.getValue());
        boolean disallowHidingUI = TRUE.equals(isBuffering.getValue()) || seeking;
        return shouldHideUI && !disallowHidingUI;
    }

    /**
     * Schedules hiding player controls after specific amount of time.
     * <p/>
     * If there such task is already scheduled, than it's canceled and new task is scheduled instead.
     * This means that player controls will be hided after specific amount of time, but the countdown
     * starts after last user interaction with UI.
     */
    private void scheduleHidingUI() {
        if (hideUITask != null) {
            hideUITask.cancel();
        }

        hideUITask = new TimerTask() {
            @Override
            public void run() {
                if (canHideUI()) {
                    isUIRequired.postValue(false);
                }
            }
        };
        hideUITaskScheduler.schedule(hideUITask, AUTO_HIDE_UI_DELAY);
        hideUITaskScheduler.purge();
    }

}
