package com.theoplayer.demo.simpleott.network

import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import androidx.lifecycle.MutableLiveData

class WifiMonitor : NetworkCallback(), SimpleOTTWifiMonitor {
    override val isConnectedLiveData = MutableLiveData<Boolean?>()
    override fun onAvailable(network: Network) {
        isConnectedLiveData.postValue(true)
    }

    override fun onLost(network: Network) {
        isConnectedLiveData.postValue(false)
    }
}