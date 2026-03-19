package com.theoplayer.sample.localization

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.rememberPlayer
import com.theoplayer.android.ui.theme.THEOplayerTheme
import com.theoplayer.sample.common.AppTopBar
import com.theoplayer.sample.common.SourceManager

private data class LanguageOption(
    val code: String,
    val label: String,
    val flag: String
)

private val LANGUAGES = listOf(
    LanguageOption("en", "English", "\uD83C\uDDEC\uD83C\uDDE7"),
    LanguageOption("fr", "Français", "\uD83C\uDDEB\uD83C\uDDF7"),
    LanguageOption("es", "Español", "\uD83C\uDDEA\uD83C\uDDF8"),
    LanguageOption("de", "Deutsch", "\uD83C\uDDE9\uD83C\uDDEA"),
    LanguageOption("nl-BE", "Nederlands (BE)", "\uD83C\uDDE7\uD83C\uDDEA"),
    LanguageOption("it", "Italiano", "\uD83C\uDDEE\uD83C\uDDF9"),
    LanguageOption("tr", "Türkçe", "\uD83C\uDDF9\uD83C\uDDF7"),
    LanguageOption("ja", "日本語", "\uD83C\uDDEF\uD83C\uDDF5"),
    LanguageOption("ar", "العربية", "\uD83C\uDDF8\uD83C\uDDE6"),
)

class PlayerActivity : AppCompatActivity() {

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable all debug logs from THEOplayer.
        THEOplayerGlobal.getSharedInstance(this).logger.enableAllTags()

        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val theoplayerView = remember(context) {
                // Creating the player with default parameters.
                THEOplayerView(context, THEOplayerConfig.Builder().build()).apply {
                    // Keep the device screen on.
                    keepScreenOn = true
                }
            }
            val player = rememberPlayer(theoplayerView)
            val theoPlayer = theoplayerView.player

            val configuration = LocalConfiguration.current
            var selectedLocale by rememberSaveable(configuration) {
                mutableStateOf(
                    // Read the user's preferred locale from the app settings,
                    // or fall back to the default language.
                    ConfigurationCompat.getLocales(configuration).get(0)?.toLanguageTag()
                        ?: LANGUAGES.first().code
                )
            }

            LaunchedEffect(player) {

                // Configuring the player with a SourceDescription object.
                theoPlayer.source = SourceManager.TEARS_OF_STEEL_HLS

                //  Set autoplay to start video whenever player is visible.
                theoPlayer.isAutoplay = true

                // Attach event listeners.
                theoPlayer.addEventListener(PlayerEventTypes.SOURCECHANGE) {
                    Log.i(TAG, "Event: SOURCECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.CURRENTSOURCECHANGE) {
                    Log.i(TAG, "Event: CURRENTSOURCECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.LOADEDDATA) {
                    Log.i(TAG, "Event: LOADEDDATA")
                }
                theoPlayer.addEventListener(PlayerEventTypes.LOADEDMETADATA) {
                    Log.i(TAG, "Event: LOADEDMETADATA")
                }
                theoPlayer.addEventListener(PlayerEventTypes.DURATIONCHANGE) {
                    Log.i(TAG, "Event: DURATIONCHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.TIMEUPDATE) {
//                    Log.i(TAG, "Event: TIMEUPDATE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.PLAY) {
                    Log.i(TAG, "Event: PLAY")
                }
                theoPlayer.addEventListener(PlayerEventTypes.PLAYING) {
                    Log.i(TAG, "Event: PLAYING")
                }
                theoPlayer.addEventListener(PlayerEventTypes.PAUSE) {
                    Log.i(TAG, "Event: PAUSE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.SEEKING) {
                    Log.i(TAG, "Event: SEEKING")
                }
                theoPlayer.addEventListener(PlayerEventTypes.SEEKED) {
                    Log.i(TAG, "Event: SEEKED")
                }
                theoPlayer.addEventListener(PlayerEventTypes.WAITING) {
                    Log.i(TAG, "Event: WAITING")
                }
                theoPlayer.addEventListener(PlayerEventTypes.READYSTATECHANGE) {
                    Log.i(TAG, "Event: READYSTATECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.PRESENTATIONMODECHANGE) {
                    Log.i(TAG, "Event: PRESENTATIONMODECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.VOLUMECHANGE) {
                    Log.i(TAG, "Event: VOLUMECHANGE")
                }
                theoPlayer.addEventListener(PlayerEventTypes.ERROR) { event: ErrorEvent ->
                    Log.i(TAG, "Event: ERROR, error=" + event.errorObject)
                }
            }

            THEOplayerTheme(useDarkTheme = true) {
                Scaffold(
                    topBar = { AppTopBar() }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .background(Color(0xFF121212))
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DefaultUI(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f),
                                player = player
                            )
                        }

                        HorizontalDivider(color = Color(0xFF333333))

                        // Language selection.
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Select Language",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Changes the Open Video UI language using Android resource localization.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (lang in LANGUAGES) {
                                    LanguageButton(
                                        flag = lang.flag,
                                        label = lang.label,
                                        isSelected = selectedLocale == lang.code,
                                        onClick = {
                                            selectedLocale = lang.code
                                            setAppLanguage(lang.code)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setAppLanguage(code: String) {
        val locale = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(locale)
    }

    companion object {
        private val TAG: String = PlayerActivity::class.java.simpleName
    }
}

@Composable
private fun LanguageButton(
    modifier: Modifier = Modifier,
    flag: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color(0xFF6200EA) else Color(0xFF2A2A2A)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF9D46FF) else Color(0xFF444444),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                if (!isSelected) {
                    onClick()
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "$flag  $label",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}
