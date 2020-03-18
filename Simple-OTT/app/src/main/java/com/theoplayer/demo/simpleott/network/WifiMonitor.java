package com.theoplayer.demo.simpleott.network;

import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class WifiMonitor extends ConnectivityManager.NetworkCallback implements SimpleOTTWifiMonitor {

    private MutableLiveData<Boolean> isConnectedLiveData = new MutableLiveData<>();

    @Override
    public void onAvailable(@NonNull Network network) {
        isConnectedLiveData.postValue(true);
    }

    @Override
    public void onLost(@NonNull Network network) {
        isConnectedLiveData.postValue(false);
    }

    public MutableLiveData<Boolean> getIsConnectedLiveData() {
        return isConnectedLiveData;
    }
}

