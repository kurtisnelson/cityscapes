package com.thisisnotajoke.android.cityscape;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.thisisnotajoke.android.cityscape.controller.PermissionActivity;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


class ConfigManager implements GoogleApiClient.ConnectionCallbacks, LocationListener, DataApi.DataListener {
  private static final String TAG = "ConfigManager";
  private static final float MIN_DISPLACEMENT = 500;

  private GoogleApiClient mGoogleApiClient;
  private Listener mListener;
  private Location mLastLocation;
  private Context mContext;

  interface Listener {
    void randomCity();
    void onCityChanged(UUID id);
    void onLocationChanged(Location location);
  }

  void onCreate(Context context, Listener listener) {
    mContext = context.getApplicationContext();
    mListener = listener;
    mGoogleApiClient = new GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)
            .addApi(Wearable.API)  // used for data layer API
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
              @Override
              public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Logger.e(TAG, "Failed to connect to play services: %s", connectionResult);
              }
            })
            .build();
    mGoogleApiClient.connect();
    if (mLastLocation != null) {
      listener.onLocationChanged(mLastLocation);
    }
  }

  void onDestroy() {
    mListener = null;
  }

  void enable() {
    if (mListener == null) {
      throw new IllegalStateException("Listener is null");
    }
    Logger.v(TAG, "enabled");
    mGoogleApiClient.connect();
  }

  void disable() {
    Logger.v(TAG, "disabled");
    if (mGoogleApiClient.isConnected()) {
      LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
      Wearable.DataApi.removeListener(mGoogleApiClient, this);
      mGoogleApiClient.disconnect();
    }
  }

  @Override
  public void onConnected(Bundle bundle) {
    Wearable.DataApi.addListener(mGoogleApiClient, this);
    DataSyncUtil
            .fetchConfigDataMap(mGoogleApiClient, new DataSyncUtil.FetchConfigDataMapCallback() {
              @Override
              public void onConfigDataMapFetched(DataMap config) {
                onConfigChanged(config.getInt(DataSyncUtil.KEY_MODE, DataSyncUtil.MODE_GPS),
                        config.getString(DataSyncUtil.KEY_CITY));
              }
            });
  }

  @Override
  public void onConnectionSuspended(int i) {
    Logger.i(TAG, "play services connection suspended");
  }

  private void onConfigChanged(int mode, String cityId) {
    Logger.d(TAG, "configuration changed to mode %d, city %s", mode, cityId);
    switch (mode) {
      case DataSyncUtil.MODE_GPS:
        requestLocationUpdate();
        break;
      case DataSyncUtil.MODE_MANUAL:
        UUID id;
        try {
          id = UUID.fromString(cityId);
        } catch (NullPointerException e) {
          Log.e(TAG, "Invalid manual UUID, using default");
          id = null;
        }
        mListener.onCityChanged(id);
        break;
      case DataSyncUtil.MODE_RANDOM:
        mListener.randomCity();
        break;
    }
  }

  @Override
  public void onDataChanged(DataEventBuffer dataEvents) {
    for (DataEvent event : dataEvents) {
      if (event.getType() == DataEvent.TYPE_CHANGED) {
        // DataItem changed
        DataItem item = event.getDataItem();
        if (item.getUri().getPath().compareTo(DataSyncUtil.PATH_WITH_FEATURE) == 0) {
          DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
          String cityId = dataMap.getString(DataSyncUtil.KEY_CITY);
          int mode = dataMap.getInt(DataSyncUtil.KEY_MODE);
          onConfigChanged(mode, cityId);
        }
      }
    }
  }

  @Override
  public void onLocationChanged(Location location) {
    mLastLocation = location;
    mListener.onLocationChanged(location);
  }

  @SuppressWarnings("MissingPermission")
  private void requestLocationUpdate() {
    if (!mGoogleApiClient.isConnected()) {
      Logger.w(TAG, "requestLocationUpdate called with disconnected api client");
      return;
    }

    if (PermissionActivity.isMissingPermission(mContext)) {
      Logger.i(TAG, "Missing location permissions");
      mContext.startActivity(PermissionActivity.newIntent(mContext));
      return;
    }
    if(mLastLocation == null) {
      Logger.v(TAG, "Location unknown, requesting one-off location");
      LocationRequest oneOff = LocationRequest.create()
              .setNumUpdates(1)
              .setExpirationDuration(TimeUnit.MINUTES.toMillis(1))
              .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
      LocationServices.FusedLocationApi
              .requestLocationUpdates(mGoogleApiClient, oneOff, this)
              .setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                  if (!status.isSuccess()) {
                    Logger.w(TAG, "Couldn't request one-off location %s", status.getStatusMessage());
                  } else {
                    Logger.v(TAG, "Single location requested");
                  }
                }
              });
    }

    LocationRequest locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_LOW_POWER)
            .setFastestInterval(TimeUnit.SECONDS.toMillis(30))
            .setSmallestDisplacement(MIN_DISPLACEMENT)
            .setInterval(TimeUnit.MINUTES.toMillis(5));

    LocationServices.FusedLocationApi
            .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
            .setResultCallback(new ResultCallback<Status>() {
              @Override
              public void onResult(@NonNull Status status) {
                if (!status.isSuccess()) {
                  Logger.w(TAG, "Couldn't request location %s", status.getStatusMessage());
                } else {
                  Logger.d(TAG, "Low power location updates requested");
                }
              }
            });
  }
}
