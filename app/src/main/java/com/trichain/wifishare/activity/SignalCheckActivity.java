package com.trichain.wifishare.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

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
    private static final float MAX_STRENGTH = 5.0f;
    private Timer timer;
    private int connectionStrength = 0;
    private float percent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_signal_check);

        Log.e(TAG, "onCreate: " + CheckConnectivity.getSingleWiFiSignalStrength(this));

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
        }, 10000);
    }
}