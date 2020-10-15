package com.trichain.wifishare.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.trichain.wifishare.R;
import com.trichain.wifishare.activity.HomeActivity;
import com.trichain.wifishare.activity.SecurityCheckActivity;
import com.trichain.wifishare.activity.SignalCheckActivity;
import com.trichain.wifishare.activity.SpeedCheckActivity;
import com.trichain.wifishare.model.WifiModel;
import com.trichain.wifishare.util.util;

import java.security.Security;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.WifiAdapterViewHolder> {

    List<WifiModel> wifiModels;
    Context c;
    int layout;
    Dialog dialog;
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private static final String TAG = "WifiAdapter";
    private static final String WIFI_REPORT_ROOT = "wifi_report_locations";
    private static final String WIFI_ROOT = "wifi_locations";
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(WIFI_REPORT_ROOT);
    DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child(WIFI_ROOT);
    private WiFiSelectionListener wiFiSelectionListener;

    public interface WiFiSelectionListener {
        void onWiFiSelected(WifiModel wifiModel, int position);
    }

    public void setWiFiSelectionListener(WiFiSelectionListener wiFiSelectionListener) {
        this.wiFiSelectionListener = wiFiSelectionListener;
    }

    public WifiAdapter(List<WifiModel> wifiModels, Context c, int layout, BottomSheetBehavior mBehavior, BottomSheetDialog mBottomSheetDialog) {
        this.wifiModels = wifiModels;
        this.c = c;
        this.layout = layout;
        this.mBehavior = mBehavior;
        this.mBottomSheetDialog = mBottomSheetDialog;
        wiFiSelectionListener = (wifiModel, position) -> {
        };
    }

    @NonNull
    @Override
    public WifiAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WifiAdapterViewHolder(LayoutInflater.from(c).inflate(layout, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull WifiAdapterViewHolder h, int position) {
        Log.e(TAG, "onBindViewHolder: ");
        WifiModel w = wifiModels.get(position);
        h.tvSSIDName.setText(w.getSsid());
        getWifiStrengthRealtime(h.imgWiFiItem);//update strength realtime
        Log.e(TAG, "onBindViewHolder: isWifiConnected: " + w.isConnected());
        if (w.isConnected()) {
            h.tvConnectionStatus.setText("Connected");
            util.hideView(h.fabConnect, false);
            util.showView(h.imgConnectionStatus, false);
            h.itemView.setOnClickListener(v -> {
                Toast.makeText(c, "You are already connected to this network", Toast.LENGTH_SHORT).show();
                //wiFiSelectionListener.onWiFiSelected(w, position);
            });
        } else {
            util.showView(h.fabConnect, false);
            util.hideView(h.imgConnectionStatus, false);
            h.tvConnectionStatus.setText("");

            h.fabConnect.setOnClickListener(v -> connect(w));

            h.itemView.setOnClickListener(v -> {
                connect(w);
                //wiFiSelectionListener.onWiFiSelected(w, position);
            });

        }
        if (w.isSecured()) {
            h.tvSecurityStatus.setText("Secured");
        } else {
            h.tvSecurityStatus.setText("Not Secured");
        }

        if (w.getLevel() != null) {
            int signalStrength = 100 + w.getLevel();
            Log.e(TAG, "onBindViewHolder: " + w.getSsid() + " level: " + w.getLevel());
            if (isBetween(signalStrength, 0, 25)) {
                h.imgWiFiItem.setImageResource(w.isSecured() ? R.drawable.ic_wifi_secure_empty : R.drawable.ic_wifi_empty);
            } else if (isBetween(signalStrength, 26, 50)) {
                h.imgWiFiItem.setImageResource(w.isSecured() ? R.drawable.ic_wifi_secure_signal_1 : R.drawable.ic_wifi_signal_1);
            } else if (isBetween(signalStrength, 51, 75)) {
                h.imgWiFiItem.setImageResource(w.isSecured() ? R.drawable.ic_wifi_secure_signal_2 : R.drawable.ic_wifi_signal_2);
            } else if (isBetween(signalStrength, 76, 100)) {
                h.imgWiFiItem.setImageResource(w.isSecured() ? R.drawable.ic_wifi_secure_signal_fill : R.drawable.ic_wifi_fill);
            }
        }

        h.btnItemMenu.setOnClickListener(v -> {
            showMenu(h, w);
        });


    }

    public static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }


    private void showMenu(WifiAdapterViewHolder h, WifiModel w) {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = LayoutInflater.from(c).inflate(R.layout.sheet_basic, null);
        if (!w.isConnected()) {
            (view.findViewById(R.id.llPost)).setVisibility(View.GONE);
            (view.findViewById(R.id.llSecurity)).setVisibility(View.GONE);
            (view.findViewById(R.id.llSpeed)).setVisibility(View.GONE);
            (view.findViewById(R.id.llInvite)).setVisibility(View.GONE);
            (view.findViewById(R.id.llConnect)).setVisibility(View.GONE);
        } else {
            (view.findViewById(R.id.llDisconnect)).setVisibility(View.GONE);
        }
        (view.findViewById(R.id.fabPost)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postFacebook(w);
            }
        });
        (view.findViewById(R.id.fabSecurity)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, SecurityCheckActivity.class);
                c.startActivity(i);
            }
        });
        (view.findViewById(R.id.fabSpeed)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, SpeedCheckActivity.class);
                c.startActivity(i);
            }
        });
        (view.findViewById(R.id.fabInvite)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteNative(w);
            }
        });
        (view.findViewById(R.id.fabSignal)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, SignalCheckActivity.class);
                i.putExtra("ssid", w.getSsid());
                c.startActivity(i);
            }
        });
        (view.findViewById(R.id.fabShare)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareInFireBase(w);
            }
        });
        (view.findViewById(R.id.fabReport)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportWifi(w);
            }
        });
        (view.findViewById(R.id.forget)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forget(w);
                AlertDialog.Builder b= new AlertDialog.Builder(c);
                b.setTitle("Failed to forget network");
                b.setMessage("Due to system restriction, failed to forget network in the app. Please proceed to phone settings->Wi-Fi (WLAN)");
                b.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        c.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                    }
                }).setNegativeButton("Cancel", (dialog1, which) -> {
                    dialog1.dismiss();
                });
                b.show();
            }
        });
        (view.findViewById(R.id.disconnect)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disConnect(w);
            }
        });
        (view.findViewById(R.id.connect)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect(w);
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(c);
        mBottomSheetDialog.setContentView(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialog = null;
            }
        });

    }

    private void postFacebook(WifiModel w) {
        inviteNative(w);
    }

    private void connect(WifiModel w) {
        final boolean[] hasConnected = {false};
        //showLoader(true, "Connecting");
        //showRealConnectDialog(w, 0);
        wiFiSelectionListener.onWiFiSelected(w, 0);
        String ssid = w.getSsid();
        String key = w.getPassword();
        WifiManager wifiManager = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                Log.e(TAG, "connectToWifi: " + ssid);

                break;
            } else {
                Log.e(TAG, "connectToWifi: non Target: " + ssid);
            }
        }

    }

    private void showLoader(boolean b, String message) {
        if (!b) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            return;
        }
        dialog = new Dialog(c, R.style.Theme_CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(true);

        ((TextView) dialog.findViewById(R.id.text_status)).setText(message);
        ((AppCompatButton) dialog.findViewById(R.id.btnDeclineLeadership)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(c.getApplicationContext(), "Canceled", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void disConnect(WifiModel w) {
        showLoader(true);
        WifiManager wifi = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        wifi.disconnect();
        DisconnectWifi discon = new DisconnectWifi(wifi);
        c.registerReceiver(discon, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));

    }

    public class DisconnectWifi extends BroadcastReceiver {
        WifiManager wifi;

        public DisconnectWifi(WifiManager wifi) {
            this.wifi = wifi;
        }

        @Override
        public void onReceive(Context c, Intent intent) {
            if (!intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE).toString().equals(SupplicantState.SCANNING))
                wifi.disconnect();
            showLoader(false);
        }
    }

    private void forget(WifiModel wifiModel) {
        WifiManager manager = ((WifiManager) c.getSystemService(Context.WIFI_SERVICE));
        @SuppressLint("MissingPermission") List<WifiConfiguration> list = manager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID.contentEquals(wifiModel.getSsid())) {
                Log.e(TAG, "forget: " + wifiModel.getSsid());
                manager.removeNetwork(i.networkId);
                manager.saveConfiguration();
            }
        }
    }

    private void reportWifi(WifiModel wifiModel) {
        showLoader(true);
        String id = UUID.randomUUID().toString();
        ref.child(id).setValue(wifiModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showLoader(false);
                        Log.e(TAG, "onComplete: " + task.toString());
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showLoader(false);
                Log.e(TAG, "onComplete: " + e.getMessage());

            }
        });
    }

    private void showLoader(boolean b) {
        if(mBehavior!=null){
            if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
        if (!b) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            return;
        }
        dialog = new Dialog(c);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


        ((TextView) dialog.findViewById(R.id.text_status)).setText("Connecting...");
        ((AppCompatButton) dialog.findViewById(R.id.btnDeclineLeadership)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(c.getApplicationContext(), "Declined", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);

    }

    private void shareInFireBase(WifiModel wifiModel) {
        showLoader(true, "Sharing...");
        HomeActivity activity = ((HomeActivity) c);
        if (activity == null) {
            Log.e(TAG, "getFreeHotspots: activity is null");
            return;
        }

        activity.addWifiToFirebase(wifiModel.getSsid(), wifiModel.getPassword(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.e(TAG, "onComplete: " + task.toString());
                showLoader(false);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showLoader(false);
            }
        });
        String id = UUID.randomUUID().toString();
        ref2.child(id).setValue(wifiModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e(TAG, "onComplete: " + task.toString());
                        Toast.makeText(activity, "WiFi shared successfully", Toast.LENGTH_SHORT).show();
                        showLoader(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showLoader(false);
            }
        });


    }

    private void inviteNative(WifiModel wifiModel) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);

