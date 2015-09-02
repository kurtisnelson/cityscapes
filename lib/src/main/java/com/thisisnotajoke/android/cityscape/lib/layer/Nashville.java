package com.thisisnotajoke.android.cityscape.lib.layer;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.thisisnotajoke.android.cityscape.lib.FaceLayer;
import com.thisisnotajoke.android.cityscape.lib.R;
import com.thisisnotajoke.android.cityscape.lib.Sun;
import com.thisisnotajoke.android.cityscape.lib.SunColors;

public class Nashville extends FaceLayer {
    private final Drawable mBuilding;
    private final Drawable mHole;

    public Nashville(Resources resources) {
        super(resources);
        mBuilding = resources.getDrawable(R.drawable.nashville, null);
        mHole = resources.getDrawable(R.drawable.nashville_hole, null);
        mHole.setTint(SunColors.NIGHT);
    }

    @Override
    public void draw(Canvas canvas, Rect bounds) {
        mBuilding.setBounds(bounds.right - mBuilding.getIntrinsicWidth(), bounds.bottom - mBuilding.getIntrinsicHeight(), bounds.right, bounds.bottom);
        mBuilding.draw(canvas);
        mHole.setBounds(mBuilding.getBounds());
        mHole.draw(canvas);
    }

    @Override
    public void onAmbientModeChanged(boolean inAmbientMode) {
        super.onAmbientModeChanged(inAmbientMode);
        if(mAmbient) {
            mBuilding.setColorFilter(SunColors.AMBIENT_FILTER);
            mHole.setTint(Color.BLACK);
        } else {
            onSunUpdated(mSun);
        }
    }

    @Override
    public void onSunUpdated(Sun sun) {
        super.onSunUpdated(sun);
        switch (sun) {
            case SUNSET:
                mHole.setTint(SunColors.SUNSET);
                mBuilding.setColorFilter(SunColors.CHANGE_FILTER);
                break;
            case SUNRISE:
                mHole.setTint(SunColors.SUNRISE);
                mBuilding.setColorFilter(SunColors.CHANGE_FILTER);
                break;
            case NIGHT:
                mHole.setTint(SunColors.NIGHT);
                mBuilding.setColorFilter(SunColors.NIGHT_FILTER);
                break;
            case DAY:
                mHole.setTint(SunColors.DAY);
                mBuilding.clearColorFilter();
        }
    }
}
