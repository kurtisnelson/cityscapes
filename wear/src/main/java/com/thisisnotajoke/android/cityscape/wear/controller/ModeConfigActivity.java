package com.thisisnotajoke.android.cityscape.wear.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.thisisnotajoke.android.cityscape.ModeManager;
import com.thisisnotajoke.android.cityscape.wear.R;

public class ModeConfigActivity extends Activity {

  private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;

  private static final String TAG = "ModeConfigActivity";
  private static final int REQUEST_CITY = 0;
  private GoogleApiClient mGoogleApiClient;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mode_config);

    setupGoogleClient();
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

  public void onRadioButtonClicked(View view) {
    // Is the button now checked?
    boolean checked = ((RadioButton) view).isChecked();

    // Check which radio button was clicked
    switch (view.getId()) {
      case R.id.activity_mode_config_gps:
        if (checked) {
          setGPS();
        }
        break;
      case R.id.activity_mode_config_manual:
        if (checked) {
          setManual();
        }
        break;
      case R.id.activity_mode_config_random:
        if (checked) {
          setRandom();
        }
        break;
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[],
      int[] grantResults) {
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
