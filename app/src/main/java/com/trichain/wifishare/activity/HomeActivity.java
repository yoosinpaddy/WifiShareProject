package com.trichain.wifishare.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.trichain.wifishare.R;
import com.trichain.wifishare.util.WifiReceiver;

public class HomeActivity extends WifiBaseActivity {

    private WifiReceiver receiverWifi;
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_connect, R.id.navigation_discover, R.id.navigation_more)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_connect, menu);

        SwitchCompat s= (SwitchCompat) menu.findItem(R.id.action_main_switch).getActionView().findViewById(R.id.switchForActionBar);
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    if (!isWifiOn()){
                        turnOnWIFI();
                    }
                }else {
                    if (isWifiOn()){
                        turnOffWIFI();
                    }
                }
            }
        });
        keepUpdatingWifi(s);
        return super.onCreateOptionsMenu(menu);
    }

    private void keepUpdatingWifi(SwitchCompat s) {
        if (s!=null){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isWifiOn()){
                        if (s!=null){
                            s.setChecked(true);
                        }
                    }else {
                        if (s!=null){
                            s.setChecked(false);
                        }

                    }
                    new Handler().postDelayed(this,1000);
                }
            },1000);
        }else {
            Log.e(TAG, "keepUpdatingWifi: switch is null" );
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.action_main_switch){

        }
        if (item.getItemId()==R.id.action_map){
            startActivity(new Intent(this, MapsActivity.class));
        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        receiverWifi = new WifiReceiver(wifiManager);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
        getWifi();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    private void getWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Toast.makeText(HomeActivity.this, "version> = marshmallow", Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(HomeActivity.this, "scanning", Toast.LENGTH_SHORT).show();
        }
        wifiManager.startScan();
    }
}