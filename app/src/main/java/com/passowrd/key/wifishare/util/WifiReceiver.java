package com.passowrd.key.wifishare.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

public class WifiReceiver extends BroadcastReceiver {
    WifiManager wifiManager;
    StringBuilder sb;
    private static final String TAG = "WifiReceiver";

    public WifiReceiver(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            List<ScanResult> wifiList = wifiManager.getScanResults();
            sb = new StringBuilder();
            for (ScanResult scanResult : wifiList) {
                sb.append("\n").append(scanResult.SSID).append(" - ").append(scanResult.capabilities + " Signal: " + (scanResult.level + 100)).append("\n");
            }

            Log.e(TAG, "onReceive: " + sb.toString());
        }
    }
}