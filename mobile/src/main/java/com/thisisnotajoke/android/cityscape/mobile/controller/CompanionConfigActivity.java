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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.thisisnotajoke.android.cityscape.mobile.R;
import com.thisisnotajoke.android.cityscape.mobile.Util;

public class CompanionConfigActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks {
    private Spinner mCitySpinner;
    private static String TAG = "CompanionConfigActivity";
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        mCitySpinner = (Spinner) findViewById(R.id.activity_config_city_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.city_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCitySpinner.setAdapter(adapter);
        mCitySpinner.setOnItemSelectedListener(this);

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
        String city = (String) adapterView.getItemAtPosition(i);
        if(city.equalsIgnoreCase("automatic"))
            city = null;
        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putString(Util.KEY_CITY, city);
        Util.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
