package com.theoplayer.demo.simpleott.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.lifecycle.MutableLiveData

class WifiMonitorLegacy : BroadcastReceiver(), SimpleOTTWifiMonitor {
    private var isConnected: Boolean? = null
    override val isConnectedLiveData = MutableLiveData<Boolean?>()
    override fun onReceive(context: Context, intent: Intent) {
        val wifiMgr =
            (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
        var result = false
        if (wifiMgr.isWifiEnabled) { // Wi-Fi adapter is ON
            val wifiInfo = wifiMgr.connectionInfo
            result = wifiInfo.networkId != -1 // Not connected to an access point
        }
        if (result !== isConnected) {
            isConnected = result
            isConnectedLiveData.setValue(isConnected)
        }
    }
}