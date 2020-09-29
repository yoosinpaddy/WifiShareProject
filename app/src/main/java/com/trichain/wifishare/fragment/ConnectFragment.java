package com.trichain.wifishare.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.trichain.wifishare.R;
import com.trichain.wifishare.activity.CountrySelectActivity;
import com.trichain.wifishare.databinding.FragmentConnectBinding;


public class ConnectFragment extends Fragment {

    private FragmentConnectBinding b;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = DataBindingUtil.inflate(inflater, R.layout.fragment_connect, container, false);

        b.btnGetMoreWiFi.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CountrySelectActivity.class));
        });

        return b.getRoot();
    }
}