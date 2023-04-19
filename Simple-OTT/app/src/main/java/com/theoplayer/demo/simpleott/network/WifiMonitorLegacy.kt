package com.theoplayer.demo.simpleott.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.lifecycle.MutableLiveData;

public class WifiMonitorLegacy extends BroadcastReceiver implements SimpleOTTWifiMonitor {

    private Boolean isConnected = null;
    private MutableLiveData<Boolean> isConnectedLiveData = new MutableLiveData<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wifiMgr != null;
        Boolean result = false;
        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            result = wifiInfo.getNetworkId() != -1; // Not connected to an access point
        }
        if (result != isConnected) {
            isConnected = result;
            isConnectedLiveData.setValue(isConnected);
        }
    }

    public MutableLiveData<Boolean> getIsConnectedLiveData() {
        return isConnectedLiveData;
    }
}



