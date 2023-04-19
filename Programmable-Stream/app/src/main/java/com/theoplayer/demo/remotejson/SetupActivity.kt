package com.theoplayer.demo.remotejson

import android.os.Bundle
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.demo.remotejson.databinding.ActivitySetupBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

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
        // Setting action on stream play request.
        viewBinding.configJsonFileUrl.setText(R.string.defaultJsonConfigUrl)
        viewBinding.playButton.setOnClickListener { onPlay() }
    }

    private fun onPlay() {
        val configJsonUrl: CharSequence? = viewBinding.configJsonFileUrl.text
        Log.d(TAG, "JSON URL: $configJsonUrl")
        viewBinding.playButton.isEnabled = false
        val thread = Thread(createDownloadRemoteJsonJob(configJsonUrl.toString()))
        thread.start()
    }

    private fun createDownloadRemoteJsonJob(url: String): Runnable {
        return Runnable {
            val stringBuilder = StringBuilder()
            try {
                BufferedReader(InputStreamReader(URL(url).openStream())).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                }
            } catch (exception: Exception) {
                Log.d(TAG, "Remote JSON download failed", exception)
            }
            runOnUiThread {
                viewBinding.playButton.isEnabled = true
                try {
                    // Reading the json file from given URL
                    val jsonObject = prepareJsonConfig(stringBuilder)
                    val playerConfiguration =
                        jsonObject.getJSONObject("playerConfiguration").toString()
                    val source = jsonObject.getJSONObject("source").toString()
                    PlayerActivity.play(this@SetupActivity, playerConfiguration, source)
                } catch (exception: JSONException) {
                    Log.e(TAG, "Not a valid JSON configuration.", exception)
                    val toastMessage =
                        SpannableString.valueOf(this.getString(R.string.incorrectJsonConfiguration))
                    toastMessage.setSpan(
                        AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                        0,
                        toastMessage.length,
                        0
                    )
                    Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @Throws(JSONException::class)
    private fun prepareJsonConfig(jsonString: StringBuilder): JSONObject {
        // the JSON file should be stripped from analytics object, as this app doesn't support it
        val jsonObject = JSONObject(jsonString.toString())
        jsonObject.getJSONObject("playerConfiguration").remove("analytics")
        jsonObject.getJSONObject("source").remove("analytics")
        return jsonObject
    }

    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
    }
}