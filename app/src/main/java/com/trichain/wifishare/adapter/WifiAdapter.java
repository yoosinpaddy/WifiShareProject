package com.trichain.wifishare.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trichain.wifishare.model.WifiModel;

import java.util.List;

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.WifiAdapterViewHolder> {

    List<WifiModel> wifiModels;
    Context c;
    @NonNull
    @Override
    public WifiAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull WifiAdapterViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class WifiAdapterViewHolder extends RecyclerView.ViewHolder{

        public WifiAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
