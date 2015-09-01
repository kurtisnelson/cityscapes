package com.thisisnotajoke.android.cityscape.controller;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
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
import com.thisisnotajoke.android.cityscape.R;
import com.thisisnotajoke.android.cityscape.Util;

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

        String[] cities = getResources().getStringArray(R.array.city_array);
        listView.setAdapter(new CityListAdapter(cities));

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
        updateConfigDataItem(colorItemViewHolder.mCityItem.getCity());
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

    private void updateConfigDataItem(final String city) {
        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putString(Util.KEY_CITY, city);
        Util.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }

    private class CityListAdapter extends WearableListView.Adapter {
        private final String[] mCities;

        public CityListAdapter(String[] cities) {
            mCities = cities;
        }

        @Override
        public CityItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CityItemViewHolder(new CityItem(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            CityItemViewHolder colorItemViewHolder = (CityItemViewHolder) holder;
            String cityName = mCities[position];
            colorItemViewHolder.mCityItem.setCity(cityName);

            RecyclerView.LayoutParams layoutParams =
                    new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            int colorPickerItemMargin = (int) getResources().getDimension(R.dimen.digital_config_color_picker_item_margin);
            // Add margins to first and last item to make it possible for user to tap on them.
            if (position == 0) {
                layoutParams.setMargins(0, colorPickerItemMargin, 0, 0);
            } else if (position == mCities.length - 1) {
                layoutParams.setMargins(0, 0, 0, colorPickerItemMargin);
            } else {
                layoutParams.setMargins(0, 0, 0, 0);
            }
            colorItemViewHolder.itemView.setLayoutParams(layoutParams);
        }

        @Override
        public int getItemCount() {
            return mCities.length;
        }
    }

    private static class CityItem extends LinearLayout implements WearableListView.OnCenterProximityListener {

        private final TextView mLabel;
        private String mCity;

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

        public void setCity(String cityName) {
            mCity = cityName;
            if(mCity.equalsIgnoreCase("automatic"))
                mCity = null;
            mLabel.setText(cityName);
        }

        public String getCity() {
            return mCity;
        }
    }

    private static class CityItemViewHolder extends WearableListView.ViewHolder {
        private final CityItem mCityItem;

        public CityItemViewHolder(CityItem cityItem) {
            super(cityItem);
            mCityItem = cityItem;
        }
    }

}
