package com.trichain.wifishare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trichain.wifishare.R;
import com.trichain.wifishare.model.Country;

import java.util.List;

public class CountriesAdapter extends RecyclerView.Adapter<CountriesAdapter.ViewHolder> {

    private List<Country> countryList;
    private Context context;

    private CountrySelectedListener countrySelectedListener;

    public interface CountrySelectedListener {
        void onCountrySelected(Country c, int pos);
    }

    public void setCountrySelectedListener(CountrySelectedListener countrySelectedListener) {
        this.countrySelectedListener = countrySelectedListener;
    }

    public CountriesAdapter(List<Country> countryList, Context context) {
        this.countryList = countryList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_country, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Country country = countryList.get(position);
        holder.country_name.setText(country.getCountryName());
        holder.num_users.setText(country.getUsersNumCount() + "+ free hotspots");

        holder.itemView.setOnClickListener(v -> {
            countrySelectedListener.onCountrySelected(country, position);
        });
    }

    @Override
    public int getItemCount() {
        return countryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView country_name, num_users;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            country_name = itemView.findViewById(R.id.country_name);
            num_users = itemView.findViewById(R.id.tv_country_num_users);

        }
    }
}
