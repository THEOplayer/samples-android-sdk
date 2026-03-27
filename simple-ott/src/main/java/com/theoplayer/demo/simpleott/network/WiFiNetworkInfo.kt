package com.theoplayer.demo.simpleott.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.core.content.edit

class WiFiNetworkInfo(context: Context) {
    private val connectedToWiFi = MutableLiveData<Boolean>()
    private val downloadOnlyOnWiFi = MutableLiveData<Boolean>()

    init {
        registerWifiMonitor(context)
        loadDownloadOnlyOnWiFiValue(context)
    }

    fun connectedToWiFi(): LiveData<Boolean> = connectedToWiFi

    val isConnectedToWiFi: Boolean
        get() = connectedToWiFi.value ?: false

    fun downloadOnlyOnWiFi(): LiveData<Boolean> = downloadOnlyOnWiFi

    val isDownloadOnlyOnWiFi: Boolean
        get() = downloadOnlyOnWiFi.value ?: true

    fun setDownloadOnlyOnWiFi(shouldDownloadOnlyOnWiFi: Boolean) {
        downloadOnlyOnWiFi.postValue(shouldDownloadOnlyOnWiFi)
    }

    private fun loadDownloadOnlyOnWiFiValue(context: Context) {
        val sharedPreferences = context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)
        downloadOnlyOnWiFi.postValue(
            sharedPreferences.getBoolean(SETTING_DOWNLOAD_ONLY_ON_WIFI, true)
        )
        downloadOnlyOnWiFi.observe((context as LifecycleOwner)) { value: Boolean? ->
            sharedPreferences.edit {
                putBoolean(SETTING_DOWNLOAD_ONLY_ON_WIFI, value!!)
            }
        }
    }

    private fun registerWifiMonitor(context: Context) {
        val wifiRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        val wifiStateCallback: NetworkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                connectedToWiFi.postValue(true)
            }

            override fun onLost(network: Network) {
                connectedToWiFi.postValue(false)
            }
        }
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(wifiRequest, wifiStateCallback)
    }

    companion object {
        private const val SETTINGS_FILE = "wifiNetworkInfoSettings"
        private const val SETTING_DOWNLOAD_ONLY_ON_WIFI = "downloadOnlyOnWiFi"
    }
}
