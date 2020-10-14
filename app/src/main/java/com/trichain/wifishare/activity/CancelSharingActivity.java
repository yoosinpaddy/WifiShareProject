package com.trichain.wifishare.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import com.trichain.wifishare.R;
import com.trichain.wifishare.databinding.ActivityCancelSharingBinding;

public class CancelSharingActivity extends AppCompatActivity {
    ActivityCancelSharingBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_cancel_sharing);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        new Handler().postDelayed(runnable, 15000);
    }
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            runOnUiThread(() -> {
                b.retry.setText("Retry");
                b.retry.setOnClickListener(v -> {
                    b.retry.setText("Cancelling...");
                    new Handler().postDelayed(runnable, 15000);
                });
            });
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}