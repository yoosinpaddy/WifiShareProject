package com.trichain.wifishare.adapter;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.trichain.wifishare.R;
import com.trichain.wifishare.activity.SignalCheckActivity;
import com.trichain.wifishare.activity.SpeedCheckActivity;
import com.trichain.wifishare.model.WifiModel;

import java.security.Security;
import java.util.List;
import java.util.UUID;

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.WifiAdapterViewHolder> {

    List<WifiModel> wifiModels;
    Context c;
    int layout;
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private static final String TAG = "WifiAdapter";
    private static final String WIFI_REPORT_ROOT = "wifi_report_locations";
    private static final String WIFI_ROOT = "wifi_locations";
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(WIFI_REPORT_ROOT);
    DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child(WIFI_ROOT);

    public WifiAdapter(List<WifiModel> wifiModels, Context c, int layout, BottomSheetBehavior mBehavior, BottomSheetDialog mBottomSheetDialog) {
        this.wifiModels = wifiModels;
        this.c = c;
        this.layout = layout;
        this.mBehavior = mBehavior;
        this.mBottomSheetDialog = mBottomSheetDialog;
    }

    @NonNull
    @Override
    public WifiAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WifiAdapterViewHolder(LayoutInflater.from(c).inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WifiAdapterViewHolder h, int position) {
        WifiModel w = wifiModels.get(position);
        h.tvSSIDName.setText(w.getSsid());
        getWifiStrengthRealtime(h.imgWiFiItem);//update strength realtime
        if (w.isConnected()){
        }else{
            h.tvConnectionStatus.setText("");
        }
        if (w.isSecured()){
            h.tvSecurityStatus.setText("Secured");
        }else{
            h.tvSecurityStatus.setText("Not Secured");
        }
        h.btnItemMenu.setOnClickListener(v->{showMenu(h,w);});

    }

    private void showMenu(WifiAdapterViewHolder h, WifiModel w) {
            if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            final View view = LayoutInflater.from(c).inflate(R.layout.sheet_basic, null);
            if (!w.isConnected()){
                (view.findViewById(R.id.llPost)).setVisibility(View.GONE);
                (view.findViewById(R.id.llSecurity)).setVisibility(View.GONE);
                (view.findViewById(R.id.llSpeed)).setVisibility(View.GONE);
                (view.findViewById(R.id.llInvite)).setVisibility(View.GONE);
                (view.findViewById(R.id.llConnect)).setVisibility(View.GONE);
            }else{
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
                    Intent i=new Intent(c, Security.class);
                    c.startActivity(i);
                }
            });
            (view.findViewById(R.id.fabSpeed)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(c, SpeedCheckActivity.class);
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
                    Intent i=new Intent(c, SignalCheckActivity.class);
                    i.putExtra("ssid",w.getSsid());
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
            if(!intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE).toString().equals(SupplicantState.SCANNING))
                wifi.disconnect();
            showLoader(false);
        }
    }

    private void forget(WifiModel wifiModel) {
        WifiManager manager=((WifiManager) c.getSystemService(Context.WIFI_SERVICE));
        @SuppressLint("MissingPermission") List<WifiConfiguration> list = manager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if (i.SSID.contentEquals(wifiModel.getSsid())){
                Log.e(TAG, "forget: "+wifiModel.getSsid() );
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

    }

    private void shareInFireBase(WifiModel wifiModel) {
        showLoader(true);
        String id = UUID.randomUUID().toString();
        ref2.child(id).setValue(wifiModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e(TAG, "onComplete: " + task.toString());
                        showLoader(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showLoader(false);
            }
        });


    }

    private void inviteNative(WifiModel wifiModel) {Intent intent = new Intent();
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
        TextView tvSSIDName,tvSecurityStatus,tvConnectionStatus;
        ImageView imgWiFiItem,btnItemMenu;
        LinearLayout ll_security_connection;
        public WifiAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSSIDName=itemView.findViewById(R.id.tvSSIDName);
            ll_security_connection=itemView.findViewById(R.id.ll_security_connection);
            imgWiFiItem=itemView.findViewById(R.id.imgWiFiItem);
            tvSecurityStatus=itemView.findViewById(R.id.tvSecurityStatus);
            tvConnectionStatus=itemView.findViewById(R.id.tvConnectionStatus);
            btnItemMenu=itemView.findViewById(R.id.btnItemMenu);
        }
    }
}
