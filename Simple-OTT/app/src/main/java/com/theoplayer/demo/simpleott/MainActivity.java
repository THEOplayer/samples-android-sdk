
package com.theoplayer.demo.simpleott;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.theoplayer.android.api.THEOplayerGlobal;
import com.theoplayer.android.api.cache.Cache;
import com.theoplayer.demo.simpleott.databinding.ActivityMainBinding;
import com.theoplayer.demo.simpleott.datamodel.SimpleOTTConfiguration;
import com.theoplayer.demo.simpleott.network.SimpleOTTWifiMonitor;
import com.theoplayer.demo.simpleott.network.WifiMonitor;
import com.theoplayer.demo.simpleott.network.WifiMonitorLegacy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class MainActivity extends AppCompatActivity {

    private SimpleOTTConfiguration simpleOTTConfiguration;
    private MutableLiveData<Boolean> onlyOnWifi = new MutableLiveData<>();
    private SharedPreferences sharedPref;
    private OfflineHandler offlineHandler;
    private OfflineAssetListAdapter offlineListAdapter;
    private SimpleOTTWifiMonitor wifiMonitor;

    // Reading configuration from the JSON file embedded in the application.
    private SimpleOTTConfiguration readConfiguration() {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try (
                InputStream is = getResources().openRawResource(R.raw.config);
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))
        ) {
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            String jsonString = writer.toString();
            // Casting JSON config into Java object for future use
            return new Gson().fromJson(jsonString, SimpleOTTConfiguration.class);
        } catch (IOException ignored) {
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TheoTheme_Base);
        super.onCreate(savedInstanceState);
        readSettings();

        // Choosing a WiFi monitor depending on the SDK version
        // to be later use by the OfflineHandler
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            wifiMonitor = new WifiMonitor();
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkRequest networkRequestWiFi = (new NetworkRequest.Builder()).addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build();

            connectivityManager.registerNetworkCallback(networkRequestWiFi, (ConnectivityManager.NetworkCallback) wifiMonitor);
        } else {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            wifiMonitor = new WifiMonitorLegacy();
            registerReceiver((BroadcastReceiver) wifiMonitor, filter);
        }

        // Inflating view and obtaining an instance of the binding class.
        ActivityMainBinding viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        simpleOTTConfiguration = readConfiguration();

        // Initializing page adapter for tabs view
        SimpleOTTPageAdapter pagerAdapter = new SimpleOTTPageAdapter(this);
        viewBinding.viewpager.setAdapter(pagerAdapter);

        // Loading all pages at once

        viewBinding.viewpager.setOffscreenPageLimit(4);
        pagerAdapter.setOnItemsReady(() -> {

            prepareUi();

            // Gathering THEO objects references.
            Cache theoCache = THEOplayerGlobal.getSharedInstance(this).getCache();

            // Initializing offline handler
            offlineHandler = new OfflineHandler(
                    MainActivity.this,
                    theoCache,
                    simpleOTTConfiguration.config.offline.vods,
                    onlyOnWifi,
                    wifiMonitor.getIsConnectedLiveData());

            // The list adapter for "Offline" tab
            offlineListAdapter = new OfflineAssetListAdapter(MainActivity.this, offlineHandler.getOfflineSources(), offlineHandler);

            ListView offlineList = findViewById(R.id.offline_list);
            offlineList.setAdapter(offlineListAdapter);

            offlineHandler.init();
        });
    }

    private void prepareUi() {
        // Preparing the list adapter for Live tab
        AssetListAdapter liveListAdapter = new AssetListAdapter(MainActivity.this, simpleOTTConfiguration.config.live.channels);
        ListView liveList = findViewById(R.id.channel_list);
        liveList.setAdapter(liveListAdapter);
        liveList.setOnItemClickListener((parent, view, position, id) ->
                FullScreenPlayerActivity.play(MainActivity.this, simpleOTTConfiguration.config.live.channels[position].videoSource));

        // Preparing the list adapter for On Demand tab
        AssetListAdapter vodListAdapter = new AssetListAdapter(MainActivity.this, simpleOTTConfiguration.config.onDemand.vods);
        ListView vodList = findViewById(R.id.vod_list);
        vodList.setAdapter(vodListAdapter);
        vodList.setOnItemClickListener((parent, view, position, id) ->
                FullScreenPlayerActivity.play(MainActivity.this, simpleOTTConfiguration.config.onDemand.vods[position].videoSource));

        // Showing confirmation dialog after hitting clear cache button
        MaterialButton clearCacheBtn = findViewById(R.id.clear_cache_btn);
        clearCacheBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(MainActivity.this)
                .setMessage(getString(R.string.clearAllDownloadsMessage))
                .setPositiveButton(getString(R.string.yes), (dialog, which) ->
                        offlineHandler.deleteAllCachedItems())
                .setNegativeButton(getString(R.string.no), null)
                .show());

        // The switch for "Download only on wifi" setting
        // When this is changed, the `onlyOnWifi` live data gets updated
        SwitchMaterial wifiSwitch = findViewById(R.id.wifi_switch);
        wifiSwitch.setChecked(onlyOnWifi.getValue());
        wifiSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onlyOnWifi.setValue(isChecked);
            storeSettings();
        });
    }


    // Reading "Download only on wifi" setting from shared preferences
    private void readSettings() {
        Context context = this;
        sharedPref = context.getSharedPreferences(
                getString(R.string.settings), Context.MODE_PRIVATE);
        onlyOnWifi.setValue(sharedPref.getBoolean(getString(R.string.settings_wifi), true));
    }

    // Storing "Download only on wifi" setting in shared preferences
    private void storeSettings() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.settings_wifi), onlyOnWifi.getValue());
        editor.apply();
    }
}
