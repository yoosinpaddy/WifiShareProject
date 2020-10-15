package com.passowrd.key.wifishare.tests;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.passowrd.key.wifishare.R;
import com.passowrd.key.wifishare.model.WifiModel;

import java.util.List;
import java.util.UUID;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MyTests extends AppCompatActivity {
    private static final String TAG = "MyTests";
    private static final String WIFI_ROOT = "wifi_locations";

    /*location*/
    private static final int PERMISSION_REQUEST_CODE = 987;
    private FusedLocationProviderClient locationProviderClient;
    private WifiManager mWifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tests);
        locationProviderClient = getFusedLocationProviderClient(this);
        requestLocationPermissions();
    }

    public void getWifiList(View v) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(WIFI_ROOT);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

    public void addWifi(View v) {
        String id = UUID.randomUUID().toString();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(WIFI_ROOT);
        ref.child(id).setValue(new WifiModel("Linus", id, "password1", "WPA", 1.222, 1.4544))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e(TAG, "onComplete: " + task.toString());
                    }
                });
    }

    public void addWifi2(View v) {

    }

    public void connectToWifi(View v) {
        String ssid = "LYNAS 2";
        String key = "mash@2020";
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            requestLocationPermissions();
            return;
        }
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                Log.e(TAG, "connectToWifi: "+ssid );

                break;
            }else {
                Log.e(TAG, "connectToWifi: non Target: "+ssid );
            }
        }

       /* try {
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



            @SuppressLint("MissingPermission") List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for( WifiConfiguration i : list ) {
                if(i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                    Log.d("re connecting", i.SSID + " " + conf.preSharedKey);

                    break;
                }
            }


            //WiFi Connection success, return true
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
        }*/
//        WifiConfiguration conf = new WifiConfiguration();
//        conf.SSID = "\"" + ssid + "\"";
//        conf.preSharedKey = "\""+ key +"\"";
//        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        wifiManager.addNetwork(conf);
//        @SuppressLint("MissingPermission") List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
//        for( WifiConfiguration i : list ) {
//            if(i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
//                wifiManager.disconnect();
//                wifiManager.enableNetwork(i.networkId, true);
//                wifiManager.reconnect();
//
//                break;
//            }
//        }

//        WifiConfiguration wifiConfig = new WifiConfiguration();
//
//        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//        wifiConfig.preSharedKey = String.format("\"%s\"", key);
//        wifiConfig.SSID = String.format("\"%s\"", ssid);
//
//        WifiManager wifiManager =
//                (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        boolean isConfigured = false;
//        @SuppressLint("MissingPermission") List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
//        for (WifiConfiguration i : list) {
//            if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
//                wifiManager.disconnect();
//                wifiManager.enableNetwork(i.networkId, true);
//                wifiManager.reconnect();
//                isConfigured = true;
//                break;
//            }
//        }
//        //adding the network
//        if (!isConfigured) {
//            int netId = wifiManager.addNetwork(wifiConfig);
//            wifiManager.saveConfiguration();
//            wifiManager.disconnect();
//            wifiManager.enableNetwork(netId, true);
//        }

//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
////remember id
////        int netId = wifiManager.addNetwork(wifiConfig);
////        wifiManager.disconnect();
////        wifiManager.enableNetwork(netId, true);
////        wifiManager.reconnect();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            return;
//        }
//        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
//        for( WifiConfiguration i : list ) {
//            if(i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
//                wifiManager.disconnect();
//                wifiManager.enableNetwork(i.networkId, true);
//                wifiManager.reconnect();
//
//                break;
//            }
//        }
    }
    public void getWifiListOffline(View v){
        if (!isWifiOn()){
            Toast.makeText(this, "Turn wifi on and retry", Toast.LENGTH_SHORT).show();
            return;
        }
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        registerReceiver(mWifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mWifiManager.startScan();

    }
    public boolean isWifiOn(){
        WifiManager wifiManager = (WifiManager) MyTests.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            return true;
        }
        return false;
    }
    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = mWifiManager.getScanResults();
                for (ScanResult a:mScanResults
                     ) {
                    Log.e(TAG, "onReceive: "+a.SSID );
                }
                // add your logic here
            }
        }
    };

    private void initFirebase() {
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(WIFI_ROOT);
//        ref.child(id).setValue(user);
    }

    public void requestLocationPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            getLastKnownLocation();
        }
    }
    public void turnOnGPS(){

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