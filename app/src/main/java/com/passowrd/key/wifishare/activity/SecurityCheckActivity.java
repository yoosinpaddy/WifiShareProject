package com.passowrd.key.wifishare.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.passowrd.key.wifishare.R;
import com.passowrd.key.wifishare.databinding.ActivitySecurityCheckBinding;
import com.passowrd.key.wifishare.util.CheckConnectivity;
import com.passowrd.key.wifishare.util.util;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SecurityCheckActivity extends AppCompatActivity {

    private ActivitySecurityCheckBinding b;
    private Timer timer;
    int progress = 0;
    View[] items;
    View[] scanRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_security_check);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initToolBar(0);

        timer = new Timer();

        items = new View[]{b.scan1, b.scan2, b.scan3, b.scan4, b.scan5, b.scan6, b.scan7, b.scan8};
        scanRes = new View[]{b.scanRes1, b.scanRes2, b.scanRes3, b.scanRes4, b.scanRes5, b.scanRes6};

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (progress < 100) {
                    progress += 1;
                    runOnUiThread(() -> {
                        b.tvSecurityProgress.setText(progress + "%");
                        if (progress > 10) {
                            toggleItemVisibility(items[new Random().nextInt(8)], false);
                        } else if (progress > 38) {
                            toggleItemVisibility(items[new Random().nextInt(8)], false);
                        } else if (progress > 56) {
                            toggleItemVisibility(items[new Random().nextInt(8)], false);
                        } else {
                            toggleItemVisibility(items[new Random().nextInt(8)], false);
                        }
                    });
                } else {
                    timer.cancel();
                    runOnUiThread(() -> {
                        toggleItemVisibility(items[new Random().nextInt(8)], true);
                        b.progressSecurity.stopAnimation();

                        displayResults();

                    });

                }
            }
        }, 0, 100);

    }


    private void initToolBar(int which) {
        getSupportActionBar().setTitle(which == 0 ? "WiFi Security Scan" : "Scan Results");
        getSupportActionBar().setSubtitle(which == 0 ? CheckConnectivity.getWiFiName(this) : "WiFi is secure");
    }


    int currentView = -1; // To show results systematically

    private void displayResults() {
        toggleLayoutVisibility(1);

        int viewCount = scanRes.length - 1;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentView < viewCount) {
                    runOnUiThread(() -> {
                        util.showView(scanRes[currentView], true);
                    });
                    currentView++;
                } else {
                    timer.cancel();
                }

            }
        }, 1400, 700);
    }


    private void toggleLayoutVisibility(int status) {
        new Handler().postDelayed(() -> {
            if (status == 0) { //0 == show scanner screen
                util.hideView(b.rlSecurityResults, false);
                util.showView(b.rlSecurityScanner, false);
            } else {//1 == show results screen
                util.showView(b.rlSecurityResults, false);
                util.hideView(b.rlSecurityScanner, false);
            }
        }, 1250);

        initToolBar(1);
    }

    private void toggleItemVisibility(View item, boolean isComplete) {
        if (!isComplete) {
            if (item.getVisibility() == View.VISIBLE) {
                util.hideViewInvisible(item, true);
            } else {
                util.showView(item, true);
            }
        } else {
            for (View v : items) {
                util.hideView(v, true);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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