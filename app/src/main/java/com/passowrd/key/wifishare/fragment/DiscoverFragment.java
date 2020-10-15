package com.passowrd.key.wifishare.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.passowrd.key.wifishare.R;
import com.passowrd.key.wifishare.activity.SecurityCheckActivity;
import com.passowrd.key.wifishare.activity.SignalCheckActivity;
import com.passowrd.key.wifishare.activity.SpeedCheckActivity;
import com.passowrd.key.wifishare.databinding.FragmentDiscoverBinding;


public class DiscoverFragment extends Fragment {

    private FragmentDiscoverBinding b;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = DataBindingUtil.inflate(inflater, R.layout.fragment_discover, container, false);

        b.fabSecurity.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SecurityCheckActivity.class));
        });

        b.fabSignal.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SignalCheckActivity.class));
        });

        b.fabSpeed.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SpeedCheckActivity.class));
        });



        return b.getRoot();
    }
}