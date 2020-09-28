package com.trichain.wifishare.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.trichain.wifishare.R;
import com.trichain.wifishare.databinding.ActivityMainBinding;
import com.trichain.wifishare.room.RoomActivity;

public class MainActivity extends AppCompatActivity {

    //Data binding class
    private ActivityMainBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        b.btnRoom.setOnClickListener(v -> startActivity(new Intent(this, RoomActivity.class)));

    }
}