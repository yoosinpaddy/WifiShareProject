package com.trichain.wifishare.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;

import com.trichain.wifishare.R;
import com.trichain.wifishare.databinding.ActivityInstructionsBinding;

public class InstructionsActivity extends AppCompatActivity {

    int instrNum = -1;
    private ActivityInstructionsBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_instructions);

        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            instrNum = extras.getInt("num");
        }

        showInstructions();

    }

    private void showInstructions() {
        switch (instrNum) {
            case 1:
                b.ll1.setVisibility(View.VISIBLE);
                break;
            case 2:
                b.ll2.setVisibility(View.VISIBLE);
                break;
            case 3:
                b.ll3.setVisibility(View.VISIBLE);
                break;
        }
    }
}