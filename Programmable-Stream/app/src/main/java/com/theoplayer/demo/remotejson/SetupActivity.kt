package com.theoplayer.demo.remotejson;

import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.theoplayer.demo.remotejson.databinding.ActivitySetupBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;


public class SetupActivity extends AppCompatActivity {
    private ActivitySetupBinding viewBinding;
    private static final String TAG = PlayerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TheoTheme_Base);
        super.onCreate(savedInstanceState);

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup);

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar);

        // Configure UI behavior and default values.
        configureUI();
    }

    private void configureUI() {
        // Setting action on stream play request.
        viewBinding.configJsonFileUrl.setText(R.string.defaultJsonConfigUrl);
        viewBinding.playButton.setOnClickListener(playButton -> onPlay());
    }

    private void onPlay() {
        CharSequence configJsonUrl = viewBinding.configJsonFileUrl.getText();

        Log.d(TAG, "JSON URL: " + configJsonUrl);

        viewBinding.playButton.setEnabled(false);
        Thread thread = new Thread(this.createDownloadRemoteJsonJob(configJsonUrl.toString()));
        thread.start();
    }

    private Runnable createDownloadRemoteJsonJob(String url) {
        return () -> {
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } catch (Exception exception) {
                Log.d(TAG, "Remote JSON download failed", exception);
            }

            this.runOnUiThread(() -> {
                viewBinding.playButton.setEnabled(true);
                try {
                    // Reading the json file from given URL
                    JSONObject jsonObject = prepareJsonConfig(stringBuilder);
                    String playerConfiguration = jsonObject.getJSONObject("playerConfiguration").toString();
                    String source = jsonObject.getJSONObject("source").toString();
                    PlayerActivity.play(SetupActivity.this, playerConfiguration, source);
                } catch (JSONException exception) {
                    Log.e(TAG, "Not a valid JSON configuration.", exception);
                    SpannableString toastMessage = SpannableString.valueOf(this.getString(R.string.incorrectJsonConfiguration));
                    toastMessage.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, toastMessage.length(), 0);
                    Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
                }
            });
        };
    }

    private JSONObject prepareJsonConfig(StringBuilder jsonString) throws JSONException {
        // the JSON file should be stripped from analytics object, as this app doesn't support it
        JSONObject jsonObject = new JSONObject(jsonString.toString());
        jsonObject.getJSONObject("playerConfiguration").remove("analytics");
        jsonObject.getJSONObject("source").remove("analytics");
        return jsonObject;
    }

}
