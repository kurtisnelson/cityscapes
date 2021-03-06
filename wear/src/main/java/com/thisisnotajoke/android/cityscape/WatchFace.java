package com.thisisnotajoke.android.cityscape;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.animation.DecelerateInterpolator;

import com.thisisnotajoke.android.cityscape.layer.Rural;
import com.thisisnotajoke.android.cityscape.layer.Sky;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ch.hsr.geohash.WGS84Point;

public class WatchFace extends CanvasWatchFaceService {

  private static final String TAG = "WatchFace";

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

  private class Engine extends CanvasWatchFaceService.Engine implements ConfigManager.Listener {

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

    private SparseArray<FaceLayer> mLayers = new SparseArray<>();
    private static final int SKY_INDEX = 0;
    private static final int CITY_INDEX = 1;

    private ValueAnimator mBottomBoundAnimator = new ValueAnimator();
    private Rect mCardBounds = new Rect();
    private int mHeight = 0;
    private ConfigManager mConfigManager = new ConfigManager();

    @Override
    public void onCreate(SurfaceHolder holder) {
      super.onCreate(holder);
      mConfigManager.onCreate(WatchFace.this, this);
      mResources = WatchFace.this.getResources();
      mLayers.put(SKY_INDEX, new Sky(mResources));
      mLayers.put(CITY_INDEX, new Rural(getResources()));
      SunColors.initialize(getResources());

      setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFace.this)
              .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
              .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
              .setShowSystemUiTime(false)
              .setHotwordIndicatorGravity(Gravity.TOP | Gravity.END)
              .setStatusBarGravity(Gravity.TOP | Gravity.START)
              .build());
      mTextPaint = createTextPaint(mResources.getColor(R.color.text));

      // Setup time
      mZone = DateTimeZone.forID(TimeZone.getDefault().getID());
      if (DateFormat.is24HourFormat(WatchFace.this)) {
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
      mConfigManager.onDestroy();
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

    @Override
    public void onCityChanged(UUID city) {
      if (city != null) {
        mLayers.put(CITY_INDEX, World.getCityFace(mResources, city));
      } else {
        mLayers.put(CITY_INDEX, World.getCurrentCityFace(mResources, mCurrentPoint));
      }
      postInvalidate();
    }

    @Override
    public void onAmbientModeChanged(boolean inAmbientMode) {
      super.onAmbientModeChanged(inAmbientMode);
      if (mAmbient != inAmbientMode) {
        for (int i = 0; i < mLayers.size(); i++) {
          mLayers.valueAt(i).onAmbientModeChanged(inAmbientMode);
        }
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
        mBottomBoundAnimator.setFloatValues((Float) mBottomBoundAnimator.getAnimatedValue(),
                mCardBounds.top > 0 ? mCardBounds.top : mHeight);
        mBottomBoundAnimator.setDuration(200);
        mBottomBoundAnimator.start();
        postInvalidate();
      }
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
      Rect restrictedBounds = new Rect(bounds);
      restrictedBounds.bottom = ((Float) mBottomBoundAnimator.getAnimatedValue()).intValue();
      for (int i = 0; i < mLayers.size(); i++) {
        mLayers.valueAt(i).draw(canvas, bounds);
      }
      drawClock(canvas, restrictedBounds);

      if (mBottomBoundAnimator.isRunning()) {
        postInvalidate();
      }
    }

    private void updateSun() {
      Sun sun = World.calculateSun(mCurrentPoint, DateTime.now(mZone));
      if (sun != mSun) {
        Log.d(TAG, "Sun is now " + sun);
        mSun = sun;
        for (int i = 0; i < mLayers.size(); i++) {
          mLayers.valueAt(i).onSunUpdated(sun);
        }
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
      mConfigManager.enable();
      if (mRegisteredTimeZoneReceiver) {
        return;
      }
      mRegisteredTimeZoneReceiver = true;
      IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
      WatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
    }

    private void unregisterReceiver() {
      mConfigManager.disable();
      if (!mRegisteredTimeZoneReceiver) {
        return;
      }
      mRegisteredTimeZoneReceiver = false;
      WatchFace.this.unregisterReceiver(mTimeZoneReceiver);
    }

    /**
     * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently or
     * stops it if it shouldn't be running but currently is.
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
    public void randomCity() {
      Log.d(TAG, "Picking random city face");
      mLayers.put(CITY_INDEX, World.getRandomCityFace(getResources()));
      postInvalidate();
    }

    @Override
    public void onLocationChanged(Location location) {
      if (location == null) {
        Logger.w(TAG, "Got null location");
        return;
      }
      Logger.d(TAG, "New location: %s", location);
      if (mCurrentPoint == null || location.getLongitude() != mCurrentPoint.getLongitude()
              || location.getLatitude() != mCurrentPoint.getLatitude()) {
        Logger.v(TAG, "Selecting face based on location");
        mCurrentPoint = new WGS84Point(location.getLatitude(), location.getLongitude());
        mLayers.put(CITY_INDEX, World.getCurrentCityFace(mResources, mCurrentPoint));
        postInvalidate();
      } else {
        Logger.v(TAG, "Location didn't change enough, skipping invalidate");
      }
    }
  }

  private static class EngineHandler extends Handler {

    private final WeakReference<WatchFace.Engine> mWeakReference;

    EngineHandler(WatchFace.Engine reference) {
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
