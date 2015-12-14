package com.thisisnotajoke.android.cityscape.mobile.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;
import com.thisisnotajoke.android.cityscape.lib.DataSyncUtil;
import com.thisisnotajoke.android.cityscape.lib.ModeManager;
import com.thisisnotajoke.android.cityscape.lib.World;
import com.thisisnotajoke.android.cityscape.lib.model.City;
import com.thisisnotajoke.android.cityscape.mobile.R;

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

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.activity_config_gps:
                if (checked)
                    setGPS();
                break;
            case R.id.activity_config_manual:
                if (checked)
                    setManual();
                break;
            case R.id.activity_config_random:
                if (checked)
                    setRandom();
                break;
        }
    }

    private void setGPS() {
        ModeManager.setGPS(mGoogleApiClient);
        mCitySpinner.setVisibility(View.GONE);
    }

    private void setManual() {
        ModeManager.setManual(mGoogleApiClient);
        mCitySpinner.setVisibility(View.VISIBLE);
    }

    private void setRandom() {
        ModeManager.setRandom(mGoogleApiClient);
        mCitySpinner.setVisibility(View.GONE);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        UUID cityId = ((City) adapterView.getItemAtPosition(i)).getID();
        Log.d(TAG, "Setting city ID to " + cityId);
        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putString(DataSyncUtil.KEY_CITY, cityId.toString());
        DataSyncUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        DataSyncUtil.fetchConfigDataMap(mGoogleApiClient, new DataSyncUtil.FetchConfigDataMapCallback() {
            @Override
            public void onConfigDataMapFetched(DataMap config) {
                String selectedCity = config.getString(DataSyncUtil.KEY_CITY, null);
                City city = World.getCity(selectedCity);
                mCitySpinner.setAdapter(mAdapter);
                mCitySpinner.setSelection(mAdapter.getPosition(city), false);
                mCitySpinner.setOnItemSelectedListener(CompanionConfigActivity.this);

                int mode = config.getInt(DataSyncUtil.KEY_MODE, DataSyncUtil.MODE_GPS);
                switch (mode) {
                    case DataSyncUtil.MODE_GPS:
                        break;
                    case DataSyncUtil.MODE_RANDOM:
                        break;
                    case DataSyncUtil.MODE_MANUAL:
                        mCitySpinner.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
