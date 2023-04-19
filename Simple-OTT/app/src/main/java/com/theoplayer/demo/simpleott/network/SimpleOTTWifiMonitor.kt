package com.theoplayer.demo.simpleott.network

import androidx.lifecycle.MutableLiveData

interface SimpleOTTWifiMonitor {
    val isConnectedLiveData: MutableLiveData<Boolean?>
}