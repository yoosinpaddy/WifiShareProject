package com.trichain.wifishare.listeners;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

import java.util.List;

public interface ScanResultsInterface {
    void onScanResultsAvailable(List<ScanResult> scanResultInterfaces);
    void onSavedWifiResults(List<WifiConfiguration> savedResults);
}
