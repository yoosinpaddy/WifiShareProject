package com.trichain.wifishare.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;

public class CheckConnectivity {

    /*Activity context*/
    Context context;

    /*Log TAG*/
    private static final String TAG = "CheckConnectivity";

    /*
     * TODO: Required permission android.permission.ACCESS_NETWORK_STATE
     * Check if network is connected: Mobile Data or WiFi*/

    private boolean checkNetworkAvailability() {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        boolean isMobileConn = false;
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConn |= networkInfo.isConnected();
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn |= networkInfo.isConnected();
            }
        }
        Log.d(TAG, "Wifi connected: " + isWifiConn);
        Log.d(TAG, "Mobile connected: " + isMobileConn);

        return isMobileConn || isWifiConn;

    }

    /*Check if can actually connect to the internet*/
    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
