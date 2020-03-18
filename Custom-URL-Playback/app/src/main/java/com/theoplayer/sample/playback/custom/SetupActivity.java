package com.theoplayer.sample.playback.custom;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.theoplayer.sample.playback.custom.databinding.ActivitySetupBinding;


public class SetupActivity extends AppCompatActivity {

    private ActivitySetupBinding viewBinding;

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
        // Defining default sources and its visibility constraints.
        viewBinding.sourceUrlTextInput.setTag(R.id.streamTypeClear, getString(R.string.defaultClearSourceUrl));
        viewBinding.sourceUrlTextInput.setTag(R.id.streamTypeProtected, getString(R.string.defaultProtectedSourceUrl));
        viewBinding.sourceUrlTextInput.setOnFocusChangeListener((view, hasFocus) -> onTextInputFocusChange((TextView) view));

        // Defining default licenses and its visibility constraints.
        viewBinding.licenseUrlTextInput.setTag(R.id.streamTypeProtected, getString(R.string.defaultProtectedLicenseUrl));
        viewBinding.licenseUrlTextInput.setOnFocusChangeListener((view, hasFocus) -> onTextInputFocusChange((TextView) view));

        // Checking default stream type and defining action on its change.
        viewBinding.streamTypesGroup.setOnCheckedChangeListener((group, checkedId) -> onStreamTypeChange(checkedId));
        viewBinding.streamTypesGroup.check(R.id.streamTypeClear);

        // Setting action on stream play request.
        viewBinding.playButton.setOnClickListener(playButton -> onPlay());
    }

    private void onTextInputFocusChange(TextView textInput) {
        changeDefaultValueVisibilityForTextInput(textInput);

        // Focus can be lost by clicking outside text input, in this case soft keyboard
        // needs to be hidden manually if not needed anymore.
        if (!viewBinding.sourceUrlTextInput.isFocused() && !viewBinding.licenseUrlTextInput.isFocused()) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
            }
        }
    }

    private void onStreamTypeChange(int checkedStreamTypeId) {
        changeDefaultValueForTextInput(viewBinding.sourceUrlTextInput, checkedStreamTypeId);
        changeDefaultValueVisibilityForTextInput(viewBinding.sourceUrlTextInput);

        changeDefaultValueForTextInput(viewBinding.licenseUrlTextInput, checkedStreamTypeId);
        changeDefaultValueVisibilityForTextInput(viewBinding.licenseUrlTextInput);

        boolean isProtectedStreamChecked = R.id.streamTypeProtected == checkedStreamTypeId;
        viewBinding.licenseUrlTextInput.setEnabled(isProtectedStreamChecked);
    }

    private void changeDefaultValueForTextInput(TextView textInput, int defaultValueId) {
        CharSequence currentDefaultValue = (CharSequence) textInput.getTag();
        CharSequence newDefaultValue = (CharSequence) textInput.getTag(defaultValueId);

        // Clearing currently displayed value if equal to default value.
        if (TextUtils.equals(textInput.getText(), currentDefaultValue)) {
            textInput.setText("");
        }

        // Changing default value according to given default value resource id.
        textInput.setTag(newDefaultValue);
    }

    private void changeDefaultValueVisibilityForTextInput(TextView textInput) {
        CharSequence defaultValue = (CharSequence) textInput.getTag();
        // Default value is displayed if text field is empty and not focused.
        if (TextUtils.equals(textInput.getText(), textInput.isFocused() ? defaultValue : "")) {
            textInput.setText(textInput.isFocused() ? "" : defaultValue);
        }
    }

    private void onPlay() {
        CharSequence sourceUrl = viewBinding.sourceUrlTextInput.getText();
        String defaultSourceUrl = (String) viewBinding.sourceUrlTextInput.getTag();

        CharSequence licenseUrl = viewBinding.licenseUrlTextInput.getText();
        String defaultLicenseUrl = (String) viewBinding.licenseUrlTextInput.getTag();

        PlayerActivity.play(
                this,
                TextUtils.isEmpty(sourceUrl) ? defaultSourceUrl : sourceUrl.toString(),
                TextUtils.isEmpty(licenseUrl) ? defaultLicenseUrl : licenseUrl.toString()
        );
    }

}
