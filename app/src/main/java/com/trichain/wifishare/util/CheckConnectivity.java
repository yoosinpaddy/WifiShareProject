package com.trichain.wifishare.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.URL;
import java.net.URLConnection;

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
    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public boolean isActuallyConnectedToTheInternet(Context context) {
        if (isOnline(context)) {
            try {
                URLConnection google = new URL("https://www.google.com").openConnection();
                google.setRequestProperty("User-Agent", "ConnectionTest");
                google.setRequestProperty("Connection", "close");
                google.setConnectTimeout(1000); // configurable
                google.connect();
            } catch (Exception e) {

            }
        }
        return false;//TODO;;;;;;
    }

    public static int getSingleWiFiSignalStrength(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
    }


    public static int getAllWiFiSignalStrength(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        wifiManager.startScan();
       /* wifiManager.registerScanResultsCallback(new Executor() {
            @Override
            public void execute(Runnable runnable) {

            }
        }, new WifiManager.ScanResultsCallback() {
            @Override
            public void onScanResultsAvailable() {

            }
        });*/
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
    }

    public static String getWiFiName(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }
}