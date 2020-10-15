package com.passowrd.key.wifishare.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.passowrd.key.wifishare.R;
import com.passowrd.key.wifishare.databinding.ActivityCancelSharingBinding;
import com.passowrd.key.wifishare.model.WifiModel;
import com.passowrd.key.wifishare.util.CheckConnectivity;

public class CancelSharingActivity extends AppCompatActivity {
    ActivityCancelSharingBinding b;
    private static final String WIFI_ROOT = "wifi_locations";
    boolean canceled=false;
    private static final String TAG = "CancelSharingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_cancel_sharing);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        new Handler().postDelayed(runnable, 15000);
        cancelWifiSharing(() -> {
            canceled=true;
            Toast.makeText(this, "Canceling done", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    CancelSharingActivity.super.onBackPressed();
                }
            },1500);
        });
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(() -> {
                if (!canceled){
                    b.retry.setText("Retry");
                    b.retry.setOnClickListener(v -> {
                        b.retry.setText("Cancelling...");
                        new Handler().postDelayed(runnable, 15000);
                    });
                }
            });
        }
    };

    interface Canceling {
        void onComplete();
    }

    public void cancelWifiSharing(Canceling c) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(WIFI_ROOT);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot a : dataSnapshot.getChildren()) {
                    WifiModel wifiModel = a.getValue(WifiModel.class);
                    if (wifiModel != null) {
                        if (wifiModel.getSsid().replace("\"", "").contentEquals(CheckConnectivity.getWiFiName(CancelSharingActivity.this).replace("\"", ""))) {
                            Log.e(TAG, "onDataChange: deleting " + wifiModel.getSsid());
                            if (a.getKey() != null) {
                                ref.child(a.getKey()).removeValue();
                            }
                        }
                    }
                    Log.e(TAG, "onDataChange: " + wifiModel.toString());
                }
                c.onComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}