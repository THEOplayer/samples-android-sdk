package com.theoplayer.demo.simpleott.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * This class is responsible for keeping WiFi connection state info.
 * <p/>
 * It registers WiFi monitor to keep application informed about WiFi connectivity changes.
 * <p/>
 * It keep setting about download allowance depending on WiFi connectivity state. This value is
 * also persisted in <code>SharedPreferences</code>, so it can be restored after application restart.
 * <p/>
 * Appropriate <code>LiveData</code> objects are provided to observe WiFi network info changes.
 */
public class WiFiNetworkInfo {

    private static final String SETTINGS_FILE = "wifiNetworkInfoSettings";
    private static final String SETTING_DOWNLOAD_ONLY_ON_WIFI = "downloadOnlyOnWiFi";

    /**
     * Keeps information about WiFi connectivity state.
     */
    private MutableLiveData<Boolean> connectedToWiFi = new MutableLiveData<>();

    /**
     * Keeps information about download allowance depending on WiFi connectivity state.
     */
    private MutableLiveData<Boolean> downloadOnlyOnWiFi = new MutableLiveData<>();

    public WiFiNetworkInfo(Context context) {
        // Choosing a Wifi monitor depending on the Android API version.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            registerModernWifiMonitor(context);
        } else {
            registerLegacyWifiMonitor(context);
        }

        loadDownloadOnlyOnWiFiValue(context);
    }

    /**
     * Provides WiFi connectivity state holder that can be observed.
     *
     * @return WiFi connectivity state holder.
     */
    public LiveData<Boolean> connectedToWiFi() {
        return connectedToWiFi;
    }

    /**
     * Returns current WiFi connectivity state.
     *
     * @return <code>true</code> if WiFi is connected; <code>false</code> otherwise.
     */
    public boolean isConnectedToWiFi() {
        return connectedToWiFi.getValue() != null ? connectedToWiFi.getValue() : false;
    }

    /**
     * Provides download allowance depending on WiFi connectivity state setting holder that can be observed.
     *
     * @return download allowance depending on WiFi connectivity state holder.
     */
    public LiveData<Boolean> downloadOnlyOnWiFi() {
        return downloadOnlyOnWiFi;
    }

    /**
     * Returns current value of download allowance depending on WiFi connectivity state setting.
     *
     * @return <code>true</code> if download is allowed when WiFi is connected; <code>false</code> otherwise.
     */
    public boolean isDownloadOnlyOnWiFi() {
        return downloadOnlyOnWiFi.getValue() != null ? downloadOnlyOnWiFi.getValue() : true;
    }

    /**
     * Allows to change value of download allowance depending on WiFi connectivity state setting.
     *
     * @param shouldDownloadOnlyOnWiFi <code>true</code> if download is allowed when WiFi is connected;
     *                                 <code>false</code> otherwise.
     */
    public void setDownloadOnlyOnWiFi(boolean shouldDownloadOnlyOnWiFi) {
        downloadOnlyOnWiFi.postValue(shouldDownloadOnlyOnWiFi);
    }

    /**
     * Loads value of download allowance depending on WiFi connectivity state setting from
     * <code>SharedPreferences</code>.
     *
     * @param context - The current context.
     */
    private void loadDownloadOnlyOnWiFiValue(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);

        downloadOnlyOnWiFi.postValue(sharedPreferences.getBoolean(SETTING_DOWNLOAD_ONLY_ON_WIFI, true));

        downloadOnlyOnWiFi.observe((LifecycleOwner) context, downloadOnlyOnWiFi -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(SETTING_DOWNLOAD_ONLY_ON_WIFI, downloadOnlyOnWiFi);
            editor.apply();
        });
    }

    /**
     * Registers WiFi connectivity state monitor using <code>NetworkCallback</code>.
     * Solution for API Level >= 21.
     *
     * @param context - The current context.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void registerModernWifiMonitor(Context context) {
        NetworkRequest wifiRequest = (new NetworkRequest.Builder())
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        NetworkCallback wifiSateCallback = new NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                connectedToWiFi.postValue(true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                connectedToWiFi.postValue(false);
            }
        };

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(wifiRequest, wifiSateCallback);
        }
    }

    /**
     * Registers WiFi connectivity state monitor using <code>BroadcastReceiver</code>.
     * Solution for API Level < 21.
     *
     * @param context - The current context.
     */
    private void registerLegacyWifiMonitor(Context context) {
        BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                boolean wifiConnected = wifiManager != null
                        && wifiManager.isWifiEnabled()                           // Wi-Fi adapter is ON
                        && wifiManager.getConnectionInfo().getNetworkId() != -1; // Connected to an access point

                if (isConnectedToWiFi() != wifiConnected) {
                    connectedToWiFi.setValue(wifiConnected);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        context.registerReceiver(wifiStateReceiver, intentFilter);
    }
}
