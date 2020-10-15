package com.passowrd.key.wifishare.activity;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.passowrd.key.wifishare.R;
import com.passowrd.key.wifishare.databinding.ActivitySettingsBinding;
import com.passowrd.key.wifishare.util.SharedPrefsManager;

public class SettingsActivity extends WifiBaseActivity {
ActivitySettingsBinding b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b= DataBindingUtil.setContentView(this,R.layout.activity_settings);
        b.statusBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPrefsManager.getInstance(SettingsActivity.this).shouldShowIcon(isChecked);
            createNotification(isChecked);
        });
    }
}