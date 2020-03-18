package com.theoplayer.demo.simpleott.network;

import androidx.lifecycle.MutableLiveData;

public interface SimpleOTTWifiMonitor {
    MutableLiveData<Boolean> getIsConnectedLiveData();
}
