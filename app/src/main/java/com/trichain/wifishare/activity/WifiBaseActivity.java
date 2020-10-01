package com.trichain.wifishare.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trichain.wifishare.listeners.ScanResultsInterface;
import com.trichain.wifishare.model.WifiModel;

import java.util.List;
import java.util.UUID;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class WifiBaseActivity extends AppCompatActivity {
    private static final String TAG = "WifiBaseActivity";
    private static final String WIFI_ROOT = "wifi_locations";
    private static final String WIFI_REPORT_ROOT = "wifi_report_locations";
    DatabaseReference ref;
    WifiManager wifiManager;
    LatLng currentLocation;
    /*location*/
    private static final int PERMISSION_REQUEST_CODE = 987;
    private FusedLocationProviderClient locationProviderClient;
    private WifiManager mWifiManager;
    ScanResultsInterface scanResultsInterfaceListener;
    ScanResultsInterface savedResultsInterfaceListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentLocation = new LatLng(0, 0);
        locationProviderClient = getFusedLocationProviderClient(this);
        requestLocationPermissions();
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager = mWifiManager;
        ref = FirebaseDatabase.getInstance().getReference().child(WIFI_ROOT);
    }

    public boolean isWifiOn() {
        if (wifiManager.isWifiEnabled()) {
            return true;
        }
        return false;
    }

    public void getWifiListOffline(ScanResultsInterface scanResultsInterfaceListener) {
        if (!isWifiOn()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(WifiBaseActivity.this, "Turn wifi on and retry", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        @SuppressLint("MissingPermission") List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
        this.scanResultsInterfaceListener = scanResultsInterfaceListener;
        this.savedResultsInterfaceListener = scanResultsInterfaceListener;
        savedResultsInterfaceListener.onSavedWifiResults(wifiConfigurations);
        IntentFilter i = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//        i.addAction(WifiManager.ACTION_PICK_WIFI_NETWORK);
        registerReceiver(mWifiScanReceiver, i);

        mWifiManager.startScan();

    }

    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                Log.e(TAG, "BroadcastReceiver onReceive: SCAN_RESULTS_AVAILABLE_ACTION");
                List<ScanResult> mScanResults = mWifiManager.getScanResults();
                for (ScanResult a : mScanResults
                ) {
                    Log.e(TAG, "onReceive: " + a.SSID);
                }
                if (scanResultsInterfaceListener != null) {
                    scanResultsInterfaceListener.onScanResultsAvailable(mScanResults);
                } else {
                    Log.e(TAG, "onReceive: interface is null");
                }
            } else if (intent.getAction().equals(WifiManager.ACTION_PICK_WIFI_NETWORK)) {
                Log.e(TAG, "BroadcastReceiver onReceive: ACTION_PICK_WIFI_NETWORK");
                if (ActivityCompat.checkSelfPermission(WifiBaseActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "onReceive: permision errors");
                    requestLocationPermissions();
                    return;
                }
                List<WifiConfiguration> mScanResults = mWifiManager.getConfiguredNetworks();
                for (WifiConfiguration a : mScanResults
                ) {
                    Log.e(TAG, "onReceive: " + a.SSID);
                }
                if (savedResultsInterfaceListener != null) {
                    savedResultsInterfaceListener.onSavedWifiResults(mScanResults);
                }
            }
        }
    };

    public void addWifiToFirebase(String SSID, String password) {
        getLastKnownLocation();
        String id = UUID.randomUUID().toString();
        int a = 0;
        while (currentLocation.latitude == 0) {
            if (a < 10000) {
                getLastKnownLocation();
            }
            a++;
        }
        ref.child(id).setValue(new WifiModel(SSID, id, password, "WPA", currentLocation.latitude, currentLocation.longitude))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e(TAG, "onComplete: " + task.toString());
                    }
                });
    }

    public void addWifiToFirebase(String SSID, String password, OnCompleteListener listener, OnFailureListener onFailureListener) {
        getLastKnownLocation();
        String id = UUID.randomUUID().toString();
        int a = 0;
        while (currentLocation.latitude == 0) {
            if (a < 10000) {
                getLastKnownLocation();
            }
            a++;
        }
        ref.child(id).setValue(new WifiModel(SSID, id, password, "WPA", currentLocation.latitude, currentLocation.longitude))
                .addOnCompleteListener(listener)
                .addOnFailureListener(onFailureListener);
    }

    public void getWifiListFromFirebase(ValueEventListener v) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(WIFI_ROOT);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                v.onDataChange(dataSnapshot);
                Log.e(TAG, "onDataChange: " + dataSnapshot.toString());
                for (DataSnapshot a : dataSnapshot.getChildren()
                ) {
                    WifiModel wifiModel = a.getValue(WifiModel.class);

                    Log.e(TAG, "onDataChange: " + wifiModel.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void turnOnWIFI() {
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
    }

    public void turnOffWIFI() {
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
    }


    public void connectToWifi(WifiModel w) {
        String ssid = w.getSsid();
        String key = w.getPassword();
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            requestLocationPermissions();
            return;
        }
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                Log.e(TAG, "connectToWifi: " + ssid);

                break;
            } else {
                Log.e(TAG, "connectToWifi: non Target: " + ssid);
            }
        }
    }

    public void connectToNewWifi(WifiModel w) {
        String ssid = w.getSsid();
        String key = w.getPassword();
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + ssid + "\"";   // Please note the quotes. String should contain SSID in quotes

        conf.preSharedKey = "\"" + key + "\"";

        conf.status = WifiConfiguration.Status.ENABLED;
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

        Log.d("connecting", conf.SSID + " " + conf.preSharedKey);

        wifiManager.addNetwork(conf);

        Log.d("after connecting", conf.SSID + " " + conf.preSharedKey);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            requestLocationPermissions();
            return;
        }
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                Log.e(TAG, "connectToWifi: " + ssid);

                break;
            } else {
                Log.e(TAG, "connectToWifi: non Target: " + ssid);
            }
        }
    }

    public void requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE},
                    PERMISSION_REQUEST_CODE);
        } else {
            getLastKnownLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        Log.e(TAG, "getLastKnownLocation: called");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();
            return;
        }
        locationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        Log.e(TAG, "getLastKnownLocation: current location: " + location.getLatitude() + ", " + location.getLongitude());
                    } else {
                        Log.e(TAG, "getLastKnownLocation: Location is null");
                    }

                });
    }

    public void unregisterReceivers() {
        if (mWifiScanReceiver != null) {
            try {
                unregisterReceiver(mWifiScanReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
