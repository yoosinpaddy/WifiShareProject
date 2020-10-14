package com.trichain.wifishare.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.trichain.wifishare.R;

public class FeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
    }

    public void howToUse(View v) {
        Intent intent = new Intent(new Intent(this, InstructionsActivity.class));
        intent.putExtra("num", 1);
        startActivity(intent);
    }

    public void failedToConnect(View v) {
        Intent intent = new Intent(new Intent(this, InstructionsActivity.class));
        intent.putExtra("num", 2);
        startActivity(intent);
    }

    public void hotspotSharing(View v) {
        Intent intent = new Intent(new Intent(this, InstructionsActivity.class));
        intent.putExtra("num", 3);
        startActivity(intent);
    }
}