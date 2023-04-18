package com.theoplayer.sample.ads.custom

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.sample.ads.custom.PlayerActivity.Companion.play
import com.theoplayer.sample.ads.custom.databinding.ActivitySetupBinding

class SetupActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivitySetupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TheoTheme_Base)
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding class.
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup)

        // Configuring action bar.
        setSupportActionBar(viewBinding.toolbarLayout.toolbar)

        // Configure UI behavior and default values.
        configureUI()
    }

    private fun configureUI() {
        // Defining default ads and its visibility constraints.
        viewBinding.adUrlTextInput.setTag(
            R.id.adStandardVast,
            getString(R.string.defaultVastAdUrl)
        )
        viewBinding.adUrlTextInput.setTag(
            R.id.adStandardVmap,
            getString(R.string.defaultVmapAdUrl)
        )
        viewBinding.adUrlTextInput.onFocusChangeListener =
            OnFocusChangeListener { view: View, hasFocus: Boolean -> onTextInputFocusChange(view as TextView) }

        // Defining default sources and its visibility constraints.
        viewBinding.sourceUrlTextInput.tag = getString(R.string.defaultSourceUrl)
        viewBinding.sourceUrlTextInput.onFocusChangeListener =
            OnFocusChangeListener { view: View, hasFocus: Boolean -> onTextInputFocusChange(view as TextView) }

        // Defining ad placement values and checking default ad placement.
        viewBinding.adPlacementsGroup.setTag(
            R.id.adPlacementPreRoll,
            getString(R.string.defaultPreRollAddPlacement)
        )
        viewBinding.adPlacementsGroup.setTag(
            R.id.adPlacementMidRoll,
            getString(R.string.defaultMidRollAddPlacement)
        )
        viewBinding.adPlacementsGroup.setTag(
            R.id.adPlacementPostRoll,
            getString(R.string.defaultPostRollAddPlacement)
        )
        viewBinding.adPlacementsGroup.check(R.id.adPlacementPreRoll)

        // Checking default ad standard and defining action on its change.
        viewBinding.adStandardsGroup.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            onAdStandardChange(
                checkedId
            )
        }
        viewBinding.adStandardsGroup.check(R.id.adStandardVast)

        // Setting action on stream play request.
        viewBinding.playButton.setOnClickListener { playButton: View? -> onPlay() }
    }

    private fun onTextInputFocusChange(textInput: TextView) {
        changeDefaultValueVisibilityForTextInput(textInput)

        // Focus can be lost by clicking outside text input, in this case soft keyboard
        // needs to be hidden manually if not needed anymore.
        if (!viewBinding.sourceUrlTextInput.isFocused && !viewBinding.adUrlTextInput.isFocused) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(textInput.windowToken, 0)
        }
    }

    private fun onAdStandardChange(checkedAdStandardId: Int) {
        changeDefaultValueForTextInput(viewBinding.adUrlTextInput, checkedAdStandardId)
        changeDefaultValueVisibilityForTextInput(viewBinding.adUrlTextInput)
        changeDefaultValueVisibilityForTextInput(viewBinding.sourceUrlTextInput)

        // VMAP standard already defines ad placement, redefining it has no sense.
        val isVmapNotChecked = R.id.adStandardVmap != checkedAdStandardId
        for (i in 0 until viewBinding.adPlacementsGroup.childCount) {
            viewBinding.adPlacementsGroup.getChildAt(i).isEnabled = isVmapNotChecked
        }
    }

    private fun changeDefaultValueForTextInput(textInput: TextView, defaultValueId: Int) {
        val currentDefaultValue = (textInput.tag ?: "") as CharSequence
        val newDefaultValue = textInput.getTag(defaultValueId) as CharSequence

        // Clearing currently displayed value if equal to default value.
        if (TextUtils.equals(textInput.text, currentDefaultValue)) {
            textInput.text = ""
        }

        // Changing default value according to given default value resource id.
        textInput.tag = newDefaultValue
    }

    private fun changeDefaultValueVisibilityForTextInput(textInput: TextView) {
        val defaultValue = (textInput.tag ?: "")  as CharSequence
        // Default value is displayed if text field is empty and not focused.
        if (TextUtils.equals(textInput.text, if (textInput.isFocused) defaultValue else "")) {
            textInput.text = if (textInput.isFocused) "" else defaultValue
        }
    }

    private fun onPlay() {
        val sourceUrl: CharSequence? = viewBinding.sourceUrlTextInput.text
        val defaultSourceUrl = viewBinding.sourceUrlTextInput.tag as String
        val adUrl: CharSequence? = viewBinding.adUrlTextInput.text
        val defaultAdUrl = viewBinding.adUrlTextInput.tag as String
        val adTimeOffset =
            viewBinding.adPlacementsGroup.getTag(viewBinding.adPlacementsGroup.checkedRadioButtonId) as String
        val isVmapChecked = viewBinding.adStandardVmap.isChecked
        play(
            this,
            if (TextUtils.isEmpty(sourceUrl)) defaultSourceUrl else sourceUrl.toString(),
            if (TextUtils.isEmpty(adUrl)) defaultAdUrl else adUrl.toString(),
            if (isVmapChecked) "" else adTimeOffset
        )
    }
}