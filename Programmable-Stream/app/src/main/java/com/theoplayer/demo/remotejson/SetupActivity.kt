package com.theoplayer.demo.remotejson

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.demo.remotejson.databinding.ActivitySetupBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
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
            var jsonConfiguration: String
            try {
                jsonConfiguration = URL(url).openStream().bufferedReader().use (BufferedReader::readText)
            } catch (exception: Exception) {
                jsonConfiguration = ""
                Log.d(TAG, "Remote JSON download failed", exception)
            }
            runOnUiThread {
                viewBinding.playButton.isEnabled = true
                try {
                    // Reading the json file from given URL
                    val jsonObject = prepareJsonConfig(jsonConfiguration)
                    val playerConfiguration =
                        jsonObject.getJSONObject("playerConfiguration").toString()
                    val source = jsonObject.getJSONObject("source").toString()
                    PlayerActivity.play(this@SetupActivity, playerConfiguration, source)
                } catch (exception: JSONException) {
                    Log.e(TAG, "Not a valid JSON configuration.", exception)
                    val toastMessage = this.getString(R.string.incorrectJsonConfiguration)
                    Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @Throws(JSONException::class)
    private fun prepareJsonConfig(jsonString: String): JSONObject {
        // the JSON file should be stripped from analytics object, as this app doesn't support it
        val jsonObject = JSONObject(jsonString)
        jsonObject.getJSONObject("playerConfiguration").remove("analytics")
        jsonObject.getJSONObject("source").remove("analytics")
        return jsonObject
    }

    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
    }
}