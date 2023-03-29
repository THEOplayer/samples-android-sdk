package com.theoplayer.sample.playback.custom

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.sample.playback.custom.PlayerActivity.Companion.play
import com.theoplayer.sample.playback.custom.databinding.ActivitySetupBinding

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
        // Defining default sources and its visibility constraints.
        viewBinding.sourceUrlTextInput.setTag(
            R.id.streamTypeClear,
            getString(R.string.defaultClearSourceUrl)
        )
        viewBinding.sourceUrlTextInput.setTag(
            R.id.streamTypeProtected,
            getString(R.string.defaultProtectedSourceUrl)
        )
        viewBinding.sourceUrlTextInput.onFocusChangeListener =
            OnFocusChangeListener { view: View, hasFocus: Boolean -> onTextInputFocusChange(view as TextView) }

        // Defining default licenses and its visibility constraints.
        viewBinding.licenseUrlTextInput.setTag(
            R.id.streamTypeProtected,
            getString(R.string.defaultProtectedLicenseUrl)
        )
        viewBinding.licenseUrlTextInput.onFocusChangeListener =
            OnFocusChangeListener { view: View, hasFocus: Boolean -> onTextInputFocusChange(view as TextView) }

        // Checking default stream type and defining action on its change.
        viewBinding.streamTypesGroup.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            onStreamTypeChange(
                checkedId
            )
        }
        viewBinding.streamTypesGroup.check(R.id.streamTypeClear)

        // Setting action on stream play request.
        viewBinding.playButton.setOnClickListener { playButton: View? -> onPlay() }
    }

    private fun onTextInputFocusChange(textInput: TextView) {
        changeDefaultValueVisibilityForTextInput(textInput)

        // Focus can be lost by clicking outside text input, in this case soft keyboard
        // needs to be hidden manually if not needed anymore.
        if (!viewBinding.sourceUrlTextInput.isFocused && !viewBinding.licenseUrlTextInput.isFocused) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(textInput.windowToken, 0)
        }
    }

    private fun onStreamTypeChange(checkedStreamTypeId: Int) {
        changeDefaultValueForTextInput(viewBinding.sourceUrlTextInput, checkedStreamTypeId)
        changeDefaultValueVisibilityForTextInput(viewBinding.sourceUrlTextInput)
        changeDefaultValueForTextInput(viewBinding.licenseUrlTextInput, checkedStreamTypeId)
        changeDefaultValueVisibilityForTextInput(viewBinding.licenseUrlTextInput)
        val isProtectedStreamChecked = R.id.streamTypeProtected == checkedStreamTypeId
        viewBinding.licenseUrlTextInput.isEnabled = isProtectedStreamChecked
    }

    private fun changeDefaultValueForTextInput(textInput: TextView, defaultValueId: Int) {
        val currentDefaultValue = textInput.tag as CharSequence?
        val newDefaultValue = textInput.getTag(defaultValueId) as CharSequence?

        // Clearing currently displayed value if equal to default value.
        if (TextUtils.equals(textInput.text, currentDefaultValue)) {
            textInput.text = ""
        }

        // Changing default value according to given default value resource id.
        textInput.tag = newDefaultValue
    }

    private fun changeDefaultValueVisibilityForTextInput(textInput: TextView) {
        val defaultValue = textInput.tag as CharSequence?
        // Default value is displayed if text field is empty and not focused.
        if (TextUtils.equals(textInput.text, if (textInput.isFocused) defaultValue else "")) {
            textInput.text = if (textInput.isFocused) "" else defaultValue
        }
    }

    private fun onPlay() {
        val sourceUrl: CharSequence? = viewBinding.sourceUrlTextInput.text
        val defaultSourceUrl = viewBinding.sourceUrlTextInput.tag as String?
        val licenseUrl: CharSequence? = viewBinding.licenseUrlTextInput.text
        val defaultLicenseUrl = viewBinding.licenseUrlTextInput.tag as String?
        play(
            this,
            if (TextUtils.isEmpty(sourceUrl)) defaultSourceUrl else sourceUrl.toString(),
            if (TextUtils.isEmpty(licenseUrl)) defaultLicenseUrl else licenseUrl.toString()
        )
    }
}