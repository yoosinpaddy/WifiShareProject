package com.trichain.wifishare.fragment;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.util.Log;
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
import com.trichain.wifishare.activity.HomeActivity;
import com.trichain.wifishare.adapter.WifiAdapter;
import com.trichain.wifishare.databinding.FragmentConnectBinding;
import com.trichain.wifishare.listeners.ScanResultsInterface;
import com.trichain.wifishare.model.WifiModel;
import com.trichain.wifishare.util.CheckConnectivity;
import com.trichain.wifishare.util.util;

import java.util.ArrayList;
import java.util.List;


public class ConnectFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 332;
    private static final String TAG = "ConnectFragment";
    private FragmentConnectBinding b;
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    WifiAdapter freeAdapter,nonFreeAdapter;
    List<WifiModel> wifiModels =new ArrayList<>();
    List<WifiModel> protectedWifiModels =new ArrayList<>();
    Context c;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = DataBindingUtil.inflate(inflater, R.layout.fragment_connect, container, false);
        if (!CheckConnectivity.getWiFiName(getActivity()).contains("<unknown ssid>")){
            b.tvConnectionMessage.setText(getActivity().getString(R.string.connected_to_wifi, CheckConnectivity.getWiFiName(getActivity()).replace("\"","")));
        }else{
            b.tvConnectionMessage.setText("No wifi connected");
        }
        b.tvInternetConnected.setText(CheckConnectivity.isOnline(getContext()) ? "Internet connected" : "No internet connection");
        c=getActivity();
        checkGPSEnabled();
        initRecycler();
        initBottomSheet();
        setUpRecycler();
        getFreeHotSpots();
        getNonFreeHotSpots();

        b.btnGetMoreWiFi.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CountrySelectActivity.class));
        });

        b.fabGPSSettings.setOnClickListener(v -> startGPSIntent());

        return b.getRoot();
    }

    private void getFreeHotSpots() {
        HomeActivity activity=((HomeActivity)getActivity());
        if (activity==null){
            Log.e(TAG, "getFreeHotspots: activity is null" );
            return;
        }
        activity.getWifiListOffline(new ScanResultsInterface() {
            @Override
            public void onScanResultsAvailable(List<ScanResult> scanResultInterfaces) {

            }

            @Override
            public void onSavedWifiResults(List<WifiConfiguration> savedResults) {
                Log.e(TAG, "onSavedWifiResults: "+savedResults.size() );
                for (WifiConfiguration a:savedResults
                     ) {
                    if (a.SSID.contentEquals(CheckConnectivity.getWiFiName(getActivity()))){
                        WifiModel m=new WifiModel();
                        m.setSsid(a.SSID);
                        m.setConnected(true);
                        wifiModels.add(m);
                    }else{
                        wifiModels.add(new WifiModel(a.SSID));
                    }
                }
                Log.e(TAG, "onSavedWifiResults: "+wifiModels.size() );
            }
        });
    }
    private void getNonFreeHotSpots() {
        HomeActivity activity=((HomeActivity)getActivity());
        if (activity==null){
            Log.e(TAG, "getFreeHotspots: activity is null" );
            return;
        }
        activity.getWifiListOffline(new ScanResultsInterface() {
            @Override
            public void onScanResultsAvailable(List<ScanResult> scanResultInterfaces) {
                Log.e(TAG, "onScanResultsAvailable: "+scanResultInterfaces.size() );
                List<WifiModel> wifiModels2=new ArrayList<>();
                for (ScanResult a:scanResultInterfaces
                ) {
                    boolean ispresent=false;
                    for (WifiModel aa:wifiModels
                         ) {
                        if (aa.getSsid().contentEquals(a.SSID.replace("\"",""))){
                            ispresent=true;
                        }
                    }
                    wifiModels2.add(new WifiModel(a.SSID.replace("\"","")));
                    if (!ispresent){
                        WifiModel m=new WifiModel();
                        m.setSsid(a.SSID);
                        protectedWifiModels.add(m);
                    }
                }
                setUpAgain(wifiModels2);
                Log.e(TAG, "onScanResultsAvailable: "+protectedWifiModels.size() );
                nonFreeAdapter.notifyDataSetChanged();

            }

            @Override
            public void onSavedWifiResults(List<WifiConfiguration> savedResults) {
            }
        });
    }

    private void setUpAgain(List<WifiModel> protectedWifiModels2) {
        Log.e(TAG, "setUpAgain: "+protectedWifiModels2.size() );
        List<WifiModel> wifiModels3=new ArrayList<>();
        for (WifiModel w:protectedWifiModels2
             ) {
            Log.e(TAG, "setUpAgain: "+w.getSsid());
            boolean isNearBy=false;
            for (WifiModel wifiModel:wifiModels ) {
                Log.e(TAG, "setUpAgain: -"+wifiModel.getSsid() );
                if (w.getSsid().trim().toLowerCase().contentEquals(wifiModel.getSsid().replace("\"","").trim().toLowerCase())){
                    isNearBy=true;
                }
            }
            if (isNearBy){
                wifiModels3.add(w);
            }
            wifiModels.clear();
            wifiModels.addAll(wifiModels3);
            freeAdapter.notifyDataSetChanged();

        }
    }

    private void setUpRecycler() {
        b.recyclerFreeHotspots.setLayoutManager(new LinearLayoutManager(getActivity()));
        freeAdapter = new WifiAdapter(wifiModels,c,R.layout.item_free_wifi,mBehavior,mBottomSheetDialog);
        b.recyclerFreeHotspots.setAdapter(freeAdapter);
        b.recyclerProtectedHotspots.setLayoutManager(new LinearLayoutManager(getActivity()));
        nonFreeAdapter = new WifiAdapter(protectedWifiModels,c,R.layout.item_wifi_password_required,mBehavior,mBottomSheetDialog);
        b.recyclerProtectedHotspots.setAdapter(nonFreeAdapter);
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