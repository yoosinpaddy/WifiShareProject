package com.trichain.wifishare.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.trichain.wifishare.R;
import com.trichain.wifishare.activity.HomeActivity;
import com.trichain.wifishare.activity.MapsActivity;
import com.trichain.wifishare.adapter.WifiAdapter;
import com.trichain.wifishare.databinding.FragmentConnectBinding;
import com.trichain.wifishare.listeners.ScanResultsInterface;
import com.trichain.wifishare.model.WifiModel;
import com.trichain.wifishare.util.CheckConnectivity;
import com.trichain.wifishare.util.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ConnectFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 332;
    private static final String TAG = "ConnectFragment";
    private FragmentConnectBinding b;
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    WifiAdapter freeAdapter, nonFreeAdapter;
    List<WifiModel> wifiModels = new ArrayList<>();
    List<WifiModel> protectedWifiModels = new ArrayList<>();
    private Timer timerFree, timerSecure;

    Context c;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = DataBindingUtil.inflate(inflater, R.layout.fragment_connect, container, false);
        if (!CheckConnectivity.getWiFiName(getActivity()).contains("<unknown ssid>")) {
            b.tvConnectionMessage.setText(getActivity().getString(R.string.connected_to_wifi, CheckConnectivity.getWiFiName(getActivity()).replace("\"", "")));
        } else {
            b.tvConnectionMessage.setText("No wifi connected");
        }
        b.tvInternetConnected.setText(CheckConnectivity.isOnline(getContext()) ? "Internet connected" : "No internet connection");

        b.btnGetMoreWiFi.setOnClickListener(v -> startRunnables());


        b.srlMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startRunnables();
            }
        });

        c = getActivity();
        checkGPSEnabled();
        initRecycler();
        initBottomSheet();
        setUpRecycler();
        setUpMapListener();

        /*getFreeHotSpots();

        getNonFreeHotSpots();*/

        b.fabGPSSettings.setOnClickListener(v -> startGPSIntent());

        return b.getRoot();
    }

    private void setUpMapListener() {
        b.mapRipple.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MapsActivity.class));
        });
    }

    private void getFreeHotSpots() {
        HomeActivity activity = ((HomeActivity) getActivity());
        if (activity == null) {
            Log.e(TAG, "getFreeHotspots: activity is null");
            return;
        }
        activity.getWifiListOffline(new ScanResultsInterface() {
            @Override
            public void onScanResultsAvailable(List<ScanResult> scanResultInterfaces) {
                Log.e(TAG, "getFreeHotSpots onScanResultsAvailable: " + scanResultInterfaces.size());

            }

            @Override
            public void onSavedWifiResults(List<WifiConfiguration> savedResults) {
                Log.e(TAG, "onSavedWifiResults: before clear");
                wifiModels.clear();
                Log.e(TAG, "onSavedWifiResults: after clear");
                Log.e(TAG, "onSavedWifiResults: " + savedResults.size());
                for (WifiConfiguration a : savedResults) {
                    if (a.SSID.contentEquals(CheckConnectivity.getWiFiName(getActivity()))) {
                        WifiModel m = new WifiModel();
                        m.setSsid(a.SSID);
                        m.setConnected(true);
                        wifiModels.add(m);
                    } else {
                        wifiModels.add(new WifiModel(a.SSID));
                    }
                }
                getNonFreeHotSpots();
                Log.e(TAG, "onSavedWifiResults: " + wifiModels.size());
            }
        });
    }

    private void getNonFreeHotSpots() {
        HomeActivity activity = ((HomeActivity) getActivity());
        if (activity == null) {
            Log.e(TAG, "getFreeHotspots: activity is null");
            return;
        }
        activity.getWifiListOffline(new ScanResultsInterface() {
            @Override
            public void onScanResultsAvailable(List<ScanResult> scanResultInterfaces) {
                getActivity().runOnUiThread(() -> {
                    util.hideView(b.pbProtectedWifi, true);
                });
                Log.e(TAG, "getNonFreeHotSpots onScanResultsAvailable: " + scanResultInterfaces.size());
                protectedWifiModels.clear();
                List<WifiModel> wifiModels2 = new ArrayList<>();
                for (ScanResult a : scanResultInterfaces) {
                    boolean ispresent = false;
                    for (WifiModel aa : wifiModels) {
                        String mySSID = aa.getSsid().replace("\"", "");
                        if (a.SSID.contains(mySSID)) {
//                            Log.e(TAG, "GetNonFreeHotSpots: Does Not:  if:"+mySSID+":"+a.SSID);
                            ispresent = true;
                        } else {
//                            Log.e(TAG, "GetNonFreeHotSpots: Does Not:else:"+mySSID+":"+a.SSID);
                        }
                    }
                    if (!ispresent) {
                        Log.e(TAG, "protectedWifiModels: if:" + a.SSID);
                        WifiModel m = new WifiModel();
                        m.setSsid(a.SSID);
                        m.setSecured(a.capabilities.contains("WPA2-PSK") || a.capabilities.contains("WPA-PSK"));
//                        Log.e(TAG, "getWifiListOffline: Comparing: " + a.SSID + " with: " + CheckConnectivity.getWiFiName(activity).replace("\"", ""));
//                        Log.e(TAG, "getWifiListOffline: isConnected to: " + a.SSID + " = " +
                        a.SSID.toLowerCase().contentEquals(CheckConnectivity.getWiFiName(activity).replace("\"", "").toLowerCase());
                        boolean isConnected = a.SSID.toLowerCase().contentEquals(CheckConnectivity.getWiFiName(activity).replace("\"", "").toLowerCase());
                        m.setConnected(isConnected);
                        m.setLevel(a.level);
                        Log.e(TAG, "onScanResultsAvailable: current model connected " + m.getSsid() + " = " + m.isConnected());
                        protectedWifiModels.add(m);
                    } else {
                        Log.e(TAG, "protectedWifiModels:else:" + a.SSID);
                        Log.e(TAG, "onScanResultsAvailable: ");
                        WifiModel m = new WifiModel();
                        m.setSsid(a.SSID);
                        m.setSecured(a.capabilities.contains("WPA2-PSK") || a.capabilities.contains("WPA-PSK"));
//                        Log.e(TAG, "getWifiListOffline: Comparing: " + a.SSID + " with: " + CheckConnectivity.getWiFiName(activity).replace("\"", ""));
//                        Log.e(TAG, "getWifiListOffline: isConnected to: " + a.SSID + " = " +
                        a.SSID.toLowerCase().contentEquals(CheckConnectivity.getWiFiName(activity).replace("\"", "").toLowerCase());
                        boolean isConnected = a.SSID.toLowerCase().contentEquals(CheckConnectivity.getWiFiName(activity).replace("\"", "").toLowerCase());
                        m.setConnected(isConnected);
                        m.setLevel(a.level);
                        Log.e(TAG, "onScanResultsAvailable: current model connected " + m.getSsid() + " = " + m.isConnected());
                        wifiModels2.add(m);
                    }
                }


                wifiModels.clear();
                wifiModels.addAll(wifiModels2);
                Log.e(TAG, "wifiModels: " + wifiModels.size());
                Log.e(TAG, "protectedWifiModels: " + protectedWifiModels.size());
                getActivity().runOnUiThread(() -> {
                    util.hideView(b.pbFreeWifi, true);
                });
                nonFreeAdapter.notifyDataSetChanged();
                freeAdapter.notifyDataSetChanged();
                //setUpAgain(wifiModels2);
//                setUpAgain(protectedWifiModels);
//                Log.e(TAG, "onScanResultsAvailable: protectedWifiModels:" + protectedWifiModels.size());

            }

            @Override
            public void onSavedWifiResults(List<WifiConfiguration> savedResults) {
            }
        });
    }

    private void setUpAgain(List<WifiModel> protectedWifiModels2) {
        Log.e(TAG, "setUpAgain: " + protectedWifiModels2.size());
        Log.e(TAG, "setUpAgain: List of available WiFi " + protectedWifiModels2.toString());
        List<WifiModel> wifiModels3 = new ArrayList<>();

        for (WifiModel w : protectedWifiModels2) {
            Log.e(TAG, "setUpAgain Selected: " + w.getSsid());
            boolean isNearBy = false;

            for (WifiModel wifiModel : wifiModels) {
                //Log.e(TAG, "setUpAgain: -" + wifiModel.getSsid());
                Log.e(TAG, "setUpAgain: Comparing: " + w.getSsid() + " with: " + wifiModel.getSsid());
                if (w.getSsid().trim().toLowerCase().contentEquals(wifiModel.getSsid().replace("\"", "").trim().toLowerCase())) {
                    isNearBy = true;
                    Log.e(TAG, "setUpAgain: " + w.getSsid() + " is nearby. Adding to list...");
                    break;
                }
            }
            if (isNearBy) {
                wifiModels3.add(w);
            } else {
                Log.e(TAG, "setUpAgain: " + w.getSsid() + " is NOT nearby. Skipping...");
            }

        }


        wifiModels.clear();
        wifiModels.addAll(wifiModels3);
        nonFreeAdapter.notifyDataSetChanged();
        freeAdapter.notifyDataSetChanged();

    }

    private void setUpRecycler() {
        b.recyclerFreeHotspots.setLayoutManager(new LinearLayoutManager(getActivity()));
        freeAdapter = new WifiAdapter(wifiModels, c, R.layout.item_free_wifi, mBehavior, mBottomSheetDialog);
        b.recyclerFreeHotspots.setAdapter(freeAdapter);
        b.recyclerProtectedHotspots.setLayoutManager(new LinearLayoutManager(getActivity()));
        nonFreeAdapter = new WifiAdapter(protectedWifiModels, c, R.layout.item_wifi_password_required, mBehavior, mBottomSheetDialog);
        b.recyclerProtectedHotspots.setAdapter(nonFreeAdapter);

        freeAdapter.setWiFiSelectionListener(new WifiAdapter.WiFiSelectionListener() {
            @Override
            public void onWiFiSelected(WifiModel wifiModel, int position) {
                if (!wifiModel.isConnected()) showConnectDialog(wifiModel, position);
            }
        });

        nonFreeAdapter.setWiFiSelectionListener(new WifiAdapter.WiFiSelectionListener() {
            @Override
            public void onWiFiSelected(WifiModel wifiModel, int position) {
                showConnectDialog(wifiModel, position);
            }
        });

        util.showView(b.pbFreeWifi, true);
        util.showView(b.pbProtectedWifi, true);

        startRunnables();

    }

    private void showConnectDialog(WifiModel wifiModel, int position) {
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        View root = LayoutInflater.from(getContext()).inflate(R.layout.dialog_connect_to_wifi, null);
        TextInputEditText edtPassword = root.findViewById(R.id.edtConnectPassword);
        MaterialCheckBox checkBox = root.findViewById(R.id.cbxShareConnect);
        TextView tvWifiNameConnectDialog = root.findViewById(R.id.tvWifiNameConnectDialog);

        tvWifiNameConnectDialog.setText(wifiModel.getSsid());

        b.setPositiveButton("Connect", (dialogInterface, i) -> {
            String password = edtPassword.getText().toString().trim();
            if (!TextUtils.isEmpty(password)) {
                wifiModel.setPassword(password);
                connectToWiFi(checkBox, wifiModel);
            } else {
                edtPassword.setError("Password is required");
                edtPassword.requestFocus();
            }
        });
        b.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });

        b.setView(root);
        b.show();

    }

    private void connectToWiFi(MaterialCheckBox checkBox, WifiModel wifiModel) {
        HomeActivity activity = ((HomeActivity) getActivity());
        if (activity == null) {
            Log.e(TAG, "connectToWiFi: activity is null");
            return;
        }
        activity.connectToNewWifi(wifiModel);

        if (checkBox.isChecked()) {
            activity.addWifiToFirebase(wifiModel.getSsid(), wifiModel.getPassword());
        }


    }

    private void startRunnables() {
        stopRunnables();
        Toast.makeText(c, "Searching for WiFi", Toast.LENGTH_SHORT).show();
        timerFree = new Timer();
        timerFree.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getFreeHotSpots();
            }
        }, 600, 5000);

        timerSecure = new Timer();
        timerSecure.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
//                getNonFreeHotSpots();
            }
        }, 1600, 5000);
    }

    @Override
    public void onPause() {
        stopRunnables();
        HomeActivity activity = ((HomeActivity) getActivity());
        assert activity != null;
        activity.unregisterReceivers();
        super.onPause();
    }

    private void stopRunnables() {
        if (timerSecure != null) {
            timerSecure.cancel();
        }
        if (timerFree != null) {
            timerFree.cancel();
        }
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