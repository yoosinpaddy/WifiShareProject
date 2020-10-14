package com.trichain.wifishare.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.widget.CompoundButton;

import com.trichain.wifishare.R;
import com.trichain.wifishare.databinding.ActivitySettingsBinding;
import com.trichain.wifishare.util.SharedPrefsManager;

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