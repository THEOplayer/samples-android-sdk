package com.theoplayer.sample.ads.custom;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.theoplayer.sample.ads.custom.databinding.ActivitySetupBinding;


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
        // Defining default ads and its visibility constraints.
        viewBinding.adUrlTextInput.setTag(R.id.adStandardVast, getString(R.string.defaultVastAdUrl));
        viewBinding.adUrlTextInput.setTag(R.id.adStandardVmap, getString(R.string.defaultVmapAdUrl));
        viewBinding.adUrlTextInput.setOnFocusChangeListener((view, hasFocus) -> onTextInputFocusChange((TextView) view));

        // Defining default sources and its visibility constraints.
        viewBinding.sourceUrlTextInput.setTag(getString(R.string.defaultSourceUrl));
        viewBinding.sourceUrlTextInput.setOnFocusChangeListener((view, hasFocus) -> onTextInputFocusChange((TextView) view));

        // Defining ad placement values and checking default ad placement.
        viewBinding.adPlacementsGroup.setTag(R.id.adPlacementPreRoll, getString(R.string.defaultPreRollAddPlacement));
        viewBinding.adPlacementsGroup.setTag(R.id.adPlacementMidRoll, getString(R.string.defaultMidRollAddPlacement));
        viewBinding.adPlacementsGroup.setTag(R.id.adPlacementPostRoll, getString(R.string.defaultPostRollAddPlacement));
        viewBinding.adPlacementsGroup.check(R.id.adPlacementPreRoll);

        // Checking default ad standard and defining action on its change.
        viewBinding.adStandardsGroup.setOnCheckedChangeListener((group, checkedId) -> onAdStandardChange(checkedId));
        viewBinding.adStandardsGroup.check(R.id.adStandardVast);

        // Setting action on stream play request.
        viewBinding.playButton.setOnClickListener(playButton -> onPlay());
    }

    private void onTextInputFocusChange(TextView textInput) {
        changeDefaultValueVisibilityForTextInput(textInput);

        // Focus can be lost by clicking outside text input, in this case soft keyboard
        // needs to be hidden manually if not needed anymore.
        if (!viewBinding.sourceUrlTextInput.isFocused() && !viewBinding.adUrlTextInput.isFocused()) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
            }
        }
    }

    private void onAdStandardChange(int checkedAdStandardId) {
        changeDefaultValueForTextInput(viewBinding.adUrlTextInput, checkedAdStandardId);
        changeDefaultValueVisibilityForTextInput(viewBinding.adUrlTextInput);

        changeDefaultValueVisibilityForTextInput(viewBinding.sourceUrlTextInput);

        // VMAP standard already defines ad placement, redefining it has no sense.
        boolean isVmapNotChecked = R.id.adStandardVmap != checkedAdStandardId;
        for (int i = 0; i < viewBinding.adPlacementsGroup.getChildCount(); i++) {
            viewBinding.adPlacementsGroup.getChildAt(i).setEnabled(isVmapNotChecked);
        }
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

        CharSequence adUrl = viewBinding.adUrlTextInput.getText();
        String defaultAdUrl = (String) viewBinding.adUrlTextInput.getTag();

        String adTimeOffset = (String) viewBinding.adPlacementsGroup.getTag(viewBinding.adPlacementsGroup.getCheckedRadioButtonId());
        boolean isVmapChecked = viewBinding.adStandardVmap.isChecked();

        PlayerActivity.play(
                this,
                TextUtils.isEmpty(sourceUrl) ? defaultSourceUrl : sourceUrl.toString(),
                TextUtils.isEmpty(adUrl) ? defaultAdUrl : adUrl.toString(),
                isVmapChecked ? null : adTimeOffset
        );
    }

}
