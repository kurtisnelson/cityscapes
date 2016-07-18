package com.thisisnotajoke.android.cityscape.layer;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.thisisnotajoke.android.cityscape.FaceLayer;
import com.thisisnotajoke.android.cityscape.Sun;
import com.thisisnotajoke.android.cityscape.SunColors;

public abstract class SimpleCity extends FaceLayer {

  protected final Drawable mBuilding;

  public SimpleCity(Drawable building, Resources resources) {
    super(resources);
    mBuilding = building;
  }

  @Override
  public void draw(Canvas canvas, Rect bounds) {
    mBuilding.setBounds(bounds.right - mBuilding.getIntrinsicWidth(),
        bounds.bottom - mBuilding.getIntrinsicHeight(), bounds.right, bounds.bottom);
    mBuilding.draw(canvas);
  }

  @Override
  public void onAmbientModeChanged(boolean inAmbientMode) {
    super.onAmbientModeChanged(inAmbientMode);
    if (mAmbient) {
      mBuilding.setColorFilter(SunColors.AMBIENT_FILTER);
    } else {
      onSunUpdated(mSun);
    }
  }

  @Override
  public void onSunUpdated(Sun sun) {
    super.onSunUpdated(sun);
    switch (sun) {
      case SUNSET:
        mBuilding.setColorFilter(SunColors.CHANGE_FILTER);
        break;
      case SUNRISE:
        mBuilding.setColorFilter(SunColors.CHANGE_FILTER);
        break;
      case NIGHT:
        mBuilding.setColorFilter(SunColors.NIGHT_FILTER);
        break;
      case DAY:
        mBuilding.clearColorFilter();
    }
  }
}
