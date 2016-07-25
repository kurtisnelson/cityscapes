package com.thisisnotajoke.android.cityscape.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.view.WearableListView;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.thisisnotajoke.android.cityscape.ModeManager;
import com.thisisnotajoke.android.cityscape.R;

public class ModeConfigActivity extends Activity implements WearableListView.ClickListener {

  private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;

  private static final String TAG = "ModeConfigActivity";
  private static final int REQUEST_CITY = 0;
  private GoogleApiClient mGoogleApiClient;

  private static final String[] MODES = {"GPS", "Manual", "Random"};

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list);

    setupGoogleClient();
    WearableListView listView =
            (WearableListView) findViewById(R.id.wearable_list);

    listView.setAdapter(new ListViewAdapter(this, MODES));
    listView.setClickListener(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mGoogleApiClient.connect();
  }

  @Override
  protected void onStop() {
    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
      mGoogleApiClient.disconnect();
    }
    super.onStop();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                         @NonNull int[] grantResults) {
    switch (requestCode) {
      case MY_PERMISSIONS_REQUEST_LOCATION: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          finish();
        } else {
          setManual();
        }
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQUEST_CITY:
        finish();
    }
  }

  @Override
  public void onClick(WearableListView.ViewHolder viewHolder) {
    int tag = (Integer) viewHolder.itemView.getTag();
    switch (tag) {
      case 0:
        setGPS();
        break;
      case 1:
        setManual();
        break;
      case 2:
        setRandom();
        break;
    }
  }

  @Override
  public void onTopEmptyRegionClick() {

  }


  private void setGPS() {
    ModeManager.setGPS(mGoogleApiClient);
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat
              .requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                      MY_PERMISSIONS_REQUEST_LOCATION);
    } else {
      finish();
    }
  }

  private void setManual() {
    ModeManager.setManual(mGoogleApiClient);
    startActivityForResult(CityConfigActivity.newIntent(this), REQUEST_CITY);
  }

  private void setRandom() {
    ModeManager.setRandom(mGoogleApiClient);
    finish();
  }

  private void setupGoogleClient() {
    mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
              @Override
              public void onConnected(Bundle connectionHint) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                  Log.d(TAG, "onConnected: " + connectionHint);
                }
              }

              @Override
              public void onConnectionSuspended(int cause) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                  Log.d(TAG, "onConnectionSuspended: " + cause);
                }
              }
            })
            .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
              @Override
              public void onConnectionFailed(ConnectionResult result) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                  Log.d(TAG, "onConnectionFailed: " + result);
                }
              }
            })
            .addApi(Wearable.API)
            .build();
  }
}
