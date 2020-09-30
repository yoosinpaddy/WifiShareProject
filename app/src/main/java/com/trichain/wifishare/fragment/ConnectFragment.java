package com.trichain.wifishare.fragment;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.trichain.wifishare.R;
import com.trichain.wifishare.activity.CountrySelectActivity;
import com.trichain.wifishare.databinding.FragmentConnectBinding;
import com.trichain.wifishare.util.CheckConnectivity;
import com.trichain.wifishare.util.util;


public class ConnectFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 332;
    private static final String TAG = "ConnectFragment";
    private FragmentConnectBinding b;
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = DataBindingUtil.inflate(inflater, R.layout.fragment_connect, container, false);
        b.tvConnectionMessage.setText(getActivity().getString(R.string.connected_to_wifi, CheckConnectivity.getWiFiName(getActivity())));
        b.tvInternetConnected.setText(CheckConnectivity.isOnline(getContext()) ? "Internet connected" : "No internet connection");

        checkGPSEnabled();
        initRecycler();
        initBottomSheet();

        b.btnGetMoreWiFi.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CountrySelectActivity.class));
        });

        b.fabGPSSettings.setOnClickListener(v -> startGPSIntent());

        return b.getRoot();
    }

    private void initBottomSheet() {
        mBottomSheetDialog = new BottomSheetDialog(getActivity());
        mBehavior = BottomSheetBehavior.from(b.bottomSheet);
    }

    private void initRecycler() {
        b.recyclerFreeHotspots.setLayoutManager(new LinearLayoutManager(getActivity()));
        b.recyclerProtectedHotspots.setLayoutManager(new LinearLayoutManager(getActivity()));


    }

    public void checkGPSEnabled() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        assert manager != null;
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            toggleVisibility(0);
        } else {
            toggleVisibility(1);
        }
    }

    private void startGPSIntent() {
        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkGPSEnabled();
    }

    private void toggleVisibility(int status) {
        if (status == 0) { //0 == no gps permission granted
            util.hideView(b.llNetworks, false);
            util.showView(b.llNoGPS, false);
        } else {//1 == gps permission is granted
            util.showView(b.llNetworks, false);
            util.hideView(b.llNoGPS, false);
        }
    }

}