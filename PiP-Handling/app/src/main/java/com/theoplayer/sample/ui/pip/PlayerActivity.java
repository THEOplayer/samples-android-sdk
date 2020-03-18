package com.theoplayer.sample.ui.pip;

import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.TypedSource;
import com.theoplayer.sample.ui.pip.databinding.ActivityPlayerBinding;

import static com.theoplayer.android.api.source.SourceDescription.Builder.sourceDescription;
import static com.theoplayer.android.api.source.TypedSource.Builder.typedSource;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    private static boolean SUPPORTS_PIP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

    private ActivityPlayerBinding viewBinding;
    private Player theoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TheoTheme_Base);
        super.onCreate(savedInstanceState);

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);

        // Gathering THEO objects references.
        theoPlayer = viewBinding.theoPlayerView.getPlayer();

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar);

        // Configuring THEOplayer playback with default parameters.
        configureTHEOplayer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.pipMenuItem == item.getItemId()) {
            tryEnterPictureInPictureMode();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (isInPictureInPictureMode) {
            getSupportActionBar().hide();
            viewBinding.theoPlayerView.getSettings().setFullScreenOrientationCoupled(false);
        } else {
            getSupportActionBar().show();
            viewBinding.theoPlayerView.getSettings().setFullScreenOrientationCoupled(true);
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }

    @Override
    protected void onUserLeaveHint() {
        tryEnterPictureInPictureMode();
    }

    private void tryEnterPictureInPictureMode() {
        if (SUPPORTS_PIP) {
            enterPictureInPictureMode(new PictureInPictureParams.Builder().build());
        } else {
            SpannableString toastMessage = SpannableString.valueOf(getString(R.string.pipNotSupported));
            toastMessage.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, toastMessage.length(), 0);
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void configureTHEOplayer() {
        // Coupling the orientation of the device with the fullscreen state.
        // The player will go fullscreen when the device is rotated to landscape
        // and will also exit fullscreen when the device is rotated back to portrait.
        viewBinding.theoPlayerView.getSettings().setFullScreenOrientationCoupled(true);

        // Creating a TypedSource builder that defines the location of a single stream source.
        TypedSource.Builder typedSource = typedSource(getString(R.string.defaultSourceUrl));

        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        SourceDescription.Builder sourceDescription = sourceDescription(typedSource.build())
                .poster(getString(R.string.defaultPosterUrl));

        // Configuring THEOplayer with defined SourceDescription object.
        theoPlayer.setSource(sourceDescription.build());

        // Adding listeners to THEOplayer basic playback events.
        theoPlayer.addEventListener(PlayerEventTypes.PLAY, event -> Log.i(TAG, "Event: PLAY"));
        theoPlayer.addEventListener(PlayerEventTypes.PLAYING, event -> Log.i(TAG, "Event: PLAYING"));
        theoPlayer.addEventListener(PlayerEventTypes.PAUSE, event -> Log.i(TAG, "Event: PAUSE"));
        theoPlayer.addEventListener(PlayerEventTypes.ENDED, event -> Log.i(TAG, "Event: ENDED"));
        theoPlayer.addEventListener(PlayerEventTypes.ERROR, event -> Log.i(TAG, "Event: ERROR, error=" + event.getError()));

        // Adding listeners to THEOplayer basic picture-in-picture changes events.
        theoPlayer.addEventListener(PlayerEventTypes.PRESENTATIONMODECHANGE, event -> Log.i(TAG, "Event: PRESENTATION_MODE_CHANGE"));
    }


    // In order to work properly and in sync with the activity lifecycle changes (e.g. device
    // is rotated, new activity is started or app is moved to background) we need to call
    // the "onResume", "onPause" and "onDestroy" methods of the THEOplayerView when the matching
    // activity methods are called.

    @Override
    protected void onPause() {
        super.onPause();
        if (SUPPORTS_PIP && !isInPictureInPictureMode()) {
            viewBinding.theoPlayerView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SUPPORTS_PIP && !isInPictureInPictureMode()) {
            viewBinding.theoPlayerView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewBinding.theoPlayerView.onDestroy();
    }

}
