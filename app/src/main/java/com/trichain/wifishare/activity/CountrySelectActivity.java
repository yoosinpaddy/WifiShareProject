package com.trichain.wifishare.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trichain.wifishare.R;
import com.trichain.wifishare.adapter.CountriesAdapter;
import com.trichain.wifishare.databinding.ActivityCountrySelectBinding;
import com.trichain.wifishare.model.Country;

import java.util.ArrayList;
import java.util.List;

public class CountrySelectActivity extends AppCompatActivity {

    private ActivityCountrySelectBinding b;
    private List<Country> countryList = new ArrayList<>();
    private CountriesAdapter countriesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_country_select);

        countriesAdapter = new CountriesAdapter(countryList, this);
        b.recyclerCountry.setLayoutManager(new LinearLayoutManager(this));
        b.recyclerCountry.setAdapter(countriesAdapter);

        initTooBar();

        loadCountries();

        setUpListeners();

    }


    private void initTooBar() {
        setSupportActionBar(b.toolbarCountry);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadCountries() {
        String[] countriesArray = getResources().getStringArray(R.array.countries_array);

        for (String country : countriesArray) {
            Country c = new Country(country, 0);
            countryList.add(c);
        }
        countriesAdapter.notifyDataSetChanged();
    }

    private void setUpListeners() {
        /*Adapter country selected listener*/
        countriesAdapter.setCountrySelectedListener(new CountriesAdapter.CountrySelectedListener() {
            @Override
            public void onCountrySelected(Country c, int pos) {

                /*b.tvCountryNameSelected.setText(c.getCountryName());
                b.tvCountryNumUsersSelected.setText(c.getUsersNumCount() + "+ free hotspots");*/

                //TODO: Save country to room
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}