//# change the type of data you need to share,
//# for image use "image/*"
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Download Wifi share app from playstore play.google.com/store/apps/details?id=com.trichain.wifishare");
        c.startActivity(Intent.createChooser(intent, "Share"));
    }

    private void getWifiStrengthRealtime(ImageView imgWiFiItem) {
    }

    @Override
    public int getItemCount() {
        return wifiModels.size();
    }

    class WifiAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView tvSSIDName, tvSecurityStatus, tvConnectionStatus;
        ImageView imgWiFiItem, btnItemMenu, imgConnectionStatus;
        LinearLayout ll_security_connection;
        ExtendedFloatingActionButton fabConnect;

        public WifiAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSSIDName = itemView.findViewById(R.id.tvSSIDName);
            ll_security_connection = itemView.findViewById(R.id.ll_security_connection);
            imgWiFiItem = itemView.findViewById(R.id.imgWiFiItem);
            fabConnect = itemView.findViewById(R.id.fabConnectItem);
            imgConnectionStatus = itemView.findViewById(R.id.imgConnectionStatus);
            tvSecurityStatus = itemView.findViewById(R.id.tvSecurityStatus);
            tvConnectionStatus = itemView.findViewById(R.id.tvConnectionStatus);
            btnItemMenu = itemView.findViewById(R.id.btnItemMenu);
        }
    }
}
