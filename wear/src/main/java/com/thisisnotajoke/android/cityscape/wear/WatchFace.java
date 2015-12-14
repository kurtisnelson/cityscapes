/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thisisnotajoke.android.cityscape.wear;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.animation.DecelerateInterpolator;

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
import com.thisisnotajoke.android.cityscape.lib.DataSyncUtil;
import com.thisisnotajoke.android.cityscape.lib.FaceLayer;
import com.thisisnotajoke.android.cityscape.lib.Sun;
import com.thisisnotajoke.android.cityscape.lib.SunColors;
import com.thisisnotajoke.android.cityscape.lib.World;
import com.thisisnotajoke.android.cityscape.lib.layer.Rural;
import com.thisisnotajoke.android.cityscape.lib.layer.Sky;
import com.thisisnotajoke.android.cityscape.wear.controller.PermissionActivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ch.hsr.geohash.WGS84Point;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class WatchFace extends CanvasWatchFaceService {
    private static final String TAG = "WatchFace";

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.MINUTES.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;
    private String mFormatString;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements GoogleApiClient.ConnectionCallbacks, LocationListener, DataApi.DataListener {

        final Handler mUpdateTimeHandler = new EngineHandler(this);

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mZone = DateTimeZone.forID(intent.getStringExtra("time-zone"));
            }
        };

        boolean mRegisteredTimeZoneReceiver = false;

        Paint mTextPaint;

        boolean mAmbient;
        boolean mLowBitAmbient;
        private DateTimeZone mZone;

        private Sun mSun;
        private WGS84Point mCurrentPoint;
        private Resources mResources;
        private GoogleApiClient mGoogleApiClient;

        private FaceLayer mCity;
        private FaceLayer mSky = new Sky(mResources);

        private ValueAnimator mBottomBoundAnimator = new ValueAnimator();
        private Rect mCardBounds = new Rect();
        private int mHeight = 0;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            mCity = new Rural(getResources());
            SunColors.initialize(getResources());

            mGoogleApiClient = new GoogleApiClient.Builder(WatchFace.this)
                    .addApi(LocationServices.API)
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

            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setHotwordIndicatorGravity(Gravity.TOP | Gravity.RIGHT)
                    .setStatusBarGravity(Gravity.TOP | Gravity.LEFT)
                    .build());
            mResources = WatchFace.this.getResources();
            mTextPaint = createTextPaint(mResources.getColor(R.color.text));

            // Setup time
            mZone = DateTimeZone.forID(TimeZone.getDefault().getID());
            if(DateFormat.is24HourFormat(WatchFace.this)) {
                mFormatString = "HH:mm";
            } else {
                mFormatString = "h:mm";
            }

            // Add context
            updateSun();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            updateSun();
            invalidate();
        }

        public void onCityChanged(UUID city) {
            if(city != null) {
                mCity = World.getCityFace(mResources, city);
            } else {
                mCity = World.getCurrentCityFace(mResources, mCurrentPoint);
            }
            postInvalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mCity.onAmbientModeChanged(inAmbientMode);
                mSky.onAmbientModeChanged(inAmbientMode);
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mHeight = height;
            mBottomBoundAnimator.cancel();
            mBottomBoundAnimator.setFloatValues(height, width);
            mBottomBoundAnimator.setInterpolator(new DecelerateInterpolator(3));
            mBottomBoundAnimator.setDuration(0);
            mBottomBoundAnimator.start();

        }

        @Override
        public void onPeekCardPositionUpdate(Rect bounds) {
            super.onPeekCardPositionUpdate(bounds);
            if (!bounds.equals(mCardBounds)) {
                mCardBounds.set(bounds);

                mBottomBoundAnimator.cancel();
                mBottomBoundAnimator.setFloatValues((Float) mBottomBoundAnimator.getAnimatedValue(), mCardBounds.top > 0 ? mCardBounds.top : mHeight);
                mBottomBoundAnimator.setDuration(200);
                mBottomBoundAnimator.start();
                postInvalidate();
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            Rect restrictedBounds = new Rect(bounds);
            restrictedBounds.bottom = ((Float) mBottomBoundAnimator.getAnimatedValue()).intValue();
            mSky.draw(canvas, bounds);
            mCity.draw(canvas, bounds);
            drawClock(canvas, restrictedBounds);

            if (mBottomBoundAnimator.isRunning()) {
                postInvalidate();
            }
        }

        private void updateSun() {
            Sun sun = World.calculateSun(mCurrentPoint, DateTime.now(mZone));
            if(sun != mSun) {
                Log.d(TAG, "Sun is now " + sun);
                mSun = sun;
                mCity.onSunUpdated(mSun);
                mSky.onSunUpdated(mSun);
            }
        }

        private Paint createTextPaint(int textColor) {
            Typeface typeface = Typeface.create("sans-serif-light", Typeface.NORMAL);
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            paint.setTextSize(mResources.getDimension(R.dimen.digital_text_size));
            return paint;
        }

        private void drawClock(Canvas canvas, Rect bounds) {
            String text = DateTime.now(mZone).toString(mFormatString);
            float textWidth = mTextPaint.measureText(text);
            float textY = bounds.exactCenterY() - ((mTextPaint.ascent() + mTextPaint.descent()) / 2);
            canvas.drawText(text, bounds.centerX() - (textWidth / 2), textY, mTextPaint);
        }

        private void registerReceiver() {
            mGoogleApiClient.connect();
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            WatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if(mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                Wearable.DataApi.removeListener(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            WatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            onTimeTick();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        @Override
        public void onConnected(Bundle bundle) {
            Wearable.DataApi.addListener(mGoogleApiClient, this);
            DataSyncUtil.fetchConfigDataMap(mGoogleApiClient, new DataSyncUtil.FetchConfigDataMapCallback() {
                @Override
                public void onConfigDataMapFetched(DataMap config) {
                    onConfigChanged(config.getInt(DataSyncUtil.KEY_MODE, DataSyncUtil.MODE_GPS), config.getString(DataSyncUtil.KEY_CITY));
                }
            });
        }

        private void onConfigChanged(int mode, String cityId) {
            switch (mode) {
                case DataSyncUtil.MODE_GPS:
                    mCurrentPoint = null;
                    requestLocationUpdate();
                    break;
                case DataSyncUtil.MODE_MANUAL:
                    UUID id;
                    try {
                        id = UUID.fromString(cityId);
                    } catch (NullPointerException e) {
                        id = null;
                    }
                    onCityChanged(id);
                    break;
                case DataSyncUtil.MODE_RANDOM:
                    randomCity();
                    break;
            }
        }

        private void randomCity() {
            Log.d(TAG, "Picking random city face");
            mCity = World.getRandomCityFace(getResources());
            postInvalidate();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        private void requestLocationUpdate() {
            if(!mGoogleApiClient.isConnected())
                return;
            int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
            if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Missing location permissions");
                startActivity(PermissionActivity.newIntent(getApplicationContext()));
                return;
            }
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                    .setFastestInterval(TimeUnit.SECONDS.toMillis(30))
                    .setInterval(TimeUnit.MINUTES.toMillis(5));

            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (!status.isSuccess()) {
                                Log.w(TAG, "Couldn't request location: " + status.getStatusMessage());
                                onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
                            } else {
                                Log.d(TAG, "Location updates requested");
                            }
                        }
                    });
        }

        @Override
        public void onLocationChanged(Location location) {
            if(location == null) {
                Log.w(TAG, "Got null location");
                return;
            }
            Log.d(TAG, "recieved " + location);
            if(mCurrentPoint == null || location.getLongitude() != mCurrentPoint.getLongitude() || location.getLatitude() != mCurrentPoint.getLatitude()) {
                Log.d(TAG, "Selecting face based on location");
                mCurrentPoint = new WGS84Point(location.getLatitude(), location.getLongitude());
                mCity = World.getCurrentCityFace(mResources, mCurrentPoint);
                postInvalidate();
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
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<WatchFace.Engine> mWeakReference;

        public EngineHandler(WatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            WatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }
}
