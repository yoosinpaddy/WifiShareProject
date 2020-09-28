package com.trichain.wifishare.tests;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.FirebaseApp;
import com.trichain.wifishare.R;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MyTests extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 987;
    private static final String TAG = "MyTests";

    /*location*/
    private FusedLocationProviderClient locationProviderClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tests);
        locationProviderClient = getFusedLocationProviderClient(this);
        requestLocationPermissions();
    }

    public void getWifiList(View v){


    }
    public void addWifi(View v){


    }
    public void addWifi2(View v){

    }

    private void initFirebase() {
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("userTable");
//// where User is your class and user is it's object with the values you need to update
//        ref.child(user.getNumber()).setValue(user);
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