package com.thisisnotajoke.android.cityscape.mobile.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;
import com.thisisnotajoke.android.cityscape.lib.World;
import com.thisisnotajoke.android.cityscape.lib.model.City;
import com.thisisnotajoke.android.cityscape.mobile.R;
import com.thisisnotajoke.android.cityscape.mobile.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CompanionConfigActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks {
    private Spinner mCitySpinner;
    private static String TAG = "CompanionConfigActivity";
    private GoogleApiClient mGoogleApiClient;
    private ArrayAdapter<City> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        mCitySpinner = (Spinner) findViewById(R.id.activity_config_city_spinner);
        List<City> cities = new ArrayList<>(Arrays.asList(World.CITIES));
        cities.add(0, new City(null, "Automatic"));
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cities);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.e(TAG, "Could not connect to play services: " + connectionResult.toString());
                    }
                })
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        UUID cityId = ((City) adapterView.getItemAtPosition(i)).getID();
        String id = cityId != null ? cityId.toString() : null;
        Log.d(TAG, "Setting city ID to " + id);
        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putString(Util.KEY_CITY, id);
        Util.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Util.fetchConfigDataMap(mGoogleApiClient, new Util.FetchConfigDataMapCallback() {
            @Override
            public void onConfigDataMapFetched(DataMap config) {
                String selectedCity = config.getString(Util.KEY_CITY, null);
                City city = World.getCity(selectedCity);
                mCitySpinner.setAdapter(mAdapter);
                mCitySpinner.setSelection(mAdapter.getPosition(city), false);
                mCitySpinner.setOnItemSelectedListener(CompanionConfigActivity.this);
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
