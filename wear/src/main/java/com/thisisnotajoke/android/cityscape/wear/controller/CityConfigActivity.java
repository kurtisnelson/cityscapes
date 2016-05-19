package com.thisisnotajoke.android.cityscape.wear.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;
import com.thisisnotajoke.android.cityscape.DataSyncUtil;
import com.thisisnotajoke.android.cityscape.World;
import com.thisisnotajoke.android.cityscape.model.City;
import com.thisisnotajoke.android.cityscape.wear.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CityConfigActivity extends Activity implements
        WearableListView.ClickListener, WearableListView.OnScrollListener {

    private static final String TAG = "CityConfigActivity";
    private TextView mHeader;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_config);

        mHeader = (TextView) findViewById(R.id.header);
        WearableListView listView = (WearableListView) findViewById(R.id.color_picker);
        BoxInsetLayout content = (BoxInsetLayout) findViewById(R.id.content);
        // BoxInsetLayout adds padding by default on round devices. Add some on square devices.
        content.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                if (!insets.isRound()) {
                    v.setPaddingRelative(
                            (int) getResources().getDimensionPixelSize(R.dimen.content_padding_start),
                            v.getPaddingTop(),
                            v.getPaddingEnd(),
                            v.getPaddingBottom());
                }
                return v.onApplyWindowInsets(insets);
            }
        });

        listView.setHasFixedSize(true);
        listView.setClickListener(this);
        listView.addOnScrollListener(this);

        listView.setAdapter(new CityListAdapter());

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
        CityItemViewHolder colorItemViewHolder = (CityItemViewHolder) viewHolder;
        UUID id = colorItemViewHolder.mCityItem.mId;
        updateConfigDataItem(id.toString());
        finish();
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    @Override
    public void onScroll(int i) {

    }

    @Override
    public void onAbsoluteScrollChange(int i) {
        float newTranslation = Math.min(-i, 0);
        mHeader.setTranslationY(newTranslation);
    }

    @Override
    public void onScrollStateChanged(int i) {

    }

    @Override
    public void onCentralPositionChanged(int i) {

    }

    private void updateConfigDataItem(final String cityId) {
        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putString(DataSyncUtil.KEY_CITY, cityId);
        DataSyncUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, CityConfigActivity.class);
    }

    private class CityListAdapter extends WearableListView.Adapter {
        private final List<City> mCities;

        public CityListAdapter() {
            mCities = new ArrayList<>(Arrays.asList(World.CITIES));
        }

        @Override
        public CityItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CityItemViewHolder(new CityItem(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            CityItemViewHolder colorItemViewHolder = (CityItemViewHolder) holder;
            colorItemViewHolder.bind(mCities.get(position));
        }

        @Override
        public int getItemCount() {
            return mCities.size();
        }
    }

    private static class CityItem extends LinearLayout implements WearableListView.OnCenterProximityListener {
        private final TextView mLabel;
        public UUID mId;

        public CityItem(Context context) {
            super(context);
            View.inflate(context, R.layout.city_picker_item, this);
            mLabel = (TextView) findViewById(R.id.label);
        }

        @Override
        public void onCenterPosition(boolean b) {

        }

        @Override
        public void onNonCenterPosition(boolean b) {

        }
    }

    private static class CityItemViewHolder extends WearableListView.ViewHolder {
        private final CityItem mCityItem;

        public CityItemViewHolder(CityItem cityItem) {
            super(cityItem);
            mCityItem = cityItem;
        }

        public void bind(City city) {
            mCityItem.mLabel.setText(city.getName());
            mCityItem.mId = city.getID();
        }
    }

}
