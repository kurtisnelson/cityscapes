package com.thisisnotajoke.android.cityscape.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;
import com.thisisnotajoke.android.cityscape.DataSyncUtil;
import com.thisisnotajoke.android.cityscape.R;
import com.thisisnotajoke.android.cityscape.World;
import com.thisisnotajoke.android.cityscape.model.City;
import com.thisisnotajoke.android.cityscape.view.ListItemViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CityConfigActivity extends Activity implements
        WearableListView.ClickListener {

  private static final String TAG = "CityConfigActivity";
  private GoogleApiClient mGoogleApiClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list);

    WearableListView listView = (WearableListView) findViewById(R.id.wearable_list);

    listView.setHasFixedSize(true);
    listView.setClickListener(this);
    listView.setAdapter(new CityListAdapter());

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

  @Override
  public void onClick(WearableListView.ViewHolder viewHolder) {
    ListItemViewHolder colorItemViewHolder = (ListItemViewHolder) viewHolder;
    UUID id = (UUID) colorItemViewHolder.getTag();
    updateConfigDataItem(id.toString());
    finish();
  }

  @Override
  public void onTopEmptyRegionClick() {

  }

  private void updateConfigDataItem(final String cityId) {
    DataMap configKeysToOverwrite = new DataMap();
    configKeysToOverwrite.putString(DataSyncUtil.KEY_CITY, cityId);
    DataSyncUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);

  }

  static Intent newIntent(Context context) {
    return new Intent(context, CityConfigActivity.class);
  }

  private class CityListAdapter extends WearableListView.Adapter {

    private final List<City> mCities;

    CityListAdapter() {
      mCities = new ArrayList<>(Arrays.asList(World.CITIES));
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new ListItemViewHolder(getLayoutInflater().inflate(R.layout.list_item, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
      ListItemViewHolder colorItemViewHolder = (ListItemViewHolder) holder;
      City city = mCities.get(position);
      colorItemViewHolder.setText(city.getName());
      colorItemViewHolder.setTag(city.getID());
    }

    @Override
    public int getItemCount() {
      return mCities.size();
    }
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
