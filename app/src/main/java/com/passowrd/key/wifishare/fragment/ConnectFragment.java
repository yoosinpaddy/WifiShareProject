package com.passowrd.key.wifishare.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.passowrd.key.wifishare.R;
import com.passowrd.key.wifishare.activity.HomeActivity;
import com.passowrd.key.wifishare.activity.MapsActivity;
import com.passowrd.key.wifishare.activity.WifiBaseActivity;
import com.passowrd.key.wifishare.adapter.WifiAdapter;
import com.passowrd.key.wifishare.databinding.FragmentConnectBinding;
import com.passowrd.key.wifishare.listeners.ScanResultsInterface;
import com.passowrd.key.wifishare.model.WifiModel;
import com.passowrd.key.wifishare.util.CheckConnectivity;
import com.passowrd.key.wifishare.util.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ConnectFragment extends Fragment implements WifiBaseActivity.WiFiConnectionListener {

    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 332;
    private static final String TAG = "ConnectFragment";
    private FragmentConnectBinding b;
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    WifiAdapter freeAdapter, nonFreeAdapter;
    List<WifiModel> wifiModels = new ArrayList<>();
    List<WifiModel> protectedWifiModels = new ArrayList<>();
    private Timer timerFree, timerSecure;
    private Snackbar sbInfo;
    Dialog dialog;

    Context c;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = DataBindingUtil.inflate(inflater, R.layout.fragment_connect, container, false);

        updateWiFiConnectedLabel();

        b.tvInternetConnected.setText(CheckConnectivity.isOnline(getContext()) ? "Internet connected" : "No internet connection");

        b.btnGetMoreWiFi.setOnClickListener(v -> startRunnables());


        HomeActivity activity = ((HomeActivity) getActivity());
        activity.setWiFiConnectionListener(this);

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

    private void updateWiFiConnectedLabel() {
        if (!CheckConnectivity.getWiFiName(getActivity()).contains("<unknown ssid>")) {
            b.tvConnectionMessage.setText(getActivity().getString(R.string.connected_to_wifi, CheckConnectivity.getWiFiName(getActivity()).replace("\"", "")));
            if (!CheckConnectivity.isOnline(getActivity())) {
                b.tvInternetConnected.setText("WiFi has no internet connection!");
            } else {
                b.tvInternetConnected.setText("Internet connected");
            }
        } else {
            b.tvConnectionMessage.setText("No wifi connected");
        }
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
                b.srlMain.setRefreshing(false);
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
                    nonFreeAdapter.notifyDataSetChanged();
                    freeAdapter.notifyDataSetChanged();
                });
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
                if (!wifiModel.isConnected()) showRealConnectDialog(wifiModel, position, 1200);
                else showConnectDialog(wifiModel, position, false);
            }
        });

        nonFreeAdapter.setWiFiSelectionListener(new WifiAdapter.WiFiSelectionListener() {
            @Override
            public void onWiFiSelected(WifiModel wifiModel, int position) {
                showConnectDialog(wifiModel, position, false);
            }
        });

        util.showView(b.pbFreeWifi, true);
        util.showView(b.pbProtectedWifi, true);

        startRunnables();

    }


    int curr = -1;
    int viewcount = 0;

    private void showRealConnectDialog(WifiModel wifiModel, int position, long length) {
        View root = LayoutInflater.from(c).inflate(R.layout.dialog_information, null);
        dialog = new Dialog(c, R.style.Theme_CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(root);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        TextView networkName = root.findViewById(R.id.networkName);
        TextView tvstatus = root.findViewById(R.id.tvstatus);
        TextView tvTesting1 = root.findViewById(R.id.tvTesting1);
        TextView tvTesting2 = root.findViewById(R.id.tvTesting2);
        TextView tvTesting3 = root.findViewById(R.id.tvTesting3);

        networkName.setText(wifiModel.getSsid());
        tvstatus.setText("securely connecting...");

        dialog.show();

        View[] items = new View[]{tvTesting1, tvTesting2, tvTesting3};
        viewcount = items.length - 1;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (curr < viewcount) {
                    getActivity().runOnUiThread(() -> {
                        util.showView(items[curr], true);
                    });
                    curr++;
                } else {
                    curr = -1;
                    getActivity().runOnUiThread(() -> {
                        dialog.dismiss();
                        startRunnables();
                        timer.cancel();
                    });
                }
            }
        }, 1000, length);
    }


    private void showConnectDialog(WifiModel wifiModel, int position, boolean isWrongPass) {
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        View root = LayoutInflater.from(getContext()).inflate(R.layout.dialog_connect_to_wifi, null);
        TextInputEditText edtPassword = root.findViewById(R.id.edtConnectPassword);
        MaterialCheckBox checkBox = root.findViewById(R.id.cbxShareConnect);
        TextView tvWifiNameConnectDialog = root.findViewById(R.id.tvWifiNameConnectDialog);
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        tvWifiNameConnectDialog.setText(wifiModel.getSsid());
        if (isWrongPass) {
            edtPassword.setError("Incorrect password!");
        }

        b.setPositiveButton("Connect", (dialogInterface, i) -> {
            String password = edtPassword.getText().toString().trim();
            if (!TextUtils.isEmpty(password)) {
                wifiModel.setPassword(password);
                connectToWiFi(checkBox, wifiModel);
                showRealConnectDialog(wifiModel, 0, 15000);
                final boolean[] hasConnected = {false};
                final int[] a = {0};
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        a[0]++;
                        WifiManager wifiManager = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        WifiInfo info = wifiManager.getConnectionInfo();
                        String ssid = info.getSSID();
                        Log.e(TAG, "run: has connected:" + ssid);
                        if (ssid.contentEquals(wifiModel.getSsid())) {
                            hasConnected[0] = true;
//                            showLoader(false);
                        } else {
                            hasConnected[0] = false;
                            if (a[0] >= 15) {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                showConnectDialog(wifiModel, 0, true);
                            } else {
                                new Handler().postDelayed(this, 1000);

                            }
                        }
                    }
                }, 1000);
            } else {
                edtPassword.setError("Password is required");
                edtPassword.requestFocus();
            }
        });
        b.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });

        b.setView(root);
        AlertDialog c=b.create();
        c.show();

        // Initially disable the button
        ((AlertDialog) c).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        // OR you can use here setOnShowListener to disable button at first time.

        // Now set the textchange listener for edittext
        edtPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                // Check if edittext is empty
                if (s.toString().length()<8) {
                    // Disable ok button
                    ((AlertDialog) c).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                } else {
                    // Something into edit text. Enable the button.
                    ((AlertDialog) c).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }

            }
        });

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
        b.srlMain.setRefreshing(true);
        stopRunnables();
        if (!checkGPSEnabled()) {
            b.srlMain.setRefreshing(false);
            return;
        }
        //Toast.makeText(c, "Searching for WiFi", Toast.LENGTH_SHORT).show();
        timerFree = new Timer();
        timerFree.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getFreeHotSpots();
            }
        }, 600, 7000);

        timerSecure = new Timer();
        timerSecure.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
//                getNonFreeHotSpots();
            }
        }, 1600, 7000);
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

    public boolean checkGPSEnabled() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGPSEnabled) {
            toggleVisibility(0);
        } else {
            toggleVisibility(1);
        }
        return isGPSEnabled;
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


    @Override
    public void onWiFiStatusChanged(Boolean isWiFiOn) {
        updateWiFiConnectedLabel();
        sbInfo = Snackbar.make(b.appBar, "WiFi NOT connected", BaseTransientBottomBar.LENGTH_INDEFINITE);
        if (!isWiFiOn) {
            sbInfo.setAction("Connect", v -> {
                Log.e(TAG, "onWiFiStatusChanged: Attempting to switch on wifi");
                HomeActivity activity = ((HomeActivity) getActivity());
                if (activity == null) {
                    Log.e(TAG, "onWiFiStatusChanged: activity is null");
                    return;
                }
                activity.turnOnWIFI();
            });
            sbInfo.show();
        } else {
            sbInfo.dismiss();
        }

    }
}