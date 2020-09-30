package com.trichain.wifishare.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.trichain.wifishare.R;
import com.trichain.wifishare.databinding.ActivitySignalCheckBinding;
import com.trichain.wifishare.util.CheckConnectivity;

import java.util.Timer;
import java.util.TimerTask;

public class SignalCheckActivity extends AppCompatActivity {

    private ActivitySignalCheckBinding b;
    private static final String TAG = "SignalCheckActivity";
    private static final float MAX_STRENGTH = 4.0f;
    private Timer timer;
    private int connectionStrength = 0;
    private float percent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_signal_check);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Signal Strength");

        Log.e(TAG, "onCreate: " + CheckConnectivity.getSingleWiFiSignalStrength(this));

        b.tvWiFiNameSignal.setText(CheckConnectivity.getWiFiName(this));

        startConnectionLoop();

    }

    private void startConnectionLoop() {
        Log.e(TAG, "startConnectionLoop: Attempting to start signal check");
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                connectionStrength = CheckConnectivity.getSingleWiFiSignalStrength(SignalCheckActivity.this);
                percent = (connectionStrength / MAX_STRENGTH) * 100.0f;
                Log.e(TAG, "run: Signal " + connectionStrength);
                Log.e(TAG, "run: MAX_STRENGTH " + MAX_STRENGTH);
                Log.e(TAG, "run: percent " + percent);
                runOnUiThread(() -> {
                    b.awesomeSpeedometer.realSpeedTo(percent);
                });
            }
        }, 1000, 500);

        new Handler().postDelayed(() -> {
            timer.cancel();
            runOnUiThread(() -> {
                Toast.makeText(this, "Scan Finished", Toast.LENGTH_LONG).show();
            });
        }, 60000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_speed_test, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        if (item.getItemId() == R.id.action_refresh) {
            startConnectionLoop();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (timer != null) {
            timer.cancel();
        }
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        if (timer != null) {
            timer.cancel();
        }
        super.onPause();
    }
}