package com.thisisnotajoke.android.cityscape.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.thisisnotajoke.android.cityscape.ModeManager;

public class PermissionActivity extends Activity {

  private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
  private static final String TAG = "PermissionActivity";
  private GoogleApiClient mGoogleApiClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (isMissingPermission(this)) {
      Log.d(TAG, "We do not have coarse location permissions");
      request();
    } else {
      Log.d(TAG, "We already have permission, finishing");
      finish();
    }
  }

  private void request() {
    Log.d(TAG, "Requesting permissions");
    ActivityCompat
        .requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
            MY_PERMISSIONS_REQUEST_LOCATION);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                         @NonNull int[] grantResults) {
    switch (requestCode) {
      case MY_PERMISSIONS_REQUEST_LOCATION: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          setGPS();
        } else {
          finish();
        }
      }
    }
  }

  private void setGPS() {
    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
          @Override
          public void onConnected(Bundle connectionHint) {
            ModeManager.setGPS(mGoogleApiClient);
            finish();
          }

          @Override
          public void onConnectionSuspended(int cause) {

          }
        })
        .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
          @Override
          public void onConnectionFailed(@NonNull ConnectionResult result) {

          }
        })
        .addApi(Wearable.API)
        .build();
    mGoogleApiClient.connect();
  }

  public static Intent newIntent(Context context) {
    Intent intent = new Intent(context, PermissionActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    return intent;
  }

  public static boolean isMissingPermission(Context context) {
    return ActivityCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED;
  }
}
