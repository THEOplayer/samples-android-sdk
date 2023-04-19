package com.theoplayer.demo.simpleott.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * This class is responsible for keeping WiFi connection state info.
 *
 *
 * It registers WiFi monitor to keep application informed about WiFi connectivity changes.
 *
 *
 * It keep setting about download allowance depending on WiFi connectivity state. This value is
 * also persisted in `SharedPreferences`, so it can be restored after application restart.
 *
 *
 * Appropriate `LiveData` objects are provided to observe WiFi network info changes.
 */
class WiFiNetworkInfo(context: Context) {
    /**
     * Keeps information about WiFi connectivity state.
     */
    private val connectedToWiFi = MutableLiveData<Boolean>()

    /**
     * Keeps information about download allowance depending on WiFi connectivity state.
     */
    private val downloadOnlyOnWiFi = MutableLiveData<Boolean>()

    init {
        // Choosing a Wifi monitor depending on the Android API version.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            registerModernWifiMonitor(context)
        } else {
            registerLegacyWifiMonitor(context)
        }
        loadDownloadOnlyOnWiFiValue(context)
    }

    /**
     * Provides WiFi connectivity state holder that can be observed.
     *
     * @return WiFi connectivity state holder.
     */
    fun connectedToWiFi(): LiveData<Boolean> {
        return connectedToWiFi
    }

    /**
     * Returns current WiFi connectivity state.
     *
     * @return `true` if WiFi is connected; `false` otherwise.
     */
    fun isConnectedToWiFi(): Boolean {
        return if (connectedToWiFi.value != null) connectedToWiFi.value!! else false
    }

    /**
     * Provides download allowance depending on WiFi connectivity state setting holder that can be observed.
     *
     * @return download allowance depending on WiFi connectivity state holder.
     */
    fun downloadOnlyOnWiFi(): LiveData<Boolean> {
        return downloadOnlyOnWiFi
    }

    /**
     * Returns current value of download allowance depending on WiFi connectivity state setting.
     *
     * @return `true` if download is allowed when WiFi is connected; `false` otherwise.
     */
    fun isDownloadOnlyOnWiFi(): Boolean {
        return if (downloadOnlyOnWiFi.value != null) downloadOnlyOnWiFi.value!! else true
    }

    /**
     * Allows to change value of download allowance depending on WiFi connectivity state setting.
     *
     * @param shouldDownloadOnlyOnWiFi `true` if download is allowed when WiFi is connected;
     * `false` otherwise.
     */
    fun setDownloadOnlyOnWiFi(shouldDownloadOnlyOnWiFi: Boolean) {
        downloadOnlyOnWiFi.postValue(shouldDownloadOnlyOnWiFi)
    }

    /**
     * Loads value of download allowance depending on WiFi connectivity state setting from
     * `SharedPreferences`.
     *
     * @param context - The current context.
     */
    private fun loadDownloadOnlyOnWiFiValue(context: Context) {
        val sharedPreferences = context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)
        downloadOnlyOnWiFi.postValue(
            sharedPreferences.getBoolean(
                SETTING_DOWNLOAD_ONLY_ON_WIFI,
                true
            )
        )
        downloadOnlyOnWiFi.observe((context as LifecycleOwner)) { downloadOnlyOnWiFi: Boolean? ->
            val editor = sharedPreferences.edit()
            editor.putBoolean(SETTING_DOWNLOAD_ONLY_ON_WIFI, downloadOnlyOnWiFi!!)
            editor.apply()
        }
    }

    /**
     * Registers WiFi connectivity state monitor using `NetworkCallback`.
     * Solution for API Level >= 21.
     *
     * @param context - The current context.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun registerModernWifiMonitor(context: Context) {
        val wifiRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        val wifiSateCallback: NetworkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                connectedToWiFi.postValue(true)
            }

            override fun onLost(network: Network) {
                connectedToWiFi.postValue(false)
            }
        }
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager?.registerNetworkCallback(wifiRequest, wifiSateCallback)
    }

    /**
     * Registers WiFi connectivity state monitor using `BroadcastReceiver`.
     * Solution for API Level < 21.
     *
     * @param context - The current context.
     */
    private fun registerLegacyWifiMonitor(context: Context) {
        val wifiStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val wifiManager =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiConnected =
                    wifiManager != null && wifiManager.isWifiEnabled && wifiManager.connectionInfo.networkId != -1 // Connected to an access point
                if (isConnectedToWiFi() != wifiConnected) {
                    connectedToWiFi.setValue(wifiConnected)
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        context.registerReceiver(wifiStateReceiver, intentFilter)
    }

    companion object {
        private const val SETTINGS_FILE = "wifiNetworkInfoSettings"
        private const val SETTING_DOWNLOAD_ONLY_ON_WIFI = "downloadOnlyOnWiFi"
    }
}