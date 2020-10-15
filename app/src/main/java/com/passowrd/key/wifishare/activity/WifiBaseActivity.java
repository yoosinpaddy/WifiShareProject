package com.passowrd.key.wifishare.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

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
import com.passowrd.key.wifishare.R;
import com.passowrd.key.wifishare.listeners.ScanResultsInterface;
import com.passowrd.key.wifishare.model.WifiModel;
import com.passowrd.key.wifishare.util.CheckConnectivity;
import com.passowrd.key.wifishare.util.SharedPrefsManager;

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
    private WiFiConnectionListener wiFiConnectionListener;
    NotificationManager notificationManager;

    public void setWiFiConnectionListener(WiFiConnectionListener wiFiConnectionListener) {
        this.wiFiConnectionListener = wiFiConnectionListener;
    }

    public interface WiFiConnectionListener {
        void onWiFiStatusChanged(Boolean isWiFiOn);
    }
    boolean isOnline;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentLocation = new LatLng(0, 0);
        locationProviderClient = getFusedLocationProviderClient(this);
        requestLocationPermissions();
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager = mWifiManager;
        ref = FirebaseDatabase.getInstance().getReference().child(WIFI_ROOT);

        createNotification(SharedPrefsManager.getInstance(WifiBaseActivity.this).shouldShowIcon());
        isOnline=CheckConnectivity.isOnline(WifiBaseActivity.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isOnline!=CheckConnectivity.isOnline(WifiBaseActivity.this)){
                    createNotification(CheckConnectivity.isOnline(WifiBaseActivity.this));
                    isOnline=CheckConnectivity.isOnline(WifiBaseActivity.this);
                }
                new Handler().postDelayed(this,1000);
            }
        },1000);
    }

    public boolean isWifiOn() {
        if (wifiManager.isWifiEnabled()) {
            return true;
        }
        return false;
    }

    public void getWifiListOffline(ScanResultsInterface scanResultsInterfaceListener) {
        runOnUiThread(() -> {
            wiFiConnectionListener.onWiFiStatusChanged(isWifiOn());
        });
        if (!isWifiOn()) {
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

    public static String getWiFiName(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    private void createNotification() {

        String wifi_name = CheckConnectivity.getWiFiName(this);
        String strengthStr = "";
        int strength = 100 + CheckConnectivity.getSingleWiFiSignalStrength(this);

        if (isBetween(strength, 0, 25)) {
            strengthStr = "Poor";
        } else if (isBetween(strength, 26, 50)) {
            strengthStr = "Weak";
        } else if (isBetween(strength, 51, 75)) {
            strengthStr = "Good";
        } else {
            strengthStr = "Excellent";
        }

        Log.e(TAG, "createNotification: STRENGTH INT: " + strength);
        Log.e(TAG, "createNotification: STRENGTH: " + strengthStr);

        RemoteViews collapseView = new RemoteViews(getPackageName(), R.layout.notification_layout);
        collapseView.setImageViewResource(R.id.notifiction_icon, R.mipmap.ic_launcher_round);
        collapseView.setTextViewText(R.id.notification_title, "Connected To: " + wifi_name);
        collapseView.setTextViewText(R.id.notification_message, "Network Latency: " + strengthStr);

        Intent intent2 = new Intent(this, SecurityCheckActivity.class);
        PendingIntent pendingIntent2 = PendingIntent.getActivity(this, 0, intent2, 0);
        collapseView.setOnClickPendingIntent(R.id.notification_security, pendingIntent2);


        int notificationId = 0;
        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "WIFI_NOTIFICATION")
                .setContent(collapseView)
                .setContentIntent(pendingIntent)
                .setCustomContentView(collapseView)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setAutoCancel(true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "WIFI_NOTIFICATION";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Package_Ready",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

        notificationManager.notify(notificationId, builder.build());

    }

    public void createNotification(boolean show) {

        String wifi_name = CheckConnectivity.getWiFiName(this);
        String strengthStr = "";
        int strength = 100 + CheckConnectivity.getSingleWiFiSignalStrength(this);

        if (isBetween(strength, 0, 25)) {
            strengthStr = "Poor";
        } else if (isBetween(strength, 26, 50)) {
            strengthStr = "Weak";
        } else if (isBetween(strength, 51, 75)) {
            strengthStr = "Good";
        } else {
            strengthStr = "Excellent";
        }

        Log.e(TAG, "createNotification: STRENGTH INT: " + strength);
        Log.e(TAG, "createNotification: STRENGTH: " + strengthStr);

        RemoteViews collapseView = new RemoteViews(getPackageName(), R.layout.notification_layout);
        collapseView.setImageViewResource(R.id.notifiction_icon, R.mipmap.ic_launcher_round);
        collapseView.setTextViewText(R.id.notification_title, "Connected To: " + wifi_name);
        collapseView.setTextViewText(R.id.notification_message, "Network Latency: " + strengthStr);

        Intent intent2 = new Intent(this, SecurityCheckActivity.class);
        PendingIntent pendingIntent2 = PendingIntent.getActivity(this, 0, intent2, 0);
        collapseView.setOnClickPendingIntent(R.id.notification_security, pendingIntent2);
        if (!CheckConnectivity.isOnline(WifiBaseActivity.this)){
            collapseView.setTextViewText(R.id.notification_title, "Not connected");
            collapseView.setTextViewText(R.id.notification_message, "" );
            collapseView.setTextViewText(R.id.notification_security, "Connect" );
            Intent intent3 = new Intent(this, HomeActivity.class);
            PendingIntent pendingIntent3= PendingIntent.getActivity(this, 0, intent3, 0);
            collapseView.setOnClickPendingIntent(R.id.notification_security, pendingIntent3);
        }


        int notificationId = 0;
        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "WIFI_NOTIFICATION")
                .setContent(collapseView)
                .setContentIntent(pendingIntent)
                .setCustomContentView(collapseView)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setSound(null)
                .setAutoCancel(true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "WIFI_NOTIFICATION";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Package_Ready",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        notificationManager.cancelAll();
        if (show) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    public static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationManager != null) {
            //notificationManager.cancelAll();
        }

    }
}

