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
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
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
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(WIFI_ROOT);
    WifiManager wifiManager = (WifiManager) WifiBaseActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    /*location*/
    private static final int PERMISSION_REQUEST_CODE = 987;
    private FusedLocationProviderClient locationProviderClient;
    private WifiManager mWifiManager;
    ScanResultsInterface scanResultsInterfaceListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationProviderClient = getFusedLocationProviderClient(this);
        requestLocationPermissions();
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public boolean isWifiOn() {
        if (wifiManager.isWifiEnabled()) {
            return true;
        }
        return false;
    }

    public void getWifiListOffline(ScanResultsInterface scanResultsInterfaceListener) {
        if (!isWifiOn()) {
            Toast.makeText(this, "Turn wifi on and retry", Toast.LENGTH_SHORT).show();
            return;
        }
        this.scanResultsInterfaceListener = scanResultsInterfaceListener;
        IntentFilter i= new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        i.addAction(WifiManager.ACTION_PICK_WIFI_NETWORK);
        registerReceiver(mWifiScanReceiver,
                i);

        mWifiManager.startScan();

    }

    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                Log.e(TAG, "BroadcastReceiver onReceive: SCAN_RESULTS_AVAILABLE_ACTION" );
                List<ScanResult> mScanResults = mWifiManager.getScanResults();
                for (ScanResult a : mScanResults
                ) {
                    Log.e(TAG, "onReceive: " + a.SSID);
                }
                if (scanResultsInterfaceListener != null) {
                    scanResultsInterfaceListener.onScanResultsAvailable(mScanResults);
                }
            }else if (intent.getAction().equals(WifiManager.ACTION_PICK_WIFI_NETWORK)) {
                Log.e(TAG, "BroadcastReceiver onReceive: ACTION_PICK_WIFI_NETWORK" );
                if (ActivityCompat.checkSelfPermission(WifiBaseActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestLocationPermissions();
                    return;
                }
                List<WifiConfiguration> mScanResults = mWifiManager.getConfiguredNetworks();
                for (WifiConfiguration a:mScanResults
                ) {
                    Log.e(TAG, "onReceive: "+a.SSID );
                }
                if (scanResultsInterfaceListener!=null){
                    scanResultsInterfaceListener.onSavedWifiResults(mScanResults);
                }
            }
        }
    };

    public void addWifiToFirebase(String SSID, String password) {
        String id = UUID.randomUUID().toString();
        ref.child(id).setValue(new WifiModel(SSID, id, password, "WPA", 1.222, 1.4544))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e(TAG, "onComplete: " + task.toString());
                    }
                });
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
    public void requestLocationPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED||ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED||ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.CHANGE_WIFI_STATE},
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


                        Log.e(TAG, "getLastKnownLocation: current location: " + location.getLatitude() + ", " + location.getLongitude());
                    } else {
                        Log.e(TAG, "getLastKnownLocation: Location is null");
                    }

                });
    }
}
