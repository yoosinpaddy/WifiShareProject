package com.trichain.wifishare.activity;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trichain.wifishare.R;
import com.trichain.wifishare.model.WifiModel;
import com.trichain.wifishare.util.util;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends WifiBaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "MapsActivity";
    private static final String WIFI_ROOT = "wifi_locations";
    private FusedLocationProviderClient locationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationProviderClient = getFusedLocationProviderClient(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            MapsActivity.this, R.raw.map_styles));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style.", e);
        }

        getWifiListFromFirebase(valueEventListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();
            return;
        }
        mMap.setMyLocationEnabled(true);

        getLastKnownLocation();

        mMap.setOnMarkerClickListener(marker -> {
            WifiModel m = (WifiModel) marker.getTag();
            Log.e(TAG, "onMapReady: clicked " + m.getSsid());

            new AlertDialog.Builder(MapsActivity.this)
                    .setTitle("Connect to " + m.getSsid())
                    .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connect(m);
                        }
                    })
                    .setMessage("You are about to connect to WiFi: " + m.getSsid())
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return true;
        });
    }

    private void connect(WifiModel w) {
        final boolean[] hasConnected = {false};
        showRealConnectDialog(w, 0);
        String ssid = w.getSsid();
        String key = w.getPassword();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
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
        final int[] a = {0};
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                a[0]++;
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifiManager.getConnectionInfo();
                String ssid = info.getSSID();
                Log.e(TAG, "run: has connected:" + ssid);
                if (ssid.contentEquals(w.getSsid())) {
                    hasConnected[0] = true;
                } else {
                    hasConnected[0] = false;
                    if (a[0] == 5) {
                        //Toast.makeText(c, "Cant connect to the network", Toast.LENGTH_SHORT).show();
                    } else {
                        new Handler().postDelayed(this, 5000);

                    }
                }
            }
        }, 5000);
    }

    int curr = -1;
    int viewcount = 0;

    private void showRealConnectDialog(WifiModel wifiModel, int position) {
        View root = LayoutInflater.from(MapsActivity.this).inflate(R.layout.dialog_information, null);
        Dialog dialog = new Dialog(MapsActivity.this, R.style.Theme_CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(root);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        TextView networkName = root.findViewById(R.id.networkName);
        TextView tvstatus = root.findViewById(R.id.tvstatus);
        TextView tvTesting1 = root.findViewById(R.id.tvTesting1);
        TextView tvTesting2 = root.findViewById(R.id.tvTesting2);
        TextView tvTesting3 = root.findViewById(R.id.tvTesting3);

        networkName.setText(wifiModel.getSsid());
        tvstatus.setText("securely connecting...");

        dialog.show();

        View[] items = new View[]{tvTesting1, tvTesting2, tvTesting3};
        viewcount = items.length - 1;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (curr < viewcount) {
                    runOnUiThread(() -> {
                        util.showView(items[curr], true);
                    });
                    curr++;
                } else {
                    curr = -1;
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        timer.cancel();
                    });
                }
            }
        }, 1000, 1200);
    }


    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            mMap.clear();
            for (DataSnapshot a : dataSnapshot.getChildren()) {
                WifiModel wifiModel = a.getValue(WifiModel.class);
                if (wifiModel != null) {
                    if (wifiModel.getLat() != 0) {
                        Log.e(TAG, "onDataChange: has location:" + wifiModel.getSsid());
                        LatLng sydney = new LatLng(wifiModel.getLat(), wifiModel.getLongt());
                        Marker marker = mMap.addMarker(new MarkerOptions().position(sydney)
                                .title(wifiModel.getSsid())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_signal_map)));
                        marker.setTag(wifiModel);

                        //mMap.animateCamera(CameraUpdateFactory.newLatLng(sydney));
                    } else {
                        Log.e(TAG, "onDataChange: no location");
                    }
                }
                Log.e(TAG, "onDataChange: " + wifiModel.toString());
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public void getWifiListFromFirebase(ValueEventListener v) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(WIFI_ROOT);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                v.onDataChange(dataSnapshot);
                Log.e(TAG, "onDataChange: " + dataSnapshot.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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

                        //mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                        //mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
                        Log.e(TAG, "getLastKnownLocation: current location: " + location.getLatitude() + ", " + location.getLongitude());
                    } else {
                        Log.e(TAG, "getLastKnownLocation: Location is null");
                    }

                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        if (item.getItemId() == R.id.action_refresh_map) {
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
            getWifiListFromFirebase(valueEventListener);
        }
        return super.onOptionsItemSelected(item);
    }

